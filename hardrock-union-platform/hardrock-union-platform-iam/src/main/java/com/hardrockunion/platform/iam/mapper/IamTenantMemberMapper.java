package com.hardrockunion.platform.iam.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;

@Mapper
public interface IamTenantMemberMapper extends BaseMapper<IamTenantMember> {
}
