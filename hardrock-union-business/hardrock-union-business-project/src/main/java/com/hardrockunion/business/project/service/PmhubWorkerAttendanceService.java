package com.hardrockunion.business.project.service;

import java.time.LocalDate;
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
import com.hardrockunion.business.project.domain.entity.PmhubWorkerAttendance;
import com.hardrockunion.business.project.domain.entity.PmhubWorkerEntry;
import com.hardrockunion.business.project.dto.PmhubWorkerAttendanceActionRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerAttendanceCheckInRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerAttendanceQueryRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerAttendanceResponse;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.enums.PmhubWorkerAttendanceStatus;
import com.hardrockunion.business.project.enums.PmhubWorkerEntryStatus;
import com.hardrockunion.business.project.mapper.PmhubWorkerAttendanceMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class PmhubWorkerAttendanceService {

    private final PmhubWorkerAttendanceMapper workerAttendanceMapper;
    private final PmhubWorkerEntryService workerEntryService;
    private final PmhubWorkerService workerService;
    private final PmhubProjectLookupService projectLookupService;
    private final PmhubSiteService siteService;
    private final PmhubParticipantCompanyService participantCompanyService;
    private final PmhubSiteWorkScopeService siteWorkScopeService;
    private final PmhubTeamService teamService;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubWorkerAttendanceService(PmhubWorkerAttendanceMapper workerAttendanceMapper,
                                       PmhubWorkerEntryService workerEntryService,
                                       PmhubWorkerService workerService,
                                       PmhubProjectLookupService projectLookupService,
                                       PmhubSiteService siteService,
                                       PmhubParticipantCompanyService participantCompanyService,
                                       PmhubSiteWorkScopeService siteWorkScopeService,
                                       PmhubTeamService teamService,
                                       PmhubAccessGuard pmhubAccessGuard) {
        this.workerAttendanceMapper = workerAttendanceMapper;
        this.workerEntryService = workerEntryService;
        this.workerService = workerService;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.teamService = teamService;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public PageResponse<PmhubWorkerAttendanceResponse> list(PmhubWorkerAttendanceQueryRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubWorkerAttendanceQueryRequest query = request == null ? new PmhubWorkerAttendanceQueryRequest() : request;
        if (query.getAttendanceDateFrom() != null && query.getAttendanceDateTo() != null
            && query.getAttendanceDateFrom().isAfter(query.getAttendanceDateTo())) {
            throw new BusinessException("attendanceDateFrom 不能晚于 attendanceDateTo");
        }
        LambdaQueryWrapper<PmhubWorkerAttendance> wrapper = new LambdaQueryWrapper<PmhubWorkerAttendance>()
            .eq(PmhubWorkerAttendance::getTenantId, loginUser.getTenantId())
            .eq(PmhubWorkerAttendance::getDeleted, 0);
        if (query.getProjectId() != null) {
            wrapper.eq(PmhubWorkerAttendance::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(PmhubWorkerAttendance::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(PmhubWorkerAttendance::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (query.getWorkScopeId() != null) {
            wrapper.eq(PmhubWorkerAttendance::getWorkScopeId, query.getWorkScopeId());
        }
        if (query.getTeamId() != null) {
            wrapper.eq(PmhubWorkerAttendance::getTeamId, query.getTeamId());
        }
        if (query.getWorkerId() != null) {
            wrapper.eq(PmhubWorkerAttendance::getWorkerId, query.getWorkerId());
        }
        if (query.getEntryId() != null) {
            wrapper.eq(PmhubWorkerAttendance::getEntryId, query.getEntryId());
        }
        if (query.getAttendanceDateFrom() != null) {
            wrapper.ge(PmhubWorkerAttendance::getAttendanceDate, query.getAttendanceDateFrom());
        }
        if (query.getAttendanceDateTo() != null) {
            wrapper.le(PmhubWorkerAttendance::getAttendanceDate, query.getAttendanceDateTo());
        }
        if (StringUtils.isNotBlank(query.getAttendanceStatus())) {
            PmhubWorkerAttendanceStatus attendanceStatus = PmhubWorkerAttendanceStatus.fromCode(query.getAttendanceStatus());
            if (attendanceStatus == null) {
                throw new BusinessException("attendanceStatus 非法");
            }
            wrapper.eq(PmhubWorkerAttendance::getAttendanceStatus, attendanceStatus.getCode());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            List<Long> workerIds = workerService.findIdsByKeyword(keyword, loginUser.getTenantId());
            List<Long> teamIds = teamService.findIdsByKeyword(keyword, loginUser.getTenantId());
            wrapper.and(w -> {
                w.like(PmhubWorkerAttendance::getAttendanceNo, keyword);
                if (!workerIds.isEmpty()) {
                    w.or().in(PmhubWorkerAttendance::getWorkerId, workerIds);
                }
                if (!teamIds.isEmpty()) {
                    w.or().in(PmhubWorkerAttendance::getTeamId, teamIds);
                }
            });
        }
        wrapper.orderByDesc(PmhubWorkerAttendance::getAttendanceDate)
            .orderByDesc(PmhubWorkerAttendance::getId);
        Page<PmhubWorkerAttendance> page = workerAttendanceMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public PmhubWorkerAttendanceResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public PmhubWorkerAttendanceResponse checkIn(PmhubWorkerAttendanceCheckInRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getEntryId() == null) {
            throw new BusinessException("entryId 不能为空");
        }
        PmhubWorkerEntry entry = workerEntryService.loadEntity(request.getEntryId(), loginUser.getTenantId());
        if (!PmhubWorkerEntryStatus.ENTERED.getCode().equals(entry.getEntryStatus())) {
            throw new BusinessException("当前实名进场记录不处于已进场状态，无法签到");
        }
        ensureNoAttendanceForToday(entry.getWorkerId(), loginUser.getTenantId());

        PmhubWorkerAttendance attendance = new PmhubWorkerAttendance();
        attendance.setTenantId(loginUser.getTenantId());
        attendance.setAttendanceNo(generateAttendanceNo(loginUser.getTenantId(), entry.getWorkerId()));
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setProjectId(entry.getProjectId());
        attendance.setSiteId(entry.getSiteId());
        attendance.setParticipantCompanyId(entry.getParticipantCompanyId());
        attendance.setWorkScopeId(entry.getWorkScopeId());
        attendance.setTeamId(entry.getTeamId());
        attendance.setWorkerId(entry.getWorkerId());
        attendance.setEntryId(entry.getId());
        attendance.setAttendanceStatus(PmhubWorkerAttendanceStatus.CHECKED_IN.getCode());
        attendance.setCheckInAt(LocalDateTime.now());
        attendance.setRemark(StringUtils.trimToNull(request.getRemark()));
        attendance.setStatus(1);
        attendance.setDeleted(0);
        attendance.setCreatedBy(loginUser.getUserId());
        workerAttendanceMapper.insert(attendance);
        return getById(attendance.getId(), loginUser);
    }

    public PmhubWorkerAttendanceResponse checkOut(Long id, PmhubWorkerAttendanceActionRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubWorkerAttendance attendance = loadEntity(id, loginUser.getTenantId());
        if (!PmhubWorkerAttendanceStatus.CHECKED_IN.getCode().equals(attendance.getAttendanceStatus())) {
            throw new BusinessException("当前考勤记录不允许签退");
        }
        attendance.setAttendanceStatus(PmhubWorkerAttendanceStatus.CHECKED_OUT.getCode());
        attendance.setCheckOutAt(LocalDateTime.now());
        attendance.setRemark(StringUtils.trimToNull(request == null ? null : request.getRemark()));
        workerAttendanceMapper.updateById(attendance);
        return getById(id, loginUser);
    }

    public PmhubWorkerAttendance loadEntity(Long id, Long tenantId) {
        PmhubWorkerAttendance attendance = workerAttendanceMapper.selectOne(new LambdaQueryWrapper<PmhubWorkerAttendance>()
            .eq(PmhubWorkerAttendance::getId, id)
            .eq(PmhubWorkerAttendance::getTenantId, tenantId)
            .eq(PmhubWorkerAttendance::getDeleted, 0)
            .last("limit 1"));
        if (attendance == null) {
            throw new BusinessException("考勤记录不存在");
        }
        return attendance;
    }

    private void ensureNoAttendanceForToday(Long workerId, Long tenantId) {
        Long count = workerAttendanceMapper.selectCount(new LambdaQueryWrapper<PmhubWorkerAttendance>()
            .eq(PmhubWorkerAttendance::getTenantId, tenantId)
            .eq(PmhubWorkerAttendance::getWorkerId, workerId)
            .eq(PmhubWorkerAttendance::getAttendanceDate, LocalDate.now())
            .eq(PmhubWorkerAttendance::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BusinessException("该工人今天已完成签到");
        }
    }

    private String generateAttendanceNo(Long tenantId, Long workerId) {
        return "ATT" + tenantId + System.currentTimeMillis() + workerId;
    }

    private PmhubWorkerAttendanceResponse toResponse(PmhubWorkerAttendance attendance, Long tenantId) {
        PmhubProject project = projectLookupService.loadEntity(attendance.getProjectId(), tenantId);
        PmhubSite site = siteService.loadEntity(attendance.getSiteId(), tenantId);
        PmhubParticipantCompany company = participantCompanyService.loadEntity(attendance.getParticipantCompanyId(), tenantId);
        PmhubWorker worker = workerService.loadEntity(attendance.getWorkerId(), tenantId);
        PmhubWorkerEntry entry = workerEntryService.loadEntity(attendance.getEntryId(), tenantId);
        PmhubWorkerAttendanceResponse response = new PmhubWorkerAttendanceResponse();
        response.setId(attendance.getId());
        response.setTenantId(attendance.getTenantId());
        response.setAttendanceNo(attendance.getAttendanceNo());
        response.setAttendanceDate(attendance.getAttendanceDate());
        response.setProjectId(attendance.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(attendance.getSiteId());
        response.setSiteName(site.getSiteName());
        response.setParticipantCompanyId(attendance.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setWorkScopeId(attendance.getWorkScopeId());
        if (attendance.getWorkScopeId() != null) {
            PmhubSiteWorkScope workScope = siteWorkScopeService.loadEntity(attendance.getWorkScopeId(), tenantId);
            response.setWorkScopeName(StringUtils.defaultIfBlank(workScope.getScopeName(), workScope.getScopeStartCode()));
        }
        response.setTeamId(attendance.getTeamId());
        if (attendance.getTeamId() != null) {
            PmhubTeam team = teamService.loadEntity(attendance.getTeamId(), tenantId);
            response.setTeamName(team.getTeamName());
        }
        response.setWorkerId(attendance.getWorkerId());
        response.setWorkerName(worker.getWorkerName());
        response.setWorkerPhone(worker.getWorkerPhone());
        response.setIdCardNo(worker.getIdCardNo());
        response.setEntryId(attendance.getEntryId());
        response.setEntryNo(entry.getEntryNo());
        response.setAttendanceStatus(attendance.getAttendanceStatus());
        PmhubWorkerAttendanceStatus attendanceStatus = PmhubWorkerAttendanceStatus.fromCode(attendance.getAttendanceStatus());
        response.setAttendanceStatusLabel(attendanceStatus == null ? null : attendanceStatus.getLabel());
        response.setCheckInAt(attendance.getCheckInAt());
        response.setCheckOutAt(attendance.getCheckOutAt());
        response.setRemark(attendance.getRemark());
        response.setStatus(attendance.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(attendance.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
