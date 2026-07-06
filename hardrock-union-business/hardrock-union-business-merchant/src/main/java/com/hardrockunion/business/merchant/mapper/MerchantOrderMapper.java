package com.hardrockunion.business.merchant.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.business.merchant.domain.entity.MerchantOrder;

@Mapper
public interface MerchantOrderMapper extends BaseMapper<MerchantOrder> {
}
