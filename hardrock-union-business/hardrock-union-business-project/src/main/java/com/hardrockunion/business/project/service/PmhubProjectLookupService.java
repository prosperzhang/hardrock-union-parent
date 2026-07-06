package com.hardrockunion.business.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hardrockunion.business.project.domain.entity.PmhubProject;
import com.hardrockunion.platform.tenant.dto.TenantSummaryResponse;
import com.hardrockunion.platform.tenant.service.TenantWorkspaceFlowService;

@Service
public class PmhubProjectLookupService {

    private static final String APP_CODE = "PMHUB";

    private final TenantWorkspaceFlowService tenantWorkspaceFlowService;

    public PmhubProjectLookupService(TenantWorkspaceFlowService tenantWorkspaceFlowService) {
        this.tenantWorkspaceFlowService = tenantWorkspaceFlowService;
    }

    public PmhubProject loadEntity(Long projectId, Long tenantId) {
        return toProject(tenantWorkspaceFlowService.loadTenantRegistry(APP_CODE, projectId, tenantId));
    }

    public List<Long> findIdsByKeyword(String keyword, Long tenantId) {
        return tenantWorkspaceFlowService.findTenantIdsByKeyword(APP_CODE, keyword, tenantId);
    }

    public PmhubProject loadByKeyword(String keyword, Long tenantId) {
        return toProject(tenantWorkspaceFlowService.loadTenantByKeyword(APP_CODE, keyword, tenantId));
    }

    private PmhubProject toProject(TenantSummaryResponse project) {
        PmhubProject entity = new PmhubProject();
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
