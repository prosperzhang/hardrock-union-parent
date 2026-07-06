package com.hardrockunion.business.warehouse.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.business.warehouse.domain.entity.WarehouseStock;

@Mapper
public interface WarehouseStockMapper extends BaseMapper<WarehouseStock> {
}
