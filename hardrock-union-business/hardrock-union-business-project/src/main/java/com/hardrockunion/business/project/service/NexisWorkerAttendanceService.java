package com.hardrockunion.business.project.service;

import java.time.LocalDate;
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
import com.hardrockunion.business.project.domain.entity.NexisWorkerAttendance;
import com.hardrockunion.business.project.domain.entity.NexisWorkerEntry;
import com.hardrockunion.business.project.dto.NexisWorkerAttendanceActionRequest;
import com.hardrockunion.business.project.dto.NexisWorkerAttendanceCheckInRequest;
import com.hardrockunion.business.project.dto.NexisWorkerAttendanceQueryRequest;
import com.hardrockunion.business.project.dto.NexisWorkerAttendanceResponse;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.enums.NexisWorkerAttendanceStatus;
import com.hardrockunion.business.project.enums.NexisWorkerEntryStatus;
import com.hardrockunion.business.project.mapper.NexisWorkerAttendanceMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class NexisWorkerAttendanceService {

    private final NexisWorkerAttendanceMapper workerAttendanceMapper;
    private final NexisWorkerEntryService workerEntryService;
    private final NexisWorkerService workerService;
    private final NexisProjectLookupService projectLookupService;
    private final NexisSiteService siteService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final NexisSiteWorkScopeService siteWorkScopeService;
    private final NexisTeamService teamService;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisWorkerAttendanceService(NexisWorkerAttendanceMapper workerAttendanceMapper,
                                       NexisWorkerEntryService workerEntryService,
                                       NexisWorkerService workerService,
                                       NexisProjectLookupService projectLookupService,
                                       NexisSiteService siteService,
                                       NexisParticipantCompanyService participantCompanyService,
                                       NexisSiteWorkScopeService siteWorkScopeService,
                                       NexisTeamService teamService,
                                       NexisAccessGuard nexisAccessGuard) {
        this.workerAttendanceMapper = workerAttendanceMapper;
        this.workerEntryService = workerEntryService;
        this.workerService = workerService;
        this.projectLookupService = projectLookupService;
        this.siteService = siteService;
        this.participantCompanyService = participantCompanyService;
        this.siteWorkScopeService = siteWorkScopeService;
        this.teamService = teamService;
        this.nexisAccessGuard = nexisAccessGuard;
    }

    public PageResponse<NexisWorkerAttendanceResponse> list(NexisWorkerAttendanceQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisWorkerAttendanceQueryRequest query = request == null ? new NexisWorkerAttendanceQueryRequest() : request;
        if (query.getAttendanceDateFrom() != null && query.getAttendanceDateTo() != null
            && query.getAttendanceDateFrom().isAfter(query.getAttendanceDateTo())) {
            throw new BusinessException("attendanceDateFrom 不能晚于 attendanceDateTo");
        }
        LambdaQueryWrapper<NexisWorkerAttendance> wrapper = new LambdaQueryWrapper<NexisWorkerAttendance>()
            .eq(NexisWorkerAttendance::getTenantId, loginUser.getTenantId())
            .eq(NexisWorkerAttendance::getDeleted, 0);
        if (nexisAccessGuard.hasRole(loginUser, "NEXIS_TEAM_LEADER")) {
            List<Long> teamIds = teamService.findIdsByLeader(loginUser.getTenantId(), loginUser.getUserId());
            if (teamIds.isEmpty()) wrapper.eq(NexisWorkerAttendance::getTeamId, -1L);
            else wrapper.in(NexisWorkerAttendance::getTeamId, teamIds);
        }
        if (query.getProjectId() != null) {
            wrapper.eq(NexisWorkerAttendance::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(NexisWorkerAttendance::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(NexisWorkerAttendance::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (query.getWorkScopeId() != null) {
            wrapper.eq(NexisWorkerAttendance::getWorkScopeId, query.getWorkScopeId());
        }
        if (query.getTeamId() != null) {
            wrapper.eq(NexisWorkerAttendance::getTeamId, query.getTeamId());
        }
        if (query.getWorkerId() != null) {
            wrapper.eq(NexisWorkerAttendance::getWorkerId, query.getWorkerId());
        }
        if (query.getEntryId() != null) {
            wrapper.eq(NexisWorkerAttendance::getEntryId, query.getEntryId());
        }
        if (query.getAttendanceDateFrom() != null) {
            wrapper.ge(NexisWorkerAttendance::getAttendanceDate, query.getAttendanceDateFrom());
        }
        if (query.getAttendanceDateTo() != null) {
            wrapper.le(NexisWorkerAttendance::getAttendanceDate, query.getAttendanceDateTo());
        }
        if (StringUtils.isNotBlank(query.getAttendanceStatus())) {
            NexisWorkerAttendanceStatus attendanceStatus = NexisWorkerAttendanceStatus.fromCode(query.getAttendanceStatus());
            if (attendanceStatus == null) {
                throw new BusinessException("attendanceStatus 非法");
            }
            wrapper.eq(NexisWorkerAttendance::getAttendanceStatus, attendanceStatus.getCode());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            List<Long> workerIds = workerService.findIdsByKeyword(keyword, loginUser.getTenantId());
            List<Long> teamIds = teamService.findIdsByKeyword(keyword, loginUser.getTenantId());
            wrapper.and(w -> {
                w.like(NexisWorkerAttendance::getAttendanceNo, keyword);
                if (!workerIds.isEmpty()) {
                    w.or().in(NexisWorkerAttendance::getWorkerId, workerIds);
                }
                if (!teamIds.isEmpty()) {
                    w.or().in(NexisWorkerAttendance::getTeamId, teamIds);
                }
            });
        }
        wrapper.orderByDesc(NexisWorkerAttendance::getAttendanceDate)
            .orderByDesc(NexisWorkerAttendance::getId);
        Page<NexisWorkerAttendance> page = workerAttendanceMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public NexisWorkerAttendanceResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisWorkerAttendance attendance = loadEntity(id, loginUser.getTenantId());
        ensureAttendanceAccess(attendance, loginUser);
        return toResponse(attendance, loginUser.getTenantId());
    }

    public NexisWorkerAttendanceResponse checkIn(NexisWorkerAttendanceCheckInRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.ATTENDANCE_MANAGE);
        if (request == null || request.getEntryId() == null) {
            throw new BusinessException("entryId 不能为空");
        }
        NexisWorkerEntry entry = workerEntryService.loadEntity(request.getEntryId(), loginUser.getTenantId());
        workerEntryService.ensureEntryAccess(entry, loginUser);
        if (!NexisWorkerEntryStatus.ENTERED.getCode().equals(entry.getEntryStatus())) {
            throw new BusinessException("当前实名进场记录不处于已进场状态，无法签到");
        }
        ensureNoAttendanceForToday(entry.getWorkerId(), loginUser.getTenantId());

        NexisWorkerAttendance attendance = new NexisWorkerAttendance();
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
        attendance.setAttendanceStatus(NexisWorkerAttendanceStatus.CHECKED_IN.getCode());
        attendance.setCheckInAt(LocalDateTime.now());
        attendance.setRemark(StringUtils.trimToNull(request.getRemark()));
        attendance.setStatus(1);
        attendance.setDeleted(0);
        attendance.setCreatedBy(loginUser.getUserId());
        workerAttendanceMapper.insert(attendance);
        return getById(attendance.getId(), loginUser);
    }

    public NexisWorkerAttendanceResponse checkOut(Long id, NexisWorkerAttendanceActionRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.ATTENDANCE_MANAGE);
        NexisWorkerAttendance attendance = loadEntity(id, loginUser.getTenantId());
        ensureAttendanceAccess(attendance, loginUser);
        if (!NexisWorkerAttendanceStatus.CHECKED_IN.getCode().equals(attendance.getAttendanceStatus())) {
            throw new BusinessException("当前考勤记录不允许签退");
        }
        attendance.setAttendanceStatus(NexisWorkerAttendanceStatus.CHECKED_OUT.getCode());
        attendance.setCheckOutAt(LocalDateTime.now());
        attendance.setRemark(StringUtils.trimToNull(request == null ? null : request.getRemark()));
        workerAttendanceMapper.updateById(attendance);
        return getById(id, loginUser);
    }

    public NexisWorkerAttendance loadEntity(Long id, Long tenantId) {
        NexisWorkerAttendance attendance = workerAttendanceMapper.selectOne(new LambdaQueryWrapper<NexisWorkerAttendance>()
            .eq(NexisWorkerAttendance::getId, id)
            .eq(NexisWorkerAttendance::getTenantId, tenantId)
            .eq(NexisWorkerAttendance::getDeleted, 0)
            .last("limit 1"));
        if (attendance == null) {
            throw new BusinessException("考勤记录不存在");
        }
        return attendance;
    }

    private void ensureAttendanceAccess(NexisWorkerAttendance attendance, LoginUser loginUser) {
        if (attendance.getTeamId() == null && nexisAccessGuard.hasRole(loginUser, "NEXIS_TEAM_LEADER")) {
            throw new BusinessException("班组长不能访问未分班组考勤");
        }
        if (attendance.getTeamId() != null) {
            teamService.ensureCanAccessTeam(
                teamService.loadEntity(attendance.getTeamId(), loginUser.getTenantId()), loginUser);
        }
    }

    private void ensureNoAttendanceForToday(Long workerId, Long tenantId) {
        Long count = workerAttendanceMapper.selectCount(new LambdaQueryWrapper<NexisWorkerAttendance>()
            .eq(NexisWorkerAttendance::getTenantId, tenantId)
            .eq(NexisWorkerAttendance::getWorkerId, workerId)
            .eq(NexisWorkerAttendance::getAttendanceDate, LocalDate.now())
            .eq(NexisWorkerAttendance::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BusinessException("该工人今天已完成签到");
        }
    }

    private String generateAttendanceNo(Long tenantId, Long workerId) {
        return "ATT" + tenantId + System.currentTimeMillis() + workerId;
    }

    private NexisWorkerAttendanceResponse toResponse(NexisWorkerAttendance attendance, Long tenantId) {
        NexisProject project = projectLookupService.loadEntity(attendance.getProjectId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(attendance.getParticipantCompanyId(), tenantId);
        NexisWorker worker = workerService.loadEntity(attendance.getWorkerId(), tenantId);
        NexisWorkerEntry entry = workerEntryService.loadEntity(attendance.getEntryId(), tenantId);
        NexisWorkerAttendanceResponse response = new NexisWorkerAttendanceResponse();
        response.setId(attendance.getId());
        response.setTenantId(attendance.getTenantId());
        response.setAttendanceNo(attendance.getAttendanceNo());
        response.setAttendanceDate(attendance.getAttendanceDate());
        response.setProjectId(attendance.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(attendance.getSiteId());
        if (attendance.getSiteId() != null) {
            NexisSite site = siteService.loadEntity(attendance.getSiteId(), tenantId);
            response.setSiteName(site.getSiteName());
        }
        response.setParticipantCompanyId(attendance.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setWorkScopeId(attendance.getWorkScopeId());
        if (attendance.getWorkScopeId() != null) {
            NexisSiteWorkScope workScope = siteWorkScopeService.loadEntity(attendance.getWorkScopeId(), tenantId);
            response.setWorkScopeName(StringUtils.defaultIfBlank(workScope.getScopeName(), workScope.getScopeStartCode()));
        }
        response.setTeamId(attendance.getTeamId());
        if (attendance.getTeamId() != null) {
            NexisTeam team = teamService.loadEntity(attendance.getTeamId(), tenantId);
            response.setTeamName(team.getTeamName());
        }
        response.setWorkerId(attendance.getWorkerId());
        response.setWorkerName(worker.getWorkerName());
        response.setWorkerPhone(worker.getWorkerPhone());
        response.setIdCardNo(worker.getIdCardNo());
        response.setEntryId(attendance.getEntryId());
        response.setEntryNo(entry.getEntryNo());
        response.setAttendanceStatus(attendance.getAttendanceStatus());
        NexisWorkerAttendanceStatus attendanceStatus = NexisWorkerAttendanceStatus.fromCode(attendance.getAttendanceStatus());
        response.setAttendanceStatusLabel(attendanceStatus == null ? null : attendanceStatus.getLabel());
        response.setCheckInAt(attendance.getCheckInAt());
        response.setCheckOutAt(attendance.getCheckOutAt());
        response.setRemark(attendance.getRemark());
        response.setStatus(attendance.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(attendance.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
