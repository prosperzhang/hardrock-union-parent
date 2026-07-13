package com.hardrockunion.business.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hardrockunion.business.project.domain.entity.NexisProject;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.tenant.dto.TenantSummaryResponse;
import com.hardrockunion.platform.tenant.service.TenantWorkspaceFlowService;

@Service
public class NexisProjectLookupService {

    private static final String APP_CODE = "NEXIS";
    private static final String PROJECT_TENANT_TYPE = "PROJECT";

    private final TenantWorkspaceFlowService tenantWorkspaceFlowService;

    public NexisProjectLookupService(TenantWorkspaceFlowService tenantWorkspaceFlowService) {
        this.tenantWorkspaceFlowService = tenantWorkspaceFlowService;
    }

    public NexisProject loadEntity(Long projectId, Long tenantId) {
        return toProject(tenantWorkspaceFlowService.loadTenantRegistry(APP_CODE, projectId, tenantId));
    }

    public List<Long> findIdsByKeyword(String keyword, Long tenantId) {
        return tenantWorkspaceFlowService.findTenantIdsByKeyword(APP_CODE, keyword, tenantId);
    }

    public NexisProject loadByKeyword(String keyword, Long tenantId) {
        return toProject(tenantWorkspaceFlowService.loadTenantByKeyword(APP_CODE, keyword, tenantId));
    }

    private NexisProject toProject(TenantSummaryResponse project) {
        if (project == null || !PROJECT_TENANT_TYPE.equalsIgnoreCase(project.getTenantType())) {
            throw new BusinessException("项目不存在或当前空间不是项目");
        }
        NexisProject entity = new NexisProject();
        entity.setId(project.getTenantId());
        entity.setTenantId(project.getTenantId());
        entity.setProjectName(project.getTenantName());
        entity.setProjectCode(project.getTenantCode());
        entity.setProjectAddress(project.getProjectAddress());
        entity.setManagerName(project.getManagerName());
        entity.setManagerPhone(project.getManagerPhone());
        entity.setStatus(project.getStatus());
        return entity;
    }
}
