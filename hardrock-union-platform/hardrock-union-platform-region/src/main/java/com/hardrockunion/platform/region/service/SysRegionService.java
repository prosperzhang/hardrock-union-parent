package com.hardrockunion.platform.region.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.region.domain.entity.SysRegion;
import com.hardrockunion.platform.region.dto.RegionResponse;
import com.hardrockunion.platform.region.mapper.SysRegionMapper;

@Service
public class SysRegionService {

    private final SysRegionMapper sysRegionMapper;

    public SysRegionService(SysRegionMapper sysRegionMapper) {
        this.sysRegionMapper = sysRegionMapper;
    }

    public List<RegionResponse> listChildren(String parentCode) {
        String normalizedParentCode = StringUtils.defaultIfBlank(StringUtils.trimToNull(parentCode), "0");
        return sysRegionMapper.selectList(new LambdaQueryWrapper<SysRegion>()
                .eq(SysRegion::getParentCode, normalizedParentCode)
                .eq(SysRegion::getDeleted, 0)
                .orderByAsc(SysRegion::getSort)
                .orderByAsc(SysRegion::getCode))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public RegionResponse getByCode(String code) {
        String normalizedCode = StringUtils.trimToNull(code);
        if (normalizedCode == null) {
            throw new BusinessException("code 不能为空");
        }
        SysRegion region = sysRegionMapper.selectOne(new LambdaQueryWrapper<SysRegion>()
            .eq(SysRegion::getCode, normalizedCode)
            .eq(SysRegion::getDeleted, 0)
            .last("limit 1"));
        if (region == null) {
            throw new BusinessException("行政区域不存在");
        }
        return toResponse(region);
    }

    RegionResponse toResponse(SysRegion region) {
        RegionResponse response = new RegionResponse();
        response.setId(region.getId());
        response.setCode(region.getCode());
        response.setName(region.getName());
        response.setParentCode(region.getParentCode());
        response.setLevel(region.getLevel());
        response.setFullName(region.getFullName());
        response.setLatitude(region.getLatitude());
        response.setLongitude(region.getLongitude());
        return response;
    }
}
