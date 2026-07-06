package com.hardrockunion.business.project.service;

import java.time.LocalDateTime;
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
import com.hardrockunion.business.project.domain.entity.NexisWorkerEntry;
import com.hardrockunion.business.project.dto.NexisWorkerEntryActionRequest;
import com.hardrockunion.business.project.dto.NexisWorkerEntryCreateRequest;
import com.hardrockunion.business.project.dto.NexisWorkerEntryQueryRequest;
import com.hardrockunion.business.project.dto.NexisWorkerEntryResponse;
import com.hardrockunion.business.project.enums.NexisRealNameStatus;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.enums.NexisWorkerEntryStatus;
import com.hardrockunion.business.project.mapper.NexisWorkerEntryMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class NexisWorkerEntryService {

    private final NexisWorkerEntryMapper workerEntryMapper;
    private final NexisWorkerService workerService;
    private final NexisProjectLookupService projectLookupService;
    private final NexisSiteService siteService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final NexisSiteWorkScopeService siteWorkScopeService;
    private final NexisTeamService teamService;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisWorkerEntryService(NexisWorkerEntryMapper workerEntryMapper,
                                  NexisWorkerService workerService,
                                  NexisProjectLookupService projectLookupService,
                                  NexisSiteService siteService,
                                  NexisParticipantCompanyService participantCompanyService,
                                  NexisSiteWorkScopeService siteWorkScopeService,
                                  NexisTeamService teamService,
                                  NexisAccessGuard nexisAccessGuard) {
        this.workerEntryMapper = workerEntryMapper;
        this.workerService = workerService;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.teamService = teamService;
        this.nexisAccessGuard = nexisAccessGuard;
    }

    public PageResponse<NexisWorkerEntryResponse> list(NexisWorkerEntryQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisWorkerEntryQueryRequest query = request == null ? new NexisWorkerEntryQueryRequest() : request;
        LambdaQueryWrapper<NexisWorkerEntry> wrapper = new LambdaQueryWrapper<NexisWorkerEntry>()
            .eq(NexisWorkerEntry::getTenantId, loginUser.getTenantId())
            .eq(NexisWorkerEntry::getDeleted, 0);
        if (query.getProjectId() != null) {
            wrapper.eq(NexisWorkerEntry::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(NexisWorkerEntry::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(NexisWorkerEntry::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (query.getWorkScopeId() != null) {
            wrapper.eq(NexisWorkerEntry::getWorkScopeId, query.getWorkScopeId());
        }
        if (query.getTeamId() != null) {
            wrapper.eq(NexisWorkerEntry::getTeamId, query.getTeamId());
        }
        if (query.getWorkerId() != null) {
            wrapper.eq(NexisWorkerEntry::getWorkerId, query.getWorkerId());
        }
        if (StringUtils.isNotBlank(query.getEntryStatus())) {
            NexisWorkerEntryStatus entryStatus = NexisWorkerEntryStatus.fromCode(query.getEntryStatus());
            if (entryStatus == null) {
                throw new BusinessException("entryStatus 非法");
            }
            wrapper.eq(NexisWorkerEntry::getEntryStatus, entryStatus.getCode());
        }
        if (StringUtils.isNotBlank(query.getRealNameStatus())) {
            NexisRealNameStatus realNameStatus = NexisRealNameStatus.fromCode(query.getRealNameStatus());
            if (realNameStatus == null) {
                throw new BusinessException("realNameStatus 非法");
            }
            wrapper.eq(NexisWorkerEntry::getRealNameStatus, realNameStatus.getCode());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            List<Long> workerIds = workerService.findIdsByKeyword(keyword, loginUser.getTenantId());
            List<Long> teamIds = teamService.findIdsByKeyword(keyword, loginUser.getTenantId());
            wrapper.and(w -> {
                w.like(NexisWorkerEntry::getEntryNo, keyword);
                if (!workerIds.isEmpty()) {
                    w.or().in(NexisWorkerEntry::getWorkerId, workerIds);
                }
                if (!teamIds.isEmpty()) {
                    w.or().in(NexisWorkerEntry::getTeamId, teamIds);
                }
            });
        }
        wrapper.orderByDesc(NexisWorkerEntry::getId);
        Page<NexisWorkerEntry> page = workerEntryMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public NexisWorkerEntryResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public NexisWorkerEntryResponse create(NexisWorkerEntryCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getWorkerId() == null) {
            throw new BusinessException("workerId 不能为空");
        }
        NexisWorker worker = workerService.loadEntity(request.getWorkerId(), loginUser.getTenantId());
        if (StringUtils.isBlank(worker.getIdCardNo())) {
            throw new BusinessException("工人未维护身份证号，无法办理实名进场");
        }
        ensureNoActiveEntry(worker.getId(), loginUser.getTenantId());

        NexisWorkerEntry entry = new NexisWorkerEntry();
        entry.setTenantId(loginUser.getTenantId());
        entry.setProjectId(worker.getProjectId());
        entry.setSiteId(worker.getSiteId());
        entry.setParticipantCompanyId(worker.getParticipantCompanyId());
        entry.setWorkScopeId(worker.getWorkScopeId());
        entry.setTeamId(worker.getTeamId());
        entry.setWorkerId(worker.getId());
        entry.setEntryNo(generateEntryNo(loginUser.getTenantId(), worker.getId()));
        entry.setRealNameStatus(NexisRealNameStatus.VERIFIED.getCode());
        entry.setEntryStatus(NexisWorkerEntryStatus.REGISTERED.getCode());
        entry.setRemark(StringUtils.trimToNull(request.getRemark()));
        entry.setStatus(1);
        entry.setDeleted(0);
        entry.setCreatedBy(loginUser.getUserId());
        workerEntryMapper.insert(entry);
        return getById(entry.getId(), loginUser);
    }

    public NexisWorkerEntryResponse enter(Long id, NexisWorkerEntryActionRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisWorkerEntry entry = loadEntity(id, loginUser.getTenantId());
        if (!NexisWorkerEntryStatus.REGISTERED.getCode().equals(entry.getEntryStatus())) {
            throw new BusinessException("当前实名进场记录不允许执行进场");
        }
        entry.setEntryStatus(NexisWorkerEntryStatus.ENTERED.getCode());
        entry.setEnteredAt(LocalDateTime.now());
        entry.setRemark(StringUtils.trimToNull(request == null ? null : request.getRemark()));
        workerEntryMapper.updateById(entry);
        return getById(id, loginUser);
    }

    public NexisWorkerEntryResponse exit(Long id, NexisWorkerEntryActionRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisWorkerEntry entry = loadEntity(id, loginUser.getTenantId());
        if (!NexisWorkerEntryStatus.ENTERED.getCode().equals(entry.getEntryStatus())) {
            throw new BusinessException("当前实名进场记录不允许执行退场");
        }
        entry.setEntryStatus(NexisWorkerEntryStatus.EXITED.getCode());
        entry.setExitedAt(LocalDateTime.now());
        entry.setRemark(StringUtils.trimToNull(request == null ? null : request.getRemark()));
        workerEntryMapper.updateById(entry);
        return getById(id, loginUser);
    }

    public NexisWorkerEntry loadEntity(Long id, Long tenantId) {
        NexisWorkerEntry entry = workerEntryMapper.selectOne(new LambdaQueryWrapper<NexisWorkerEntry>()
            .eq(NexisWorkerEntry::getId, id)
            .eq(NexisWorkerEntry::getTenantId, tenantId)
            .eq(NexisWorkerEntry::getDeleted, 0)
            .last("limit 1"));
        if (entry == null) {
            throw new BusinessException("实名进场记录不存在");
        }
        return entry;
    }

    private void ensureNoActiveEntry(Long workerId, Long tenantId) {
        Long count = workerEntryMapper.selectCount(new LambdaQueryWrapper<NexisWorkerEntry>()
            .eq(NexisWorkerEntry::getTenantId, tenantId)
            .eq(NexisWorkerEntry::getWorkerId, workerId)
            .eq(NexisWorkerEntry::getDeleted, 0)
            .in(NexisWorkerEntry::getEntryStatus,
                NexisWorkerEntryStatus.REGISTERED.getCode(),
                NexisWorkerEntryStatus.ENTERED.getCode()));
        if (count != null && count > 0) {
            throw new BusinessException("该工人已有未完成的实名进场记录");
        }
    }

    private String generateEntryNo(Long tenantId, Long workerId) {
        return "ENTRY" + tenantId + System.currentTimeMillis() + workerId;
    }

    private NexisWorkerEntryResponse toResponse(NexisWorkerEntry entry, Long tenantId) {
        NexisProject project = projectLookupService.loadEntity(entry.getProjectId(), tenantId);
        NexisSite site = siteService.loadEntity(entry.getSiteId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(entry.getParticipantCompanyId(), tenantId);
        NexisWorker worker = workerService.loadEntity(entry.getWorkerId(), tenantId);
        NexisWorkerEntryResponse response = new NexisWorkerEntryResponse();
        response.setId(entry.getId());
        response.setTenantId(entry.getTenantId());
        response.setEntryNo(entry.getEntryNo());
        response.setProjectId(entry.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(entry.getSiteId());
        response.setSiteName(site.getSiteName());
        response.setParticipantCompanyId(entry.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setWorkScopeId(entry.getWorkScopeId());
        if (entry.getWorkScopeId() != null) {
            NexisSiteWorkScope workScope = siteWorkScopeService.loadEntity(entry.getWorkScopeId(), tenantId);
            response.setWorkScopeName(StringUtils.defaultIfBlank(workScope.getScopeName(), workScope.getScopeStartCode()));
        }
        response.setTeamId(entry.getTeamId());
        if (entry.getTeamId() != null) {
            NexisTeam team = teamService.loadEntity(entry.getTeamId(), tenantId);
            response.setTeamName(team.getTeamName());
        }
        response.setWorkerId(entry.getWorkerId());
        response.setWorkerName(worker.getWorkerName());
        response.setWorkerPhone(worker.getWorkerPhone());
        response.setIdCardNo(worker.getIdCardNo());
        response.setRealNameStatus(entry.getRealNameStatus());
        NexisRealNameStatus realNameStatus = NexisRealNameStatus.fromCode(entry.getRealNameStatus());
        response.setRealNameStatusLabel(realNameStatus == null ? null : realNameStatus.getLabel());
        response.setEntryStatus(entry.getEntryStatus());
        NexisWorkerEntryStatus entryStatus = NexisWorkerEntryStatus.fromCode(entry.getEntryStatus());
        response.setEntryStatusLabel(entryStatus == null ? null : entryStatus.getLabel());
        response.setEnteredAt(entry.getEnteredAt());
        response.setExitedAt(entry.getExitedAt());
        response.setRemark(entry.getRemark());
        response.setStatus(entry.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(entry.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
