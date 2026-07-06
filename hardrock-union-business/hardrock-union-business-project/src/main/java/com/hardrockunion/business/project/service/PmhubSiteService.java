package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.project.domain.entity.PmhubProject;
import com.hardrockunion.business.project.domain.entity.PmhubSite;
import com.hardrockunion.business.project.dto.PmhubSiteCreateRequest;
import com.hardrockunion.business.project.dto.PmhubSiteResponse;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.mapper.PmhubSiteMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;

@Service
public class PmhubSiteService {

    private final PmhubSiteMapper siteMapper;
    private final PmhubProjectLookupService projectLookupService;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubSiteService(PmhubSiteMapper siteMapper,
                           PmhubProjectLookupService projectLookupService,
                           PmhubAccessGuard pmhubAccessGuard) {
        this.siteMapper = siteMapper;
        this.projectLookupService = projectLookupService;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public List<PmhubSiteResponse> list(LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return siteMapper.selectList(new LambdaQueryWrapper<PmhubSite>()
                .eq(PmhubSite::getTenantId, loginUser.getTenantId())
                .eq(PmhubSite::getDeleted, 0)
                .orderByDesc(PmhubSite::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public PmhubSiteResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()));
    }

    public PmhubSiteResponse create(PmhubSiteCreateRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getSiteName())) {
            throw new BusinessException("siteName 不能为空");
        }

        Long projectId = request.getProjectId() == null ? loginUser.getTenantId() : request.getProjectId();
        PmhubProject project = projectId == null ? null : projectLookupService.loadEntity(projectId, loginUser.getTenantId());

        PmhubSite site = new PmhubSite();
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
    public PmhubSite loadEntity(Long id, Long tenantId) {
        PmhubSite site = siteMapper.selectOne(new LambdaQueryWrapper<PmhubSite>()
            .eq(PmhubSite::getId, id)
            .eq(PmhubSite::getTenantId, tenantId)
            .eq(PmhubSite::getDeleted, 0)
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
        LambdaQueryWrapper<PmhubSite> queryWrapper = new LambdaQueryWrapper<PmhubSite>()
            .select(PmhubSite::getId)
            .eq(PmhubSite::getTenantId, tenantId)
            .eq(PmhubSite::getDeleted, 0)
            .and(wrapper -> {
                wrapper.like(PmhubSite::getSiteName, trimmedKeyword)
                    .or()
                    .like(PmhubSite::getSiteAddress, trimmedKeyword)
                    .or()
                    .like(PmhubSite::getManagerName, trimmedKeyword)
                    .or()
                    .like(PmhubSite::getManagerPhone, trimmedKeyword);
                if (!projectIds.isEmpty()) {
                    wrapper.or().in(PmhubSite::getProjectId, projectIds);
                }
            });
        return siteMapper.selectList(queryWrapper)
            .stream()
            .map(PmhubSite::getId)
            .toList();
    }

    private PmhubSiteResponse toResponse(PmhubSite site) {
        applyProjectIdentity(site);
        PmhubSiteResponse response = new PmhubSiteResponse();
        response.setId(site.getId());
        response.setTenantId(site.getTenantId());
        response.setProjectId(site.getProjectId());
        response.setSiteName(site.getSiteName());
        response.setProjectName(site.getProjectName());
        response.setSiteAddress(site.getSiteAddress());
        response.setManagerName(site.getManagerName());
        response.setManagerPhone(site.getManagerPhone());
        response.setStatus(site.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(site.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }

    private void applyProjectIdentity(PmhubSite site) {
        if (site == null) {
            return;
        }
        Long projectId = site.getProjectId() == null ? site.getTenantId() : site.getProjectId();
        if (projectId == null) {
            site.setProjectName(null);
            return;
        }
        try {
            PmhubProject project = projectLookupService.loadEntity(projectId, site.getTenantId());
            site.setProjectId(project.getId());
            site.setProjectName(project.getProjectName());
        } catch (BusinessException ex) {
            site.setProjectName(null);
        }
    }
}
