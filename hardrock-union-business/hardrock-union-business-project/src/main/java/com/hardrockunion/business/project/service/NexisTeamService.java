package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
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
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.iam.service.IamRoleQueryService;
import com.hardrockunion.platform.iam.service.IamTenantMemberService;

import jakarta.annotation.PostConstruct;

@Service
public class NexisTeamService {

    private final NexisTeamMapper teamMapper;
    private final NexisProjectLookupService projectLookupService;
    private final NexisSiteService siteService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final NexisProjectParticipantService projectParticipantService;
    private final NexisSiteWorkScopeService siteWorkScopeService;
    private final NexisAccessGuard nexisAccessGuard;
    private final IamTenantMemberService tenantMemberService;
    private final IamRoleQueryService roleQueryService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final JdbcTemplate jdbcTemplate;

    public NexisTeamService(NexisTeamMapper teamMapper,
                           NexisProjectLookupService projectLookupService,
                           NexisSiteService siteService,
                           NexisParticipantCompanyService participantCompanyService,
                           NexisProjectParticipantService projectParticipantService,
                           NexisSiteWorkScopeService siteWorkScopeService,
                           NexisAccessGuard nexisAccessGuard,
                           IamTenantMemberService tenantMemberService,
                           IamRoleQueryService roleQueryService,
                           AppRegistryQueryService appRegistryQueryService,
                           JdbcTemplate jdbcTemplate) {
        this.teamMapper = teamMapper;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.projectParticipantService = projectParticipantService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.nexisAccessGuard = nexisAccessGuard;
        this.tenantMemberService = tenantMemberService;
        this.roleQueryService = roleQueryService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureLeaderUserColumn() {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'project_team' AND COLUMN_NAME = 'leader_user_id'
            """, Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE project_team ADD COLUMN leader_user_id BIGINT NULL COMMENT '班组长 Nexis 用户ID' AFTER leader_phone");
            jdbcTemplate.execute("CREATE INDEX idx_project_team_leader_user ON project_team (tenant_id, leader_user_id)");
        }
    }

    public PageResponse<NexisTeamResponse> list(NexisTeamQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisTeamQueryRequest query = request == null ? new NexisTeamQueryRequest() : request;
        LambdaQueryWrapper<NexisTeam> wrapper = new LambdaQueryWrapper<NexisTeam>()
            .eq(NexisTeam::getTenantId, loginUser.getTenantId())
            .eq(NexisTeam::getDeleted, 0);
        if (nexisAccessGuard.hasRole(loginUser, "NEXIS_TEAM_LEADER")) {
            wrapper.eq(NexisTeam::getLeaderUserId, loginUser.getUserId());
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
        NexisTeam team = loadEntity(id, loginUser.getTenantId());
        ensureCanAccessTeam(team, loginUser);
        return toResponse(team, loginUser.getTenantId());
    }

    public NexisTeamResponse create(NexisTeamCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.TEAM_MANAGE);
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getTeamName())) {
            throw new BusinessException("teamName 不能为空");
        }

        Long projectId = loginUser.getTenantId();
        NexisSite site = null;
        if (request.getSiteId() != null) {
            site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
            if (site.getProjectId() != null && !projectId.equals(site.getProjectId())) {
                throw new BusinessException("标段不属于当前项目");
            }
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());
        participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        projectParticipantService.ensureActiveParticipantCompany(request.getParticipantCompanyId(), loginUser.getTenantId());
        String teamName = StringUtils.trim(request.getTeamName());
        Long duplicateCount = teamMapper.selectCount(new LambdaQueryWrapper<NexisTeam>()
            .eq(NexisTeam::getTenantId, loginUser.getTenantId())
            .eq(NexisTeam::getParticipantCompanyId, request.getParticipantCompanyId())
            .eq(NexisTeam::getTeamName, teamName)
            .eq(NexisTeam::getDeleted, 0));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException("当前参建单位已存在同名班组");
        }
        validateLeaderUser(request.getLeaderUserId(), loginUser.getTenantId());
        NexisSiteWorkScope workScope = normalizeWorkScope(
            request.getWorkScopeId(),
            loginUser.getTenantId(),
            projectId,
            site == null ? null : site.getId(),
            request.getParticipantCompanyId());
        if (site == null && workScope != null) {
            site = siteService.loadEntity(workScope.getSiteId(), loginUser.getTenantId());
        }

        NexisTeam team = new NexisTeam();
        team.setTenantId(loginUser.getTenantId());
        team.setSiteId(site == null ? null : site.getId());
        team.setParticipantCompanyId(request.getParticipantCompanyId());
        team.setWorkScopeId(workScope == null ? null : workScope.getId());
        team.setTeamName(teamName);
        team.setTeamCode(StringUtils.trimToNull(request.getTeamCode()));
        team.setLeaderName(StringUtils.trimToNull(request.getLeaderName()));
        team.setLeaderPhone(StringUtils.trimToNull(request.getLeaderPhone()));
        team.setLeaderUserId(request.getLeaderUserId());
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

    public List<Long> findIdsByLeader(Long tenantId, Long leaderUserId) {
        return teamMapper.selectList(new LambdaQueryWrapper<NexisTeam>()
                .select(NexisTeam::getId)
                .eq(NexisTeam::getTenantId, tenantId)
                .eq(NexisTeam::getLeaderUserId, leaderUserId)
                .eq(NexisTeam::getDeleted, 0)
                .eq(NexisTeam::getStatus, 1))
            .stream().map(NexisTeam::getId).toList();
    }

    public void ensureCanAccessTeam(NexisTeam team, LoginUser loginUser) {
        if (nexisAccessGuard.hasRole(loginUser, "NEXIS_TEAM_LEADER")
            && !loginUser.getUserId().equals(team.getLeaderUserId())) {
            throw new BusinessException("班组长只能访问自己负责的班组");
        }
    }

    private void validateLeaderUser(Long leaderUserId, Long tenantId) {
        if (leaderUserId == null) {
            return;
        }
        Long appId = appRegistryQueryService.getEnabledAppByCode("NEXIS").getId();
        if (tenantMemberService.getActiveMember(appId, tenantId, leaderUserId) == null) {
            throw new BusinessException("班组长必须是当前项目成员");
        }
        boolean teamLeader = roleQueryService.listRoleEntitiesByUser(leaderUserId, "NEXIS", tenantId)
            .stream().anyMatch(role -> StringUtils.equalsIgnoreCase(role.getRoleCode(), "NEXIS_TEAM_LEADER"));
        if (!teamLeader) {
            throw new BusinessException("所选成员没有班组长角色");
        }
    }

    private NexisSiteWorkScope normalizeWorkScope(Long workScopeId, Long tenantId, Long projectId, Long siteId, Long participantCompanyId) {
        if (workScopeId == null) {
            return null;
        }
        NexisSiteWorkScope workScope = siteWorkScopeService.loadEntity(workScopeId, tenantId);
        if (!projectId.equals(workScope.getProjectId())) {
            throw new BusinessException("施工范围不属于当前项目");
        }
        if (siteId != null && !siteId.equals(workScope.getSiteId())) {
            throw new BusinessException("施工范围不属于当前标段");
        }
        if (!participantCompanyId.equals(workScope.getParticipantCompanyId())) {
            throw new BusinessException("施工范围不属于当前参建单位");
        }
        return workScope;
    }

    private NexisTeamResponse toResponse(NexisTeam team, Long tenantId) {
        NexisProject project = projectLookupService.loadEntity(team.getTenantId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(team.getParticipantCompanyId(), tenantId);
        NexisTeamResponse response = new NexisTeamResponse();
        response.setId(team.getId());
        response.setTenantId(team.getTenantId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(team.getSiteId());
        if (team.getSiteId() != null) {
            NexisSite site = siteService.loadEntity(team.getSiteId(), tenantId);
            response.setSiteName(site.getSiteName());
        }
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
        response.setLeaderUserId(team.getLeaderUserId());
        response.setStatus(team.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(team.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
