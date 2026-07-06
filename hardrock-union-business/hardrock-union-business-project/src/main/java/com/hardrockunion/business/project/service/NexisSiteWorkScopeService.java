package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.NexisParticipantCompany;
import com.hardrockunion.business.project.domain.entity.NexisProject;
import com.hardrockunion.business.project.domain.entity.NexisSite;
import com.hardrockunion.business.project.domain.entity.NexisSiteWorkScope;
import com.hardrockunion.business.project.dto.NexisSiteWorkScopeCreateRequest;
import com.hardrockunion.business.project.dto.NexisSiteWorkScopeQueryRequest;
import com.hardrockunion.business.project.dto.NexisSiteWorkScopeResponse;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.enums.NexisSiteWorkScopeType;
import com.hardrockunion.business.project.mapper.NexisSiteWorkScopeMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 标段施工范围服务。
 *
 * <p>这里承接类似 `A-001 ~ A-5000` 这样的责任范围，避免把可变范围直接塞进标段主档。
 */
@Service
public class NexisSiteWorkScopeService {

    private final NexisSiteWorkScopeMapper siteWorkScopeMapper;
    private final NexisSiteService siteService;
    private final NexisProjectLookupService projectLookupService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisSiteWorkScopeService(NexisSiteWorkScopeMapper siteWorkScopeMapper,
                                    NexisSiteService siteService,
                                    NexisProjectLookupService projectLookupService,
                                    NexisParticipantCompanyService participantCompanyService,
                                    NexisAccessGuard nexisAccessGuard) {
        this.siteWorkScopeMapper = siteWorkScopeMapper;
        this.siteService = siteService;
        this.projectLookupService = projectLookupService;
        this.participantCompanyService = participantCompanyService;
        this.nexisAccessGuard = nexisAccessGuard;
    }

    public PageResponse<NexisSiteWorkScopeResponse> list(NexisSiteWorkScopeQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisSiteWorkScopeQueryRequest query = request == null ? new NexisSiteWorkScopeQueryRequest() : request;
        LambdaQueryWrapper<NexisSiteWorkScope> wrapper = new LambdaQueryWrapper<NexisSiteWorkScope>()
            .eq(NexisSiteWorkScope::getTenantId, loginUser.getTenantId())
            .eq(NexisSiteWorkScope::getDeleted, 0)
            .orderByDesc(NexisSiteWorkScope::getId);
        if (query.getProjectId() != null) {
            wrapper.eq(NexisSiteWorkScope::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(NexisSiteWorkScope::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(NexisSiteWorkScope::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (StringUtils.isNotBlank(query.getScopeType())) {
            wrapper.eq(NexisSiteWorkScope::getScopeType, StringUtils.upperCase(StringUtils.trim(query.getScopeType())));
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            List<Long> siteIds = siteService.findIdsByKeyword(keyword, loginUser.getTenantId());
            List<Long> companyIds = participantCompanyService.findIdsByKeyword(keyword, loginUser.getTenantId());
            wrapper.and(w -> {
                w.like(NexisSiteWorkScope::getScopeName, keyword)
                    .or()
                    .like(NexisSiteWorkScope::getScopeStartCode, keyword)
                    .or()
                    .like(NexisSiteWorkScope::getScopeEndCode, keyword);
                if (!siteIds.isEmpty()) {
                    w.or().in(NexisSiteWorkScope::getSiteId, siteIds);
                }
                if (!companyIds.isEmpty()) {
                    w.or().in(NexisSiteWorkScope::getParticipantCompanyId, companyIds);
                }
            });
        }
        Page<NexisSiteWorkScope> page = siteWorkScopeMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public NexisSiteWorkScopeResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public NexisSiteWorkScopeResponse create(NexisSiteWorkScopeCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getSiteId() == null) {
            throw new BusinessException("siteId 不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getScopeType())) {
            throw new BusinessException("scopeType 不能为空");
        }
        NexisSiteWorkScopeType scopeType = NexisSiteWorkScopeType.fromCode(request.getScopeType());
        if (scopeType == null) {
            throw new BusinessException("scopeType 不合法");
        }

        NexisSite site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
        NexisParticipantCompany company = participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long projectId = site.getProjectId() != null ? site.getProjectId() : request.getProjectId();
        if (projectId == null) {
            throw new BusinessException("标段尚未关联项目，无法维护施工范围");
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());

        NexisSiteWorkScope scope = new NexisSiteWorkScope();
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

    public NexisSiteWorkScope loadEntity(Long id, Long tenantId) {
        NexisSiteWorkScope scope = siteWorkScopeMapper.selectOne(new LambdaQueryWrapper<NexisSiteWorkScope>()
            .eq(NexisSiteWorkScope::getId, id)
            .eq(NexisSiteWorkScope::getTenantId, tenantId)
            .eq(NexisSiteWorkScope::getDeleted, 0)
            .last("limit 1"));
        if (scope == null) {
            throw new BusinessException("标段施工范围不存在");
        }
        return scope;
    }

    private NexisSiteWorkScopeResponse toResponse(NexisSiteWorkScope item, Long tenantId) {
        NexisSite site = siteService.loadEntity(item.getSiteId(), tenantId);
        NexisProject project = projectLookupService.loadEntity(item.getProjectId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(item.getParticipantCompanyId(), tenantId);
        NexisSiteWorkScopeResponse response = new NexisSiteWorkScopeResponse();
        response.setId(item.getId());
        response.setTenantId(item.getTenantId());
        response.setProjectId(item.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(item.getSiteId());
        response.setSiteName(site.getSiteName());
        response.setParticipantCompanyId(item.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setScopeType(item.getScopeType());
        NexisSiteWorkScopeType scopeType = NexisSiteWorkScopeType.fromCode(item.getScopeType());
        response.setScopeTypeLabel(scopeType == null ? null : scopeType.getLabel());
        response.setScopeName(item.getScopeName());
        response.setScopeStartCode(item.getScopeStartCode());
        response.setScopeEndCode(item.getScopeEndCode());
        response.setScopeRemark(item.getScopeRemark());
        response.setStatus(item.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(item.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
