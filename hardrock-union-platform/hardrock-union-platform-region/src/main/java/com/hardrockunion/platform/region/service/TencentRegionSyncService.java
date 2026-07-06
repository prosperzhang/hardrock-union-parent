package com.hardrockunion.platform.region.service;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.region.client.TencentDistrictResponse;
import com.hardrockunion.platform.region.client.TencentDistrictResponse.DistrictItem;
import com.hardrockunion.platform.region.client.TencentDistrictResponse.Location;
import com.hardrockunion.platform.region.config.TencentMapProperties;
import com.hardrockunion.platform.region.domain.entity.SysRegion;
import com.hardrockunion.platform.region.dto.RegionSyncResponse;
import com.hardrockunion.platform.region.mapper.SysRegionMapper;

@Service
public class TencentRegionSyncService {

    private static final String ROOT_PARENT_CODE = "0";

    private static final int MAX_LEVEL = 3;

    private static final int MAX_RETRY_COUNT = 3;

    private final SysRegionMapper sysRegionMapper;

    private final TencentMapProperties properties;

    private final JdbcTemplate jdbcTemplate;

    private final RestClient restClient;

    public TencentRegionSyncService(SysRegionMapper sysRegionMapper, TencentMapProperties properties, JdbcTemplate jdbcTemplate) {
        this.sysRegionMapper = sysRegionMapper;
        this.properties = properties;
        this.jdbcTemplate = jdbcTemplate;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }

    public RegionSyncResponse syncTencentRegions() {
        if (StringUtils.isBlank(properties.getApiKey())) {
            throw new BusinessException("请先配置 hardrock.integration.tencent-map.api-key 或环境变量 TENCENT_MAP_API_KEY");
        }
        ensureSortColumn();
        int savedCount = syncChildren(ROOT_PARENT_CODE, 0, "");
        return new RegionSyncResponse(savedCount);
    }

    private void ensureSortColumn() {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'sys_region'
              AND COLUMN_NAME = 'sort'
            """, Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE sys_region ADD COLUMN sort INT NOT NULL DEFAULT 0 COMMENT '排序' AFTER longitude");
        }
    }

    private int syncChildren(String parentCode, int parentLevel, String parentFullName) {
        TencentDistrictResponse response = getChildren(parentCode);
        if (response == null || response.getStatus() == null || response.getStatus() != 0) {
            throw new BusinessException("腾讯行政区划接口调用失败: " + (response == null ? "empty response" : response.getMessage()));
        }
        List<List<DistrictItem>> result = response.getResult();
        if (result == null || result.isEmpty() || result.getFirst() == null) {
            return 0;
        }

        int level = parentLevel + 1;
        int savedCount = 0;
        List<DistrictItem> children = result.getFirst();
        for (int i = 0; i < children.size(); i++) {
            DistrictItem item = children.get(i);
            if (StringUtils.isBlank(item.getId())) {
                continue;
            }
            String name = StringUtils.defaultIfBlank(item.getFullname(), item.getName());
            String fullName = StringUtils.isBlank(parentFullName) ? name : parentFullName + "/" + name;
            saveOrUpdate(item, parentCode, level, fullName, i);
            savedCount++;
            if (level < MAX_LEVEL) {
                savedCount += syncChildren(item.getId(), level, fullName);
            }
        }
        return savedCount;
    }

    private TencentDistrictResponse getChildren(String parentCode) {
        for (int retry = 0; retry <= MAX_RETRY_COUNT; retry++) {
            sleep(retry == 0 ? 250L : 1000L);
            TencentDistrictResponse response = restClient.get()
                .uri(uriBuilder -> buildGetChildrenUri(uriBuilder, parentCode))
                .retrieve()
                .body(TencentDistrictResponse.class);
            if (!isRateLimited(response)) {
                return response;
            }
        }
        return restClient.get()
            .uri(uriBuilder -> buildGetChildrenUri(uriBuilder, parentCode))
            .retrieve()
            .body(TencentDistrictResponse.class);
    }

    private boolean isRateLimited(TencentDistrictResponse response) {
        return response != null
            && response.getStatus() != null
            && response.getStatus() != 0
            && StringUtils.contains(response.getMessage(), "每秒请求量");
    }

    private void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException("腾讯行政区划同步已中断");
        }
    }

    private URI buildGetChildrenUri(UriBuilder uriBuilder, String parentCode) {
        UriBuilder builder = uriBuilder
            .path("/ws/district/v1/getchildren")
            .queryParam("key", properties.getApiKey());
        if (!ROOT_PARENT_CODE.equals(parentCode)) {
            builder.queryParam("id", parentCode);
        }
        return builder.build();
    }

    private void saveOrUpdate(DistrictItem item, String parentCode, int level, String fullName, int sort) {
        SysRegion region = sysRegionMapper.selectOne(new LambdaQueryWrapper<SysRegion>()
            .eq(SysRegion::getCode, item.getId())
            .last("limit 1"));
        if (region == null) {
            region = new SysRegion();
            region.setCode(item.getId());
            region.setDeleted(0);
        }

        region.setName(StringUtils.defaultIfBlank(item.getFullname(), item.getName()));
        region.setParentCode(parentCode);
        region.setLevel(level);
        region.setFullName(fullName);
        region.setSort(sort);
        Location location = item.getLocation();
        if (location != null) {
            region.setLatitude(location.getLat());
            region.setLongitude(location.getLng());
        }

        if (region.getId() == null) {
            sysRegionMapper.insert(region);
        } else {
            sysRegionMapper.updateById(region);
        }
    }
}
