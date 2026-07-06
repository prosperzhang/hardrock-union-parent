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
import com.hardrockunion.business.project.domain.entity.NexisTeam;
import com.hardrockunion.business.project.dto.NexisTeamCreateRequest;
import com.hardrockunion.business.project.dto.NexisTeamQueryRequest;
import com.hardrockunion.business.project.dto.NexisTeamResponse;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.mapper.NexisTeamMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class NexisTeamService {

    private final NexisTeamMapper teamMapper;
    private final NexisProjectLookupService projectLookupService;
    private final NexisSiteService siteService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final NexisSiteWorkScopeService siteWorkScopeService;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisTeamService(NexisTeamMapper teamMapper,
                           NexisProjectLookupService projectLookupService,
                           NexisSiteService siteService,
                           NexisParticipantCompanyService participantCompanyService,
                           NexisSiteWorkScopeService siteWorkScopeService,
                           NexisAccessGuard nexisAccessGuard) {
        this.teamMapper = teamMapper;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.nexisAccessGuard = nexisAccessGuard;
    }

    public PageResponse<NexisTeamResponse> list(NexisTeamQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisTeamQueryRequest query = request == null ? new NexisTeamQueryRequest() : request;
        LambdaQueryWrapper<NexisTeam> wrapper = new LambdaQueryWrapper<NexisTeam>()
            .eq(NexisTeam::getTenantId, loginUser.getTenantId())
            .eq(NexisTeam::getDeleted, 0);
        if (query.getProjectId() != null) {
            wrapper.eq(NexisTeam::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(NexisTeam::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(NexisTeam::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (query.getWorkScopeId() != null) {
            wrapper.eq(NexisTeam::getWorkScopeId, query.getWorkScopeId());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            wrapper.and(w -> w.like(NexisTeam::getTeamName, keyword)
                .or()
                .like(NexisTeam::getTeamCode, keyword)
                .or()
                .like(NexisTeam::getLeaderName, keyword)
                .or()
                .like(NexisTeam::getLeaderPhone, keyword));
        }
        wrapper.orderByDesc(NexisTeam::getId);
        Page<NexisTeam> page = teamMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public NexisTeamResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public NexisTeamResponse create(NexisTeamCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getSiteId() == null) {
            throw new BusinessException("siteId 不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getTeamName())) {
            throw new BusinessException("teamName 不能为空");
        }

        NexisSite site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
        Long projectId = site.getProjectId() != null ? site.getProjectId() : request.getProjectId();
        if (projectId == null) {
            throw new BusinessException("标段尚未关联项目，无法创建班组");
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());
        participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long workScopeId = normalizeWorkScopeId(request.getWorkScopeId(), loginUser.getTenantId(), site.getId(), request.getParticipantCompanyId());

        NexisTeam team = new NexisTeam();
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

    public NexisTeam loadEntity(Long id, Long tenantId) {
        NexisTeam team = teamMapper.selectOne(new LambdaQueryWrapper<NexisTeam>()
            .eq(NexisTeam::getId, id)
            .eq(NexisTeam::getTenantId, tenantId)
            .eq(NexisTeam::getDeleted, 0)
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
        return teamMapper.selectList(new LambdaQueryWrapper<NexisTeam>()
                .select(NexisTeam::getId)
                .eq(NexisTeam::getTenantId, tenantId)
                .eq(NexisTeam::getDeleted, 0)
                .and(wrapper -> wrapper.like(NexisTeam::getTeamName, trimmedKeyword)
                    .or()
                    .like(NexisTeam::getTeamCode, trimmedKeyword)
                    .or()
                    .like(NexisTeam::getLeaderName, trimmedKeyword)
                    .or()
                    .like(NexisTeam::getLeaderPhone, trimmedKeyword)))
            .stream()
            .map(NexisTeam::getId)
            .toList();
    }

    private Long normalizeWorkScopeId(Long workScopeId, Long tenantId, Long siteId, Long participantCompanyId) {
        if (workScopeId == null) {
            return null;
        }
        NexisSiteWorkScope workScope = siteWorkScopeService.loadEntity(workScopeId, tenantId);
        if (!siteId.equals(workScope.getSiteId())) {
            throw new BusinessException("施工范围不属于当前标段");
        }
        if (!participantCompanyId.equals(workScope.getParticipantCompanyId())) {
            throw new BusinessException("施工范围不属于当前参建单位");
        }
        return workScopeId;
    }

    private NexisTeamResponse toResponse(NexisTeam team, Long tenantId) {
        NexisProject project = projectLookupService.loadEntity(team.getProjectId(), tenantId);
        NexisSite site = siteService.loadEntity(team.getSiteId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(team.getParticipantCompanyId(), tenantId);
        NexisTeamResponse response = new NexisTeamResponse();
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
            NexisSiteWorkScope workScope = siteWorkScopeService.loadEntity(team.getWorkScopeId(), tenantId);
            response.setWorkScopeName(StringUtils.defaultIfBlank(workScope.getScopeName(), workScope.getScopeStartCode()));
        }
        response.setTeamName(team.getTeamName());
        response.setTeamCode(team.getTeamCode());
        response.setLeaderName(team.getLeaderName());
        response.setLeaderPhone(team.getLeaderPhone());
        response.setStatus(team.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(team.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
