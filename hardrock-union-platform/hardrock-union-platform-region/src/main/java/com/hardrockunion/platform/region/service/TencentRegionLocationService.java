package com.hardrockunion.platform.region.service;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.region.client.TencentGeocoderResponse;
import com.hardrockunion.platform.region.client.TencentGeocoderResponse.AddressComponent;
import com.hardrockunion.platform.region.client.TencentGeocoderResponse.AddressReference;
import com.hardrockunion.platform.region.client.TencentGeocoderResponse.AddressInfo;
import com.hardrockunion.platform.region.client.TencentGeocoderResponse.GeocoderResult;
import com.hardrockunion.platform.region.client.TencentGeocoderResponse.Poi;
import com.hardrockunion.platform.region.client.TencentGeocoderResponse.ReferenceItem;
import com.hardrockunion.platform.region.config.TencentMapProperties;
import com.hardrockunion.platform.region.domain.entity.SysRegion;
import com.hardrockunion.platform.region.dto.RegionLocateResponse;
import com.hardrockunion.platform.region.mapper.SysRegionMapper;

@Service
public class TencentRegionLocationService {

    private final TencentMapProperties properties;

    private final SysRegionMapper sysRegionMapper;

    private final SysRegionService sysRegionService;

    private final RestClient restClient;

    public TencentRegionLocationService(TencentMapProperties properties,
                                        SysRegionMapper sysRegionMapper,
                                        SysRegionService sysRegionService) {
        this.properties = properties;
        this.sysRegionMapper = sysRegionMapper;
        this.sysRegionService = sysRegionService;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }

    public RegionLocateResponse locate(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException("latitude 和 longitude 不能为空");
        }
        if (StringUtils.isBlank(properties.getApiKey())) {
            throw new BusinessException("请先配置 hardrock.integration.tencent-map.api-key 或环境变量 TENCENT_MAP_API_KEY");
        }
        TencentGeocoderResponse response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/ws/geocoder/v1")
                .queryParam("location", latitude + "," + longitude)
                .queryParam("get_poi", 1)
                .queryParam("poi_options", "address_format=short;radius=500;policy=1")
                .queryParam("key", properties.getApiKey())
                .build())
            .retrieve()
            .body(TencentGeocoderResponse.class);
        if (response == null || response.getStatus() == null || response.getStatus() != 0) {
            throw new BusinessException("腾讯坐标反查行政区失败: " + (response == null ? "empty response" : response.getMessage()));
        }

        RegionLocateResponse locate = new RegionLocateResponse();
        if (response.getResult() != null) {
            locate.setAddress(resolveAddress(response.getResult()));
            applyRegion(locate, response.getResult().getAdInfo());
        }
        return locate;
    }

    private String resolveAddress(GeocoderResult result) {
        String poiAddress = null;
        String poiTitle = null;
        if (result.getPois() != null && !result.getPois().isEmpty()) {
            Poi poi = result.getPois().get(0);
            poiAddress = stripAdministrativePrefix(poi.getAddress(), result.getAdInfo());
            poiTitle = poi.getTitle();
        }
        String recommend = result.getFormattedAddresses() == null
            ? null
            : result.getFormattedAddresses().getRecommend();
        String formattedStandardAddress = result.getFormattedAddresses() == null
            ? null
            : result.getFormattedAddresses().getStandardAddress();
        return StringUtils.firstNonBlank(
            formattedStandardAddress,
            structuredAddress(result.getAddressReference(), result.getAddressComponent(), poiTitle),
            joinAddress(referenceTown(result.getAddressReference()), poiAddress, poiTitle),
            result.getStandardAddress(),
            recommend,
            result.getAddress());
    }

    private String structuredAddress(AddressReference addressReference,
                                     AddressComponent addressComponent,
                                     String poiTitle) {
        String result = null;
        if (addressReference != null) {
            result = appendAddressPart(result, referenceTitle(addressReference.getTown()));
            result = appendAddressPart(result, referenceTitle(addressReference.getLandmarkL2()));
            result = appendAddressPart(result, referenceTitle(addressReference.getStreetNumber()));
        }
        if (addressComponent != null) {
            result = appendAddressPart(result, addressComponent.getStreet());
            result = appendAddressPart(result, addressComponent.getStreetNumber());
        }
        result = appendAddressPart(result, poiTitle);
        return result;
    }

    private String referenceTown(AddressReference addressReference) {
        if (addressReference == null) {
            return null;
        }
        return referenceTitle(addressReference.getTown());
    }

    private String referenceTitle(ReferenceItem referenceItem) {
        return referenceItem == null ? null : referenceItem.getTitle();
    }

    private String joinAddress(String town, String address, String title) {
        String result = appendAddressPart(null, town);
        result = appendAddressPart(result, address);
        return appendAddressPart(result, title);
    }

    private String appendAddressPart(String base, String part) {
        String trimmedPart = StringUtils.trimToNull(part);
        if (trimmedPart == null) {
            return base;
        }
        String trimmedBase = StringUtils.trimToNull(base);
        if (trimmedBase == null) {
            return trimmedPart;
        }
        if (trimmedBase.contains(trimmedPart) || trimmedPart.contains(trimmedBase)) {
            return trimmedBase.length() >= trimmedPart.length() ? trimmedBase : trimmedPart;
        }
        return trimmedBase + trimmedPart;
    }

    private String stripAdministrativePrefix(String address, AddressInfo adInfo) {
        String trimmedAddress = StringUtils.trimToNull(address);
        if (trimmedAddress == null || adInfo == null) {
            return trimmedAddress;
        }
        String result = trimmedAddress;
        result = removePrefix(result, adInfo.getProvince());
        result = removePrefix(result, adInfo.getCity());
        result = removePrefix(result, adInfo.getDistrict());
        return result;
    }

    private String removePrefix(String value, String prefix) {
        String trimmedPrefix = StringUtils.trimToNull(prefix);
        if (trimmedPrefix == null || !value.startsWith(trimmedPrefix)) {
            return value;
        }
        return value.substring(trimmedPrefix.length()).trim();
    }

    private void applyRegion(RegionLocateResponse locate, AddressInfo adInfo) {
        if (adInfo == null || StringUtils.isBlank(adInfo.getAdcode())) {
            return;
        }
        SysRegion current = loadByCode(adInfo.getAdcode());
        if (current == null) {
            current = loadByNames(adInfo);
        }
        while (current != null) {
            if (Integer.valueOf(1).equals(current.getLevel())) {
                locate.setProvince(sysRegionService.toResponse(current));
            } else if (Integer.valueOf(2).equals(current.getLevel())) {
                locate.setCity(sysRegionService.toResponse(current));
            } else if (Integer.valueOf(3).equals(current.getLevel())) {
                locate.setDistrict(sysRegionService.toResponse(current));
            }
            current = loadByCode(current.getParentCode());
        }
    }

    private SysRegion loadByNames(AddressInfo adInfo) {
        String name = StringUtils.firstNonBlank(adInfo.getDistrict(), adInfo.getCity(), adInfo.getProvince());
        if (name == null) {
            return null;
        }
        return sysRegionMapper.selectOne(new LambdaQueryWrapper<SysRegion>()
            .eq(SysRegion::getName, name)
            .eq(SysRegion::getDeleted, 0)
            .orderByDesc(SysRegion::getLevel)
            .last("limit 1"));
    }

    private SysRegion loadByCode(String code) {
        String normalizedCode = StringUtils.trimToNull(code);
        if (normalizedCode == null || "0".equals(normalizedCode)) {
            return null;
        }
        return sysRegionMapper.selectOne(new LambdaQueryWrapper<SysRegion>()
            .eq(SysRegion::getCode, normalizedCode)
            .eq(SysRegion::getDeleted, 0)
            .last("limit 1"));
    }
}
