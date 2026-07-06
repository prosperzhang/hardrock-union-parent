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
import com.hardrockunion.business.project.domain.entity.PmhubTeam;
import com.hardrockunion.business.project.dto.PmhubTeamCreateRequest;
import com.hardrockunion.business.project.dto.PmhubTeamQueryRequest;
import com.hardrockunion.business.project.dto.PmhubTeamResponse;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.mapper.PmhubTeamMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class PmhubTeamService {

    private final PmhubTeamMapper teamMapper;
    private final PmhubProjectLookupService projectLookupService;
    private final PmhubSiteService siteService;
    private final PmhubParticipantCompanyService participantCompanyService;
    private final PmhubSiteWorkScopeService siteWorkScopeService;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubTeamService(PmhubTeamMapper teamMapper,
                           PmhubProjectLookupService projectLookupService,
                           PmhubSiteService siteService,
                           PmhubParticipantCompanyService participantCompanyService,
                           PmhubSiteWorkScopeService siteWorkScopeService,
                           PmhubAccessGuard pmhubAccessGuard) {
        this.teamMapper = teamMapper;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public PageResponse<PmhubTeamResponse> list(PmhubTeamQueryRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubTeamQueryRequest query = request == null ? new PmhubTeamQueryRequest() : request;
        LambdaQueryWrapper<PmhubTeam> wrapper = new LambdaQueryWrapper<PmhubTeam>()
            .eq(PmhubTeam::getTenantId, loginUser.getTenantId())
            .eq(PmhubTeam::getDeleted, 0);
        if (query.getProjectId() != null) {
            wrapper.eq(PmhubTeam::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(PmhubTeam::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(PmhubTeam::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (query.getWorkScopeId() != null) {
            wrapper.eq(PmhubTeam::getWorkScopeId, query.getWorkScopeId());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            wrapper.and(w -> w.like(PmhubTeam::getTeamName, keyword)
                .or()
                .like(PmhubTeam::getTeamCode, keyword)
                .or()
                .like(PmhubTeam::getLeaderName, keyword)
                .or()
                .like(PmhubTeam::getLeaderPhone, keyword));
        }
        wrapper.orderByDesc(PmhubTeam::getId);
        Page<PmhubTeam> page = teamMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public PmhubTeamResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public PmhubTeamResponse create(PmhubTeamCreateRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getSiteId() == null) {
            throw new BusinessException("siteId 不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getTeamName())) {
            throw new BusinessException("teamName 不能为空");
        }

        PmhubSite site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
        Long projectId = site.getProjectId() != null ? site.getProjectId() : request.getProjectId();
        if (projectId == null) {
            throw new BusinessException("标段尚未关联项目，无法创建班组");
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());
        participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long workScopeId = normalizeWorkScopeId(request.getWorkScopeId(), loginUser.getTenantId(), site.getId(), request.getParticipantCompanyId());

        PmhubTeam team = new PmhubTeam();
        team.setTenantId(loginUser.getTenantId());
        team.setProjectId(projectId);
        team.setSiteId(site.getId());
        team.setParticipantCompanyId(request.getParticipantCompanyId());
        team.setWorkScopeId(workScopeId);
        team.setTeamName(StringUtils.trim(request.getTeamName()));
        team.setTeamCode(StringUtils.trimToNull(request.getTeamCode()));
        team.setLeaderName(StringUtils.trimToNull(request.getLeaderName()));
        team.setLeaderPhone(StringUtils.trimToNull(request.getLeaderPhone()));
        team.setStatus(1);
        team.setDeleted(0);
        team.setCreatedBy(loginUser.getUserId());
        teamMapper.insert(team);
        return getById(team.getId(), loginUser);
    }

    public PmhubTeam loadEntity(Long id, Long tenantId) {
        PmhubTeam team = teamMapper.selectOne(new LambdaQueryWrapper<PmhubTeam>()
            .eq(PmhubTeam::getId, id)
            .eq(PmhubTeam::getTenantId, tenantId)
            .eq(PmhubTeam::getDeleted, 0)
            .last("limit 1"));
        if (team == null) {
            throw new BusinessException("班组不存在");
        }
        return team;
    }

    public List<Long> findIdsByKeyword(String keyword, Long tenantId) {
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }
        String trimmedKeyword = StringUtils.trim(keyword);
        return teamMapper.selectList(new LambdaQueryWrapper<PmhubTeam>()
                .select(PmhubTeam::getId)
                .eq(PmhubTeam::getTenantId, tenantId)
                .eq(PmhubTeam::getDeleted, 0)
                .and(wrapper -> wrapper.like(PmhubTeam::getTeamName, trimmedKeyword)
                    .or()
                    .like(PmhubTeam::getTeamCode, trimmedKeyword)
                    .or()
                    .like(PmhubTeam::getLeaderName, trimmedKeyword)
                    .or()
                    .like(PmhubTeam::getLeaderPhone, trimmedKeyword)))
            .stream()
            .map(PmhubTeam::getId)
            .toList();
    }

    private Long normalizeWorkScopeId(Long workScopeId, Long tenantId, Long siteId, Long participantCompanyId) {
        if (workScopeId == null) {
            return null;
        }
        PmhubSiteWorkScope workScope = siteWorkScopeService.loadEntity(workScopeId, tenantId);
        if (!siteId.equals(workScope.getSiteId())) {
            throw new BusinessException("施工范围不属于当前标段");
        }
        if (!participantCompanyId.equals(workScope.getParticipantCompanyId())) {
            throw new BusinessException("施工范围不属于当前参建单位");
        }
        return workScopeId;
    }

    private PmhubTeamResponse toResponse(PmhubTeam team, Long tenantId) {
        PmhubProject project = projectLookupService.loadEntity(team.getProjectId(), tenantId);
        PmhubSite site = siteService.loadEntity(team.getSiteId(), tenantId);
        PmhubParticipantCompany company = participantCompanyService.loadEntity(team.getParticipantCompanyId(), tenantId);
        PmhubTeamResponse response = new PmhubTeamResponse();
        response.setId(team.getId());
        response.setTenantId(team.getTenantId());
        response.setProjectId(team.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(team.getSiteId());
        response.setSiteName(site.getSiteName());
        response.setParticipantCompanyId(team.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setWorkScopeId(team.getWorkScopeId());
        if (team.getWorkScopeId() != null) {
            PmhubSiteWorkScope workScope = siteWorkScopeService.loadEntity(team.getWorkScopeId(), tenantId);
            response.setWorkScopeName(StringUtils.defaultIfBlank(workScope.getScopeName(), workScope.getScopeStartCode()));
        }
        response.setTeamName(team.getTeamName());
        response.setTeamCode(team.getTeamCode());
        response.setLeaderName(team.getLeaderName());
        response.setLeaderPhone(team.getLeaderPhone());
        response.setStatus(team.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(team.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
