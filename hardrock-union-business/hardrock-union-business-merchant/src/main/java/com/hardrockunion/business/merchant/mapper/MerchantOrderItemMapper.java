package com.hardrockunion.business.merchant.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.business.merchant.domain.entity.MerchantOrderItem;

@Mapper
public interface MerchantOrderItemMapper extends BaseMapper<MerchantOrderItem> {
}
