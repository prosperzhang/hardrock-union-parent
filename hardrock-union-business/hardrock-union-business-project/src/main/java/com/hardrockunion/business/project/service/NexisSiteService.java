package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.project.domain.entity.NexisProject;
import com.hardrockunion.business.project.domain.entity.NexisSite;
import com.hardrockunion.business.project.dto.NexisSiteCreateRequest;
import com.hardrockunion.business.project.dto.NexisSiteResponse;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.mapper.NexisSiteMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;

@Service
public class NexisSiteService {

    private final NexisSiteMapper siteMapper;
    private final NexisProjectLookupService projectLookupService;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisSiteService(NexisSiteMapper siteMapper,
                           NexisProjectLookupService projectLookupService,
                           NexisAccessGuard nexisAccessGuard) {
        this.siteMapper = siteMapper;
        this.projectLookupService = projectLookupService;
        this.nexisAccessGuard = nexisAccessGuard;
    }

    public List<NexisSiteResponse> list(LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        return siteMapper.selectList(new LambdaQueryWrapper<NexisSite>()
                .eq(NexisSite::getTenantId, loginUser.getTenantId())
                .eq(NexisSite::getDeleted, 0)
                .orderByDesc(NexisSite::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public NexisSiteResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()));
    }

    public NexisSiteResponse create(NexisSiteCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.PROJECT_MANAGE);
        if (request == null || StringUtils.isBlank(request.getSiteName())) {
            throw new BusinessException("siteName 不能为空");
        }

        Long projectId = request.getProjectId() == null ? loginUser.getTenantId() : request.getProjectId();
        NexisProject project = projectId == null ? null : projectLookupService.loadEntity(projectId, loginUser.getTenantId());

        NexisSite site = new NexisSite();
        site.setTenantId(loginUser.getTenantId());
        site.setProjectId(project == null ? null : project.getId());
        site.setSiteName(request.getSiteName());
        site.setSiteAddress(request.getSiteAddress());
        site.setManagerName(request.getManagerName());
        site.setManagerPhone(request.getManagerPhone());
        site.setStatus(1);
        site.setDeleted(0);
        site.setCreatedBy(loginUser.getUserId());
        siteMapper.insert(site);
        return getById(site.getId(), loginUser);
    }

    /**
     * 加载当前租户下的单个标段/工地，给参建关系和施工范围等下游流程复用。
     */
    public NexisSite loadEntity(Long id, Long tenantId) {
        NexisSite site = siteMapper.selectOne(new LambdaQueryWrapper<NexisSite>()
            .eq(NexisSite::getId, id)
            .eq(NexisSite::getTenantId, tenantId)
            .eq(NexisSite::getDeleted, 0)
            .last("limit 1"));
        if (site == null) {
            throw new BusinessException("工地不存在");
        }
        return site;
    }

    public List<Long> findIdsByKeyword(String keyword, Long tenantId) {
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }
        String trimmedKeyword = StringUtils.trim(keyword);
        List<Long> projectIds = projectLookupService.findIdsByKeyword(trimmedKeyword, tenantId);
        LambdaQueryWrapper<NexisSite> queryWrapper = new LambdaQueryWrapper<NexisSite>()
            .select(NexisSite::getId)
            .eq(NexisSite::getTenantId, tenantId)
            .eq(NexisSite::getDeleted, 0)
            .and(wrapper -> {
                wrapper.like(NexisSite::getSiteName, trimmedKeyword)
                    .or()
                    .like(NexisSite::getSiteAddress, trimmedKeyword)
                    .or()
                    .like(NexisSite::getManagerName, trimmedKeyword)
                    .or()
                    .like(NexisSite::getManagerPhone, trimmedKeyword);
                if (!projectIds.isEmpty()) {
                    wrapper.or().in(NexisSite::getProjectId, projectIds);
                }
            });
        return siteMapper.selectList(queryWrapper)
            .stream()
            .map(NexisSite::getId)
            .toList();
    }

    private NexisSiteResponse toResponse(NexisSite site) {
        applyProjectIdentity(site);
        NexisSiteResponse response = new NexisSiteResponse();
        response.setId(site.getId());
        response.setTenantId(site.getTenantId());
        response.setProjectId(site.getProjectId());
        response.setSiteName(site.getSiteName());
        response.setProjectName(site.getProjectName());
        response.setSiteAddress(site.getSiteAddress());
        response.setManagerName(site.getManagerName());
        response.setManagerPhone(site.getManagerPhone());
        response.setStatus(site.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(site.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }

    private void applyProjectIdentity(NexisSite site) {
        if (site == null) {
            return;
        }
        Long projectId = site.getProjectId() == null ? site.getTenantId() : site.getProjectId();
        if (projectId == null) {
            site.setProjectName(null);
            return;
        }
        try {
            NexisProject project = projectLookupService.loadEntity(projectId, site.getTenantId());
            site.setProjectId(project.getId());
            site.setProjectName(project.getProjectName());
        } catch (BusinessException ex) {
            site.setProjectName(null);
        }
    }
}
