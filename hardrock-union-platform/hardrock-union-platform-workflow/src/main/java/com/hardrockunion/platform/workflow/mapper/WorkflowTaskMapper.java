package com.hardrockunion.platform.workflow.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hardrockunion.platform.workflow.domain.entity.WorkflowTask;

@Mapper
public interface WorkflowTaskMapper extends BaseMapper<WorkflowTask> {
}
