package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.PmhubParticipantCompany;
import com.hardrockunion.business.project.domain.entity.PmhubProject;
import com.hardrockunion.business.project.domain.entity.PmhubSite;
import com.hardrockunion.business.project.domain.entity.PmhubSiteWorkScope;
import com.hardrockunion.business.project.dto.PmhubSiteWorkScopeCreateRequest;
import com.hardrockunion.business.project.dto.PmhubSiteWorkScopeQueryRequest;
import com.hardrockunion.business.project.dto.PmhubSiteWorkScopeResponse;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.enums.PmhubSiteWorkScopeType;
import com.hardrockunion.business.project.mapper.PmhubSiteWorkScopeMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 标段施工范围服务。
 *
 * <p>这里承接类似 `A-001 ~ A-5000` 这样的责任范围，避免把可变范围直接塞进标段主档。
 */
@Service
public class PmhubSiteWorkScopeService {

    private final PmhubSiteWorkScopeMapper siteWorkScopeMapper;
    private final PmhubSiteService siteService;
    private final PmhubProjectLookupService projectLookupService;
    private final PmhubParticipantCompanyService participantCompanyService;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubSiteWorkScopeService(PmhubSiteWorkScopeMapper siteWorkScopeMapper,
                                    PmhubSiteService siteService,
                                    PmhubProjectLookupService projectLookupService,
                                    PmhubParticipantCompanyService participantCompanyService,
                                    PmhubAccessGuard pmhubAccessGuard) {
        this.siteWorkScopeMapper = siteWorkScopeMapper;
        this.siteService = siteService;
        this.projectLookupService = projectLookupService;
        this.participantCompanyService = participantCompanyService;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public PageResponse<PmhubSiteWorkScopeResponse> list(PmhubSiteWorkScopeQueryRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubSiteWorkScopeQueryRequest query = request == null ? new PmhubSiteWorkScopeQueryRequest() : request;
        LambdaQueryWrapper<PmhubSiteWorkScope> wrapper = new LambdaQueryWrapper<PmhubSiteWorkScope>()
            .eq(PmhubSiteWorkScope::getTenantId, loginUser.getTenantId())
            .eq(PmhubSiteWorkScope::getDeleted, 0)
            .orderByDesc(PmhubSiteWorkScope::getId);
        if (query.getProjectId() != null) {
            wrapper.eq(PmhubSiteWorkScope::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(PmhubSiteWorkScope::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(PmhubSiteWorkScope::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (StringUtils.isNotBlank(query.getScopeType())) {
            wrapper.eq(PmhubSiteWorkScope::getScopeType, StringUtils.upperCase(StringUtils.trim(query.getScopeType())));
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            List<Long> siteIds = siteService.findIdsByKeyword(keyword, loginUser.getTenantId());
            List<Long> companyIds = participantCompanyService.findIdsByKeyword(keyword, loginUser.getTenantId());
            wrapper.and(w -> {
                w.like(PmhubSiteWorkScope::getScopeName, keyword)
                    .or()
                    .like(PmhubSiteWorkScope::getScopeStartCode, keyword)
                    .or()
                    .like(PmhubSiteWorkScope::getScopeEndCode, keyword);
                if (!siteIds.isEmpty()) {
                    w.or().in(PmhubSiteWorkScope::getSiteId, siteIds);
                }
                if (!companyIds.isEmpty()) {
                    w.or().in(PmhubSiteWorkScope::getParticipantCompanyId, companyIds);
                }
            });
        }
        Page<PmhubSiteWorkScope> page = siteWorkScopeMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public PmhubSiteWorkScopeResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public PmhubSiteWorkScopeResponse create(PmhubSiteWorkScopeCreateRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getSiteId() == null) {
            throw new BusinessException("siteId 不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getScopeType())) {
            throw new BusinessException("scopeType 不能为空");
        }
        PmhubSiteWorkScopeType scopeType = PmhubSiteWorkScopeType.fromCode(request.getScopeType());
        if (scopeType == null) {
            throw new BusinessException("scopeType 不合法");
        }

        PmhubSite site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
        PmhubParticipantCompany company = participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long projectId = site.getProjectId() != null ? site.getProjectId() : request.getProjectId();
        if (projectId == null) {
            throw new BusinessException("标段尚未关联项目，无法维护施工范围");
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());

        PmhubSiteWorkScope scope = new PmhubSiteWorkScope();
        scope.setTenantId(loginUser.getTenantId());
        scope.setProjectId(projectId);
        scope.setSiteId(site.getId());
        scope.setParticipantCompanyId(company.getId());
        scope.setScopeType(scopeType.getCode());
        scope.setScopeName(StringUtils.trimToNull(request.getScopeName()));
        scope.setScopeStartCode(StringUtils.trimToNull(request.getScopeStartCode()));
        scope.setScopeEndCode(StringUtils.trimToNull(request.getScopeEndCode()));
        scope.setScopeRemark(StringUtils.trimToNull(request.getScopeRemark()));
        scope.setStatus(1);
        scope.setDeleted(0);
        scope.setCreatedBy(loginUser.getUserId());
        siteWorkScopeMapper.insert(scope);
        return getById(scope.getId(), loginUser);
    }

    public PmhubSiteWorkScope loadEntity(Long id, Long tenantId) {
        PmhubSiteWorkScope scope = siteWorkScopeMapper.selectOne(new LambdaQueryWrapper<PmhubSiteWorkScope>()
            .eq(PmhubSiteWorkScope::getId, id)
            .eq(PmhubSiteWorkScope::getTenantId, tenantId)
            .eq(PmhubSiteWorkScope::getDeleted, 0)
            .last("limit 1"));
        if (scope == null) {
            throw new BusinessException("标段施工范围不存在");
        }
        return scope;
    }

    private PmhubSiteWorkScopeResponse toResponse(PmhubSiteWorkScope item, Long tenantId) {
        PmhubSite site = siteService.loadEntity(item.getSiteId(), tenantId);
        PmhubProject project = projectLookupService.loadEntity(item.getProjectId(), tenantId);
        PmhubParticipantCompany company = participantCompanyService.loadEntity(item.getParticipantCompanyId(), tenantId);
        PmhubSiteWorkScopeResponse response = new PmhubSiteWorkScopeResponse();
        response.setId(item.getId());
        response.setTenantId(item.getTenantId());
        response.setProjectId(item.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(item.getSiteId());
        response.setSiteName(site.getSiteName());
        response.setParticipantCompanyId(item.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setScopeType(item.getScopeType());
        PmhubSiteWorkScopeType scopeType = PmhubSiteWorkScopeType.fromCode(item.getScopeType());
        response.setScopeTypeLabel(scopeType == null ? null : scopeType.getLabel());
        response.setScopeName(item.getScopeName());
        response.setScopeStartCode(item.getScopeStartCode());
        response.setScopeEndCode(item.getScopeEndCode());
        response.setScopeRemark(item.getScopeRemark());
        response.setStatus(item.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(item.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
