package com.hardrockunion.platform.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.platform.tenant.domain.entity.TenantRegistry;

@Mapper
public interface TenantRegistryMapper extends BaseMapper<TenantRegistry> {
}
