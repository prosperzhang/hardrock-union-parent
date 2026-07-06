package com.hardrockunion.business.warehouse.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.business.warehouse.domain.entity.Warehouse;

@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
}
