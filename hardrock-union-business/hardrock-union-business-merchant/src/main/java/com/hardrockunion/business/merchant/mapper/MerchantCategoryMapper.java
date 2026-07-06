package com.hardrockunion.business.merchant.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.business.merchant.domain.entity.MerchantCategory;

@Mapper
public interface MerchantCategoryMapper extends BaseMapper<MerchantCategory> {
}
