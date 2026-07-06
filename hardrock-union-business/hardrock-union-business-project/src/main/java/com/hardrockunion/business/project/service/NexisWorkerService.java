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
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public NexisWorkerResponse create(NexisWorkerCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getSiteId() == null) {
            throw new BusinessException("siteId 不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getWorkerName())) {
            throw new BusinessException("workerName 不能为空");
        }

        NexisSite site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
        Long projectId = site.getProjectId() != null ? site.getProjectId() : request.getProjectId();
        if (projectId == null) {
            throw new BusinessException("标段尚未关联项目，无法创建工人");
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());
        participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long workScopeId = normalizeWorkScopeId(request.getWorkScopeId(), loginUser.getTenantId(), site.getId(), request.getParticipantCompanyId());
        Long teamId = normalizeTeamId(request.getTeamId(), loginUser.getTenantId(), site.getId(), request.getParticipantCompanyId(), workScopeId);

        NexisWorker worker = new NexisWorker();
        worker.setTenantId(loginUser.getTenantId());
        worker.setProjectId(projectId);
        worker.setSiteId(site.getId());
        worker.setParticipantCompanyId(request.getParticipantCompanyId());
        worker.setWorkScopeId(workScopeId);
        worker.setTeamId(teamId);
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

    private Long normalizeTeamId(Long teamId, Long tenantId, Long siteId, Long participantCompanyId, Long workScopeId) {
        if (teamId == null) {
            return null;
        }
        NexisTeam team = teamService.loadEntity(teamId, tenantId);
        if (!siteId.equals(team.getSiteId())) {
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
        NexisSite site = siteService.loadEntity(worker.getSiteId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(worker.getParticipantCompanyId(), tenantId);
        NexisWorkerResponse response = new NexisWorkerResponse();
        response.setId(worker.getId());
        response.setTenantId(worker.getTenantId());
        response.setProjectId(worker.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(worker.getSiteId());
        response.setSiteName(site.getSiteName());
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
