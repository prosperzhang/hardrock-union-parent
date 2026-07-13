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
import com.hardrockunion.business.project.domain.entity.NexisWorker;
import com.hardrockunion.business.project.dto.NexisWorkerCreateRequest;
import com.hardrockunion.business.project.dto.NexisWorkerQueryRequest;
import com.hardrockunion.business.project.dto.NexisWorkerResponse;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.mapper.NexisWorkerMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class NexisWorkerService {

    private final NexisWorkerMapper workerMapper;
    private final NexisProjectLookupService projectLookupService;
    private final NexisSiteService siteService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final NexisSiteWorkScopeService siteWorkScopeService;
    private final NexisTeamService teamService;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisWorkerService(NexisWorkerMapper workerMapper,
                             NexisProjectLookupService projectLookupService,
                             NexisSiteService siteService,
                             NexisParticipantCompanyService participantCompanyService,
                             NexisSiteWorkScopeService siteWorkScopeService,
                             NexisTeamService teamService,
                             NexisAccessGuard nexisAccessGuard) {
        this.workerMapper = workerMapper;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.teamService = teamService;
        this.nexisAccessGuard = nexisAccessGuard;
    }

    public PageResponse<NexisWorkerResponse> list(NexisWorkerQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisWorkerQueryRequest query = request == null ? new NexisWorkerQueryRequest() : request;
        LambdaQueryWrapper<NexisWorker> wrapper = new LambdaQueryWrapper<NexisWorker>()
            .eq(NexisWorker::getTenantId, loginUser.getTenantId())
            .eq(NexisWorker::getDeleted, 0);
        if (nexisAccessGuard.hasRole(loginUser, "NEXIS_TEAM_LEADER")) {
            List<Long> leaderTeamIds = teamService.findIdsByLeader(loginUser.getTenantId(), loginUser.getUserId());
            if (leaderTeamIds.isEmpty()) {
                wrapper.eq(NexisWorker::getTeamId, -1L);
            } else {
                wrapper.in(NexisWorker::getTeamId, leaderTeamIds);
            }
        }
        if (query.getProjectId() != null) {
            wrapper.eq(NexisWorker::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(NexisWorker::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(NexisWorker::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (query.getWorkScopeId() != null) {
            wrapper.eq(NexisWorker::getWorkScopeId, query.getWorkScopeId());
        }
        if (query.getTeamId() != null) {
            wrapper.eq(NexisWorker::getTeamId, query.getTeamId());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            List<Long> teamIds = teamService.findIdsByKeyword(keyword, loginUser.getTenantId());
            wrapper.and(w -> {
                w.like(NexisWorker::getWorkerName, keyword)
                    .or()
                    .like(NexisWorker::getWorkerPhone, keyword)
                    .or()
                    .like(NexisWorker::getIdCardNo, keyword)
                    .or()
                    .like(NexisWorker::getJobType, keyword)
                    .or()
                    .like(NexisWorker::getJobName, keyword);
                if (!teamIds.isEmpty()) {
                    w.or().in(NexisWorker::getTeamId, teamIds);
                }
            });
        }
        wrapper.orderByDesc(NexisWorker::getId);
        Page<NexisWorker> page = workerMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public NexisWorkerResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisWorker worker = loadEntity(id, loginUser.getTenantId());
        ensureCanAccessWorker(worker, loginUser);
        return toResponse(worker, loginUser.getTenantId());
    }

    public NexisWorkerResponse create(NexisWorkerCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.WORKER_MANAGE);
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (StringUtils.isBlank(request.getWorkerName())) {
            throw new BusinessException("workerName 不能为空");
        }

        NexisTeam team = null;
        Long projectId = request.getProjectId() == null ? loginUser.getTenantId() : request.getProjectId();
        Long siteId = request.getSiteId();
        Long participantCompanyId = request.getParticipantCompanyId();
        Long workScopeId = request.getWorkScopeId();
        if (request.getTeamId() != null) {
            team = teamService.loadEntity(request.getTeamId(), loginUser.getTenantId());
            teamService.ensureCanAccessTeam(team, loginUser);
            if (request.getProjectId() != null && !request.getProjectId().equals(team.getTenantId())) {
                throw new BusinessException("班组不属于当前项目");
            }
            if (request.getSiteId() != null && team.getSiteId() != null && !request.getSiteId().equals(team.getSiteId())) {
                throw new BusinessException("班组不属于当前标段");
            }
            if (request.getParticipantCompanyId() != null
                && !request.getParticipantCompanyId().equals(team.getParticipantCompanyId())) {
                throw new BusinessException("班组不属于当前参建单位");
            }
            if (request.getWorkScopeId() != null && team.getWorkScopeId() != null
                && !request.getWorkScopeId().equals(team.getWorkScopeId())) {
                throw new BusinessException("班组不属于当前施工范围");
            }
            projectId = team.getTenantId();
            siteId = team.getSiteId() == null ? siteId : team.getSiteId();
            participantCompanyId = team.getParticipantCompanyId();
            workScopeId = team.getWorkScopeId() == null ? workScopeId : team.getWorkScopeId();
        }
        if (nexisAccessGuard.hasRole(loginUser, "NEXIS_TEAM_LEADER") && team == null) {
            throw new BusinessException("班组长新增工人时必须选择自己负责的班组");
        }
        if (participantCompanyId == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }

        NexisSite site = null;
        if (siteId != null) {
            site = siteService.loadEntity(siteId, loginUser.getTenantId());
            if (site.getProjectId() != null) {
                if (request.getProjectId() != null && !request.getProjectId().equals(site.getProjectId())) {
                    throw new BusinessException("标段不属于当前项目");
                }
                projectId = site.getProjectId();
            }
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());
        participantCompanyService.loadEntity(participantCompanyId, loginUser.getTenantId());
        NexisSiteWorkScope workScope = normalizeWorkScope(
            workScopeId,
            loginUser.getTenantId(),
            projectId,
            site == null ? null : site.getId(),
            participantCompanyId);
        if (site == null && workScope != null) {
            site = siteService.loadEntity(workScope.getSiteId(), loginUser.getTenantId());
        }
        Long normalizedTeamId = normalizeTeamId(
            request.getTeamId(),
            team,
            loginUser.getTenantId(),
            projectId,
            site == null ? null : site.getId(),
            participantCompanyId,
            workScope == null ? null : workScope.getId());

        NexisWorker worker = new NexisWorker();
        worker.setTenantId(loginUser.getTenantId());
        worker.setProjectId(projectId);
        worker.setSiteId(site == null ? null : site.getId());
        worker.setParticipantCompanyId(participantCompanyId);
        worker.setWorkScopeId(workScope == null ? null : workScope.getId());
        worker.setTeamId(normalizedTeamId);
        worker.setWorkerName(StringUtils.trim(request.getWorkerName()));
        worker.setWorkerPhone(StringUtils.trimToNull(request.getWorkerPhone()));
        worker.setIdCardNo(StringUtils.upperCase(StringUtils.trimToNull(request.getIdCardNo())));
        worker.setJobType(StringUtils.upperCase(StringUtils.trimToNull(request.getJobType())));
        worker.setJobName(StringUtils.trimToNull(request.getJobName()));
        worker.setStatus(1);
        worker.setDeleted(0);
        worker.setCreatedBy(loginUser.getUserId());
        workerMapper.insert(worker);
        return getById(worker.getId(), loginUser);
    }

    public void ensureCanAccessWorker(NexisWorker worker, LoginUser loginUser) {
        if (!nexisAccessGuard.hasRole(loginUser, "NEXIS_TEAM_LEADER")) {
            return;
        }
        if (worker.getTeamId() == null) {
            throw new BusinessException("班组长不能访问未分班组工人");
        }
        teamService.ensureCanAccessTeam(teamService.loadEntity(worker.getTeamId(), loginUser.getTenantId()), loginUser);
    }

    public NexisWorker loadEntity(Long id, Long tenantId) {
        NexisWorker worker = workerMapper.selectOne(new LambdaQueryWrapper<NexisWorker>()
            .eq(NexisWorker::getId, id)
            .eq(NexisWorker::getTenantId, tenantId)
            .eq(NexisWorker::getDeleted, 0)
            .last("limit 1"));
        if (worker == null) {
            throw new BusinessException("工人不存在");
        }
        return worker;
    }

    public List<Long> findIdsByKeyword(String keyword, Long tenantId) {
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }
        String trimmedKeyword = StringUtils.trim(keyword);
        return workerMapper.selectList(new LambdaQueryWrapper<NexisWorker>()
                .select(NexisWorker::getId)
                .eq(NexisWorker::getTenantId, tenantId)
                .eq(NexisWorker::getDeleted, 0)
                .and(wrapper -> wrapper.like(NexisWorker::getWorkerName, trimmedKeyword)
                    .or()
                    .like(NexisWorker::getWorkerPhone, trimmedKeyword)
                    .or()
                    .like(NexisWorker::getIdCardNo, trimmedKeyword)
                    .or()
                    .like(NexisWorker::getJobType, trimmedKeyword)
                    .or()
                    .like(NexisWorker::getJobName, trimmedKeyword)))
            .stream()
            .map(NexisWorker::getId)
            .toList();
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

    private Long normalizeTeamId(Long teamId,
                                 NexisTeam loadedTeam,
                                 Long tenantId,
                                 Long projectId,
                                 Long siteId,
                                 Long participantCompanyId,
                                 Long workScopeId) {
        if (teamId == null) {
            return null;
        }
        NexisTeam team = loadedTeam == null ? teamService.loadEntity(teamId, tenantId) : loadedTeam;
        if (!projectId.equals(team.getTenantId())) {
            throw new BusinessException("班组不属于当前项目");
        }
        if (siteId != null && team.getSiteId() != null && !siteId.equals(team.getSiteId())) {
            throw new BusinessException("班组不属于当前标段");
        }
        if (!participantCompanyId.equals(team.getParticipantCompanyId())) {
            throw new BusinessException("班组不属于当前参建单位");
        }
        if (workScopeId != null && team.getWorkScopeId() != null && !workScopeId.equals(team.getWorkScopeId())) {
            throw new BusinessException("班组不属于当前施工范围");
        }
        return teamId;
    }

    private NexisWorkerResponse toResponse(NexisWorker worker, Long tenantId) {
        NexisProject project = projectLookupService.loadEntity(worker.getProjectId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(worker.getParticipantCompanyId(), tenantId);
        NexisWorkerResponse response = new NexisWorkerResponse();
        response.setId(worker.getId());
        response.setTenantId(worker.getTenantId());
        response.setProjectId(worker.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(worker.getSiteId());
        if (worker.getSiteId() != null) {
            NexisSite site = siteService.loadEntity(worker.getSiteId(), tenantId);
            response.setSiteName(site.getSiteName());
        }
        response.setParticipantCompanyId(worker.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setWorkScopeId(worker.getWorkScopeId());
        if (worker.getWorkScopeId() != null) {
            NexisSiteWorkScope workScope = siteWorkScopeService.loadEntity(worker.getWorkScopeId(), tenantId);
            response.setWorkScopeName(StringUtils.defaultIfBlank(workScope.getScopeName(), workScope.getScopeStartCode()));
        }
        response.setTeamId(worker.getTeamId());
        if (worker.getTeamId() != null) {
            NexisTeam team = teamService.loadEntity(worker.getTeamId(), tenantId);
        response.setTeamName(team.getTeamName());
        }
        response.setWorkerName(worker.getWorkerName());
        response.setWorkerPhone(worker.getWorkerPhone());
        response.setIdCardNo(worker.getIdCardNo());
        response.setJobType(worker.getJobType());
        response.setJobName(worker.getJobName());
        response.setStatus(worker.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(worker.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
