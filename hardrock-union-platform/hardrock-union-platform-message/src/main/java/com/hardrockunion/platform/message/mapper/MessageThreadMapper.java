package com.hardrockunion.platform.message.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.platform.message.domain.entity.MessageThread;

@Mapper
public interface MessageThreadMapper extends BaseMapper<MessageThread> {
}
