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
import com.hardrockunion.business.project.domain.entity.PmhubWorker;
import com.hardrockunion.business.project.dto.PmhubWorkerCreateRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerQueryRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerResponse;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.mapper.PmhubWorkerMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class PmhubWorkerService {

    private final PmhubWorkerMapper workerMapper;
    private final PmhubProjectLookupService projectLookupService;
    private final PmhubSiteService siteService;
    private final PmhubParticipantCompanyService participantCompanyService;
    private final PmhubSiteWorkScopeService siteWorkScopeService;
    private final PmhubTeamService teamService;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubWorkerService(PmhubWorkerMapper workerMapper,
                             PmhubProjectLookupService projectLookupService,
                             PmhubSiteService siteService,
                             PmhubParticipantCompanyService participantCompanyService,
                             PmhubSiteWorkScopeService siteWorkScopeService,
                             PmhubTeamService teamService,
                             PmhubAccessGuard pmhubAccessGuard) {
        this.workerMapper = workerMapper;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.teamService = teamService;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public PageResponse<PmhubWorkerResponse> list(PmhubWorkerQueryRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubWorkerQueryRequest query = request == null ? new PmhubWorkerQueryRequest() : request;
        LambdaQueryWrapper<PmhubWorker> wrapper = new LambdaQueryWrapper<PmhubWorker>()
            .eq(PmhubWorker::getTenantId, loginUser.getTenantId())
            .eq(PmhubWorker::getDeleted, 0);
        if (query.getProjectId() != null) {
            wrapper.eq(PmhubWorker::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(PmhubWorker::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(PmhubWorker::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (query.getWorkScopeId() != null) {
            wrapper.eq(PmhubWorker::getWorkScopeId, query.getWorkScopeId());
        }
        if (query.getTeamId() != null) {
            wrapper.eq(PmhubWorker::getTeamId, query.getTeamId());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            List<Long> teamIds = teamService.findIdsByKeyword(keyword, loginUser.getTenantId());
            wrapper.and(w -> {
                w.like(PmhubWorker::getWorkerName, keyword)
                    .or()
                    .like(PmhubWorker::getWorkerPhone, keyword)
                    .or()
                    .like(PmhubWorker::getIdCardNo, keyword)
                    .or()
                    .like(PmhubWorker::getJobType, keyword)
                    .or()
                    .like(PmhubWorker::getJobName, keyword);
                if (!teamIds.isEmpty()) {
                    w.or().in(PmhubWorker::getTeamId, teamIds);
                }
            });
        }
        wrapper.orderByDesc(PmhubWorker::getId);
        Page<PmhubWorker> page = workerMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public PmhubWorkerResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public PmhubWorkerResponse create(PmhubWorkerCreateRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getSiteId() == null) {
            throw new BusinessException("siteId 不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getWorkerName())) {
            throw new BusinessException("workerName 不能为空");
        }

        PmhubSite site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
        Long projectId = site.getProjectId() != null ? site.getProjectId() : request.getProjectId();
        if (projectId == null) {
            throw new BusinessException("标段尚未关联项目，无法创建工人");
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());
        participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long workScopeId = normalizeWorkScopeId(request.getWorkScopeId(), loginUser.getTenantId(), site.getId(), request.getParticipantCompanyId());
        Long teamId = normalizeTeamId(request.getTeamId(), loginUser.getTenantId(), site.getId(), request.getParticipantCompanyId(), workScopeId);

        PmhubWorker worker = new PmhubWorker();
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

    public PmhubWorker loadEntity(Long id, Long tenantId) {
        PmhubWorker worker = workerMapper.selectOne(new LambdaQueryWrapper<PmhubWorker>()
            .eq(PmhubWorker::getId, id)
            .eq(PmhubWorker::getTenantId, tenantId)
            .eq(PmhubWorker::getDeleted, 0)
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
        return workerMapper.selectList(new LambdaQueryWrapper<PmhubWorker>()
                .select(PmhubWorker::getId)
                .eq(PmhubWorker::getTenantId, tenantId)
                .eq(PmhubWorker::getDeleted, 0)
                .and(wrapper -> wrapper.like(PmhubWorker::getWorkerName, trimmedKeyword)
                    .or()
                    .like(PmhubWorker::getWorkerPhone, trimmedKeyword)
                    .or()
                    .like(PmhubWorker::getIdCardNo, trimmedKeyword)
                    .or()
                    .like(PmhubWorker::getJobType, trimmedKeyword)
                    .or()
                    .like(PmhubWorker::getJobName, trimmedKeyword)))
            .stream()
            .map(PmhubWorker::getId)
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

    private Long normalizeTeamId(Long teamId, Long tenantId, Long siteId, Long participantCompanyId, Long workScopeId) {
        if (teamId == null) {
            return null;
        }
        PmhubTeam team = teamService.loadEntity(teamId, tenantId);
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

    private PmhubWorkerResponse toResponse(PmhubWorker worker, Long tenantId) {
        PmhubProject project = projectLookupService.loadEntity(worker.getProjectId(), tenantId);
        PmhubSite site = siteService.loadEntity(worker.getSiteId(), tenantId);
        PmhubParticipantCompany company = participantCompanyService.loadEntity(worker.getParticipantCompanyId(), tenantId);
        PmhubWorkerResponse response = new PmhubWorkerResponse();
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
            PmhubSiteWorkScope workScope = siteWorkScopeService.loadEntity(worker.getWorkScopeId(), tenantId);
            response.setWorkScopeName(StringUtils.defaultIfBlank(workScope.getScopeName(), workScope.getScopeStartCode()));
        }
        response.setTeamId(worker.getTeamId());
        if (worker.getTeamId() != null) {
            PmhubTeam team = teamService.loadEntity(worker.getTeamId(), tenantId);
        response.setTeamName(team.getTeamName());
        }
        response.setWorkerName(worker.getWorkerName());
        response.setWorkerPhone(worker.getWorkerPhone());
        response.setIdCardNo(worker.getIdCardNo());
        response.setJobType(worker.getJobType());
        response.setJobName(worker.getJobName());
        response.setStatus(worker.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(worker.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
