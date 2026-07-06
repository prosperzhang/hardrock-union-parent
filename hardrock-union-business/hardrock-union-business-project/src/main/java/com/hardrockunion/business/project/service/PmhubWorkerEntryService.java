package com.hardrockunion.business.project.service;

import java.time.LocalDateTime;
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
import com.hardrockunion.business.project.domain.entity.PmhubWorkerEntry;
import com.hardrockunion.business.project.dto.PmhubWorkerEntryActionRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerEntryCreateRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerEntryQueryRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerEntryResponse;
import com.hardrockunion.business.project.enums.PmhubRealNameStatus;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.enums.PmhubWorkerEntryStatus;
import com.hardrockunion.business.project.mapper.PmhubWorkerEntryMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class PmhubWorkerEntryService {

    private final PmhubWorkerEntryMapper workerEntryMapper;
    private final PmhubWorkerService workerService;
    private final PmhubProjectLookupService projectLookupService;
    private final PmhubSiteService siteService;
    private final PmhubParticipantCompanyService participantCompanyService;
    private final PmhubSiteWorkScopeService siteWorkScopeService;
    private final PmhubTeamService teamService;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubWorkerEntryService(PmhubWorkerEntryMapper workerEntryMapper,
                                  PmhubWorkerService workerService,
                                  PmhubProjectLookupService projectLookupService,
                                  PmhubSiteService siteService,
                                  PmhubParticipantCompanyService participantCompanyService,
                                  PmhubSiteWorkScopeService siteWorkScopeService,
                                  PmhubTeamService teamService,
                                  PmhubAccessGuard pmhubAccessGuard) {
        this.workerEntryMapper = workerEntryMapper;
        this.workerService = workerService;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.teamService = teamService;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public PageResponse<PmhubWorkerEntryResponse> list(PmhubWorkerEntryQueryRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubWorkerEntryQueryRequest query = request == null ? new PmhubWorkerEntryQueryRequest() : request;
        LambdaQueryWrapper<PmhubWorkerEntry> wrapper = new LambdaQueryWrapper<PmhubWorkerEntry>()
            .eq(PmhubWorkerEntry::getTenantId, loginUser.getTenantId())
            .eq(PmhubWorkerEntry::getDeleted, 0);
        if (query.getProjectId() != null) {
            wrapper.eq(PmhubWorkerEntry::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(PmhubWorkerEntry::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(PmhubWorkerEntry::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (query.getWorkScopeId() != null) {
            wrapper.eq(PmhubWorkerEntry::getWorkScopeId, query.getWorkScopeId());
        }
        if (query.getTeamId() != null) {
            wrapper.eq(PmhubWorkerEntry::getTeamId, query.getTeamId());
        }
        if (query.getWorkerId() != null) {
            wrapper.eq(PmhubWorkerEntry::getWorkerId, query.getWorkerId());
        }
        if (StringUtils.isNotBlank(query.getEntryStatus())) {
            PmhubWorkerEntryStatus entryStatus = PmhubWorkerEntryStatus.fromCode(query.getEntryStatus());
            if (entryStatus == null) {
                throw new BusinessException("entryStatus 非法");
            }
            wrapper.eq(PmhubWorkerEntry::getEntryStatus, entryStatus.getCode());
        }
        if (StringUtils.isNotBlank(query.getRealNameStatus())) {
            PmhubRealNameStatus realNameStatus = PmhubRealNameStatus.fromCode(query.getRealNameStatus());
            if (realNameStatus == null) {
                throw new BusinessException("realNameStatus 非法");
            }
            wrapper.eq(PmhubWorkerEntry::getRealNameStatus, realNameStatus.getCode());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            List<Long> workerIds = workerService.findIdsByKeyword(keyword, loginUser.getTenantId());
            List<Long> teamIds = teamService.findIdsByKeyword(keyword, loginUser.getTenantId());
            wrapper.and(w -> {
                w.like(PmhubWorkerEntry::getEntryNo, keyword);
                if (!workerIds.isEmpty()) {
                    w.or().in(PmhubWorkerEntry::getWorkerId, workerIds);
                }
                if (!teamIds.isEmpty()) {
                    w.or().in(PmhubWorkerEntry::getTeamId, teamIds);
                }
            });
        }
        wrapper.orderByDesc(PmhubWorkerEntry::getId);
        Page<PmhubWorkerEntry> page = workerEntryMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public PmhubWorkerEntryResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public PmhubWorkerEntryResponse create(PmhubWorkerEntryCreateRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getWorkerId() == null) {
            throw new BusinessException("workerId 不能为空");
        }
        PmhubWorker worker = workerService.loadEntity(request.getWorkerId(), loginUser.getTenantId());
        if (StringUtils.isBlank(worker.getIdCardNo())) {
            throw new BusinessException("工人未维护身份证号，无法办理实名进场");
        }
        ensureNoActiveEntry(worker.getId(), loginUser.getTenantId());

        PmhubWorkerEntry entry = new PmhubWorkerEntry();
        entry.setTenantId(loginUser.getTenantId());
        entry.setProjectId(worker.getProjectId());
        entry.setSiteId(worker.getSiteId());
        entry.setParticipantCompanyId(worker.getParticipantCompanyId());
        entry.setWorkScopeId(worker.getWorkScopeId());
        entry.setTeamId(worker.getTeamId());
        entry.setWorkerId(worker.getId());
        entry.setEntryNo(generateEntryNo(loginUser.getTenantId(), worker.getId()));
        entry.setRealNameStatus(PmhubRealNameStatus.VERIFIED.getCode());
        entry.setEntryStatus(PmhubWorkerEntryStatus.REGISTERED.getCode());
        entry.setRemark(StringUtils.trimToNull(request.getRemark()));
        entry.setStatus(1);
        entry.setDeleted(0);
        entry.setCreatedBy(loginUser.getUserId());
        workerEntryMapper.insert(entry);
        return getById(entry.getId(), loginUser);
    }

    public PmhubWorkerEntryResponse enter(Long id, PmhubWorkerEntryActionRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubWorkerEntry entry = loadEntity(id, loginUser.getTenantId());
        if (!PmhubWorkerEntryStatus.REGISTERED.getCode().equals(entry.getEntryStatus())) {
            throw new BusinessException("当前实名进场记录不允许执行进场");
        }
        entry.setEntryStatus(PmhubWorkerEntryStatus.ENTERED.getCode());
        entry.setEnteredAt(LocalDateTime.now());
        entry.setRemark(StringUtils.trimToNull(request == null ? null : request.getRemark()));
        workerEntryMapper.updateById(entry);
        return getById(id, loginUser);
    }

    public PmhubWorkerEntryResponse exit(Long id, PmhubWorkerEntryActionRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubWorkerEntry entry = loadEntity(id, loginUser.getTenantId());
        if (!PmhubWorkerEntryStatus.ENTERED.getCode().equals(entry.getEntryStatus())) {
            throw new BusinessException("当前实名进场记录不允许执行退场");
        }
        entry.setEntryStatus(PmhubWorkerEntryStatus.EXITED.getCode());
        entry.setExitedAt(LocalDateTime.now());
        entry.setRemark(StringUtils.trimToNull(request == null ? null : request.getRemark()));
        workerEntryMapper.updateById(entry);
        return getById(id, loginUser);
    }

    public PmhubWorkerEntry loadEntity(Long id, Long tenantId) {
        PmhubWorkerEntry entry = workerEntryMapper.selectOne(new LambdaQueryWrapper<PmhubWorkerEntry>()
            .eq(PmhubWorkerEntry::getId, id)
            .eq(PmhubWorkerEntry::getTenantId, tenantId)
            .eq(PmhubWorkerEntry::getDeleted, 0)
            .last("limit 1"));
        if (entry == null) {
            throw new BusinessException("实名进场记录不存在");
        }
        return entry;
    }

    private void ensureNoActiveEntry(Long workerId, Long tenantId) {
        Long count = workerEntryMapper.selectCount(new LambdaQueryWrapper<PmhubWorkerEntry>()
            .eq(PmhubWorkerEntry::getTenantId, tenantId)
            .eq(PmhubWorkerEntry::getWorkerId, workerId)
            .eq(PmhubWorkerEntry::getDeleted, 0)
            .in(PmhubWorkerEntry::getEntryStatus,
                PmhubWorkerEntryStatus.REGISTERED.getCode(),
                PmhubWorkerEntryStatus.ENTERED.getCode()));
        if (count != null && count > 0) {
            throw new BusinessException("该工人已有未完成的实名进场记录");
        }
    }

    private String generateEntryNo(Long tenantId, Long workerId) {
        return "ENTRY" + tenantId + System.currentTimeMillis() + workerId;
    }

    private PmhubWorkerEntryResponse toResponse(PmhubWorkerEntry entry, Long tenantId) {
        PmhubProject project = projectLookupService.loadEntity(entry.getProjectId(), tenantId);
        PmhubSite site = siteService.loadEntity(entry.getSiteId(), tenantId);
        PmhubParticipantCompany company = participantCompanyService.loadEntity(entry.getParticipantCompanyId(), tenantId);
        PmhubWorker worker = workerService.loadEntity(entry.getWorkerId(), tenantId);
        PmhubWorkerEntryResponse response = new PmhubWorkerEntryResponse();
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
            PmhubSiteWorkScope workScope = siteWorkScopeService.loadEntity(entry.getWorkScopeId(), tenantId);
            response.setWorkScopeName(StringUtils.defaultIfBlank(workScope.getScopeName(), workScope.getScopeStartCode()));
        }
        response.setTeamId(entry.getTeamId());
        if (entry.getTeamId() != null) {
            PmhubTeam team = teamService.loadEntity(entry.getTeamId(), tenantId);
            response.setTeamName(team.getTeamName());
        }
        response.setWorkerId(entry.getWorkerId());
        response.setWorkerName(worker.getWorkerName());
        response.setWorkerPhone(worker.getWorkerPhone());
        response.setIdCardNo(worker.getIdCardNo());
        response.setRealNameStatus(entry.getRealNameStatus());
        PmhubRealNameStatus realNameStatus = PmhubRealNameStatus.fromCode(entry.getRealNameStatus());
        response.setRealNameStatusLabel(realNameStatus == null ? null : realNameStatus.getLabel());
        response.setEntryStatus(entry.getEntryStatus());
        PmhubWorkerEntryStatus entryStatus = PmhubWorkerEntryStatus.fromCode(entry.getEntryStatus());
        response.setEntryStatusLabel(entryStatus == null ? null : entryStatus.getLabel());
        response.setEnteredAt(entry.getEnteredAt());
        response.setExitedAt(entry.getExitedAt());
        response.setRemark(entry.getRemark());
        response.setStatus(entry.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(entry.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
