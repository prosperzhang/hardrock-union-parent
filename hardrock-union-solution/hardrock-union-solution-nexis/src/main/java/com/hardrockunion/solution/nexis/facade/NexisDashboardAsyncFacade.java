package com.hardrockunion.solution.nexis.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.project.domain.entity.NexisSite;
import com.hardrockunion.business.project.domain.entity.NexisTeam;
import com.hardrockunion.business.project.domain.entity.NexisWorker;
import com.hardrockunion.business.project.domain.entity.NexisWorkerAttendance;
import com.hardrockunion.business.project.domain.entity.NexisWorkerEntry;
import com.hardrockunion.business.project.enums.NexisWorkerAttendanceStatus;
import com.hardrockunion.business.project.enums.NexisWorkerEntryStatus;
import com.hardrockunion.business.project.mapper.NexisSiteMapper;
import com.hardrockunion.business.project.mapper.NexisTeamMapper;
import com.hardrockunion.business.project.mapper.NexisWorkerAttendanceMapper;
import com.hardrockunion.business.project.mapper.NexisWorkerEntryMapper;
import com.hardrockunion.business.project.mapper.NexisWorkerMapper;
import com.hardrockunion.business.project.service.NexisProjectLookupService;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.solution.nexis.dashboard.dto.NexisAttendanceSnapshotResponse;
import com.hardrockunion.solution.nexis.dashboard.dto.NexisSiteSnapshotResponse;

@Service
public class NexisDashboardAsyncFacade {

    private final NexisSiteMapper nexisSiteMapper;
    private final NexisTeamMapper nexisTeamMapper;
    private final NexisWorkerMapper nexisWorkerMapper;
    private final NexisWorkerEntryMapper nexisWorkerEntryMapper;
    private final NexisWorkerAttendanceMapper nexisWorkerAttendanceMapper;
    private final NexisProjectLookupService nexisProjectLookupService;

    public NexisDashboardAsyncFacade(NexisSiteMapper nexisSiteMapper,
                                     NexisTeamMapper nexisTeamMapper,
                                     NexisWorkerMapper nexisWorkerMapper,
                                     NexisWorkerEntryMapper nexisWorkerEntryMapper,
                                     NexisWorkerAttendanceMapper nexisWorkerAttendanceMapper,
                                     NexisProjectLookupService nexisProjectLookupService) {
        this.nexisSiteMapper = nexisSiteMapper;
        this.nexisTeamMapper = nexisTeamMapper;
        this.nexisWorkerMapper = nexisWorkerMapper;
        this.nexisWorkerEntryMapper = nexisWorkerEntryMapper;
        this.nexisWorkerAttendanceMapper = nexisWorkerAttendanceMapper;
        this.nexisProjectLookupService = nexisProjectLookupService;
    }

    @Async
    public CompletableFuture<Long> countSites(Long tenantId) {
        long siteCount = nexisSiteMapper.selectCount(new LambdaQueryWrapper<NexisSite>()
            .eq(NexisSite::getTenantId, tenantId)
            .eq(NexisSite::getDeleted, 0));
        return CompletableFuture.completedFuture(siteCount);
    }

    @Async
    public CompletableFuture<Long> countActiveSites(Long tenantId) {
        long activeSiteCount = nexisSiteMapper.selectCount(new LambdaQueryWrapper<NexisSite>()
            .eq(NexisSite::getTenantId, tenantId)
            .eq(NexisSite::getDeleted, 0)
            .eq(NexisSite::getStatus, 1));
        return CompletableFuture.completedFuture(activeSiteCount);
    }

    @Async
    public CompletableFuture<Long> countManagers(Long tenantId) {
        long managerCount = nexisSiteMapper.selectList(new LambdaQueryWrapper<NexisSite>()
                .select(NexisSite::getManagerName)
                .eq(NexisSite::getTenantId, tenantId)
                .eq(NexisSite::getDeleted, 0))
            .stream()
            .map(NexisSite::getManagerName)
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .distinct()
            .count();
        return CompletableFuture.completedFuture(managerCount);
    }

    @Async
    public CompletableFuture<List<NexisSiteSnapshotResponse>> latestSites(Long tenantId) {
        List<NexisSiteSnapshotResponse> latestSites = nexisSiteMapper.selectList(new LambdaQueryWrapper<NexisSite>()
                .eq(NexisSite::getTenantId, tenantId)
                .eq(NexisSite::getDeleted, 0)
                .orderByDesc(NexisSite::getId)
                .last("limit 3"))
            .stream()
            .map(site -> {
                NexisSiteSnapshotResponse response = new NexisSiteSnapshotResponse();
                response.setId(site.getId());
                response.setSiteName(site.getSiteName());
                response.setProjectName(resolveProjectName(site));
                response.setManagerName(site.getManagerName());
                response.setStatus(site.getStatus());
                return response;
            })
            .toList();
        return CompletableFuture.completedFuture(latestSites);
    }

    private String resolveProjectName(NexisSite site) {
        Long projectId = site.getProjectId() == null ? site.getTenantId() : site.getProjectId();
        if (projectId == null) {
            return null;
        }
        try {
            return nexisProjectLookupService.loadEntity(projectId, site.getTenantId()).getProjectName();
        } catch (BusinessException ex) {
            return null;
        }
    }

    @Async
    public CompletableFuture<Long> countTeams(Long tenantId) {
        long teamCount = nexisTeamMapper.selectCount(new LambdaQueryWrapper<NexisTeam>()
            .eq(NexisTeam::getTenantId, tenantId)
            .eq(NexisTeam::getDeleted, 0));
        return CompletableFuture.completedFuture(teamCount);
    }

    @Async
    public CompletableFuture<Long> countWorkers(Long tenantId) {
        long workerCount = nexisWorkerMapper.selectCount(new LambdaQueryWrapper<NexisWorker>()
            .eq(NexisWorker::getTenantId, tenantId)
            .eq(NexisWorker::getDeleted, 0));
        return CompletableFuture.completedFuture(workerCount);
    }

    @Async
    public CompletableFuture<Long> countCurrentEntries(Long tenantId) {
        long entryCount = nexisWorkerEntryMapper.selectCount(new LambdaQueryWrapper<NexisWorkerEntry>()
            .eq(NexisWorkerEntry::getTenantId, tenantId)
            .eq(NexisWorkerEntry::getDeleted, 0)
            .eq(NexisWorkerEntry::getEntryStatus, NexisWorkerEntryStatus.ENTERED.getCode()));
        return CompletableFuture.completedFuture(entryCount);
    }

    @Async
    public CompletableFuture<Long> countTodayAttendances(Long tenantId) {
        long attendanceCount = nexisWorkerAttendanceMapper.selectCount(new LambdaQueryWrapper<NexisWorkerAttendance>()
            .eq(NexisWorkerAttendance::getTenantId, tenantId)
            .eq(NexisWorkerAttendance::getDeleted, 0)
            .eq(NexisWorkerAttendance::getAttendanceDate, LocalDate.now()));
        return CompletableFuture.completedFuture(attendanceCount);
    }

    @Async
    public CompletableFuture<Long> countTodayCheckedIn(Long tenantId) {
        long checkedInCount = nexisWorkerAttendanceMapper.selectCount(new LambdaQueryWrapper<NexisWorkerAttendance>()
            .eq(NexisWorkerAttendance::getTenantId, tenantId)
            .eq(NexisWorkerAttendance::getDeleted, 0)
            .eq(NexisWorkerAttendance::getAttendanceDate, LocalDate.now())
            .eq(NexisWorkerAttendance::getAttendanceStatus, NexisWorkerAttendanceStatus.CHECKED_IN.getCode()));
        return CompletableFuture.completedFuture(checkedInCount);
    }

    @Async
    public CompletableFuture<Long> countTodayCheckedOut(Long tenantId) {
        long checkedOutCount = nexisWorkerAttendanceMapper.selectCount(new LambdaQueryWrapper<NexisWorkerAttendance>()
            .eq(NexisWorkerAttendance::getTenantId, tenantId)
            .eq(NexisWorkerAttendance::getDeleted, 0)
            .eq(NexisWorkerAttendance::getAttendanceDate, LocalDate.now())
            .eq(NexisWorkerAttendance::getAttendanceStatus, NexisWorkerAttendanceStatus.CHECKED_OUT.getCode()));
        return CompletableFuture.completedFuture(checkedOutCount);
    }

    @Async
    public CompletableFuture<List<NexisAttendanceSnapshotResponse>> latestAttendances(Long tenantId) {
        List<NexisSite> sites = nexisSiteMapper.selectList(new LambdaQueryWrapper<NexisSite>()
            .select(NexisSite::getId, NexisSite::getSiteName)
            .eq(NexisSite::getTenantId, tenantId)
            .eq(NexisSite::getDeleted, 0));
        List<NexisTeam> teams = nexisTeamMapper.selectList(new LambdaQueryWrapper<NexisTeam>()
            .select(NexisTeam::getId, NexisTeam::getTeamName)
            .eq(NexisTeam::getTenantId, tenantId)
            .eq(NexisTeam::getDeleted, 0));
        List<NexisWorker> workers = nexisWorkerMapper.selectList(new LambdaQueryWrapper<NexisWorker>()
            .select(NexisWorker::getId, NexisWorker::getWorkerName)
            .eq(NexisWorker::getTenantId, tenantId)
            .eq(NexisWorker::getDeleted, 0));
        List<NexisAttendanceSnapshotResponse> latestAttendances = nexisWorkerAttendanceMapper.selectList(new LambdaQueryWrapper<NexisWorkerAttendance>()
                .eq(NexisWorkerAttendance::getTenantId, tenantId)
                .eq(NexisWorkerAttendance::getDeleted, 0)
                .orderByDesc(NexisWorkerAttendance::getAttendanceDate)
                .orderByDesc(NexisWorkerAttendance::getId)
                .last("limit 5"))
            .stream()
            .map(attendance -> {
                NexisAttendanceSnapshotResponse response = new NexisAttendanceSnapshotResponse();
                response.setId(attendance.getId());
                response.setAttendanceNo(attendance.getAttendanceNo());
                response.setAttendanceDate(attendance.getAttendanceDate());
                response.setSiteName(sites.stream()
                    .filter(item -> item.getId().equals(attendance.getSiteId()))
                    .map(NexisSite::getSiteName)
                    .findFirst()
                    .orElse(null));
                response.setTeamName(teams.stream()
                    .filter(item -> item.getId().equals(attendance.getTeamId()))
                    .map(NexisTeam::getTeamName)
                    .findFirst()
                    .orElse(null));
                response.setWorkerName(workers.stream()
                    .filter(item -> item.getId().equals(attendance.getWorkerId()))
                    .map(NexisWorker::getWorkerName)
                    .findFirst()
                    .orElse(null));
                response.setAttendanceStatus(attendance.getAttendanceStatus());
                NexisWorkerAttendanceStatus attendanceStatus = NexisWorkerAttendanceStatus.fromCode(attendance.getAttendanceStatus());
                response.setAttendanceStatusLabel(attendanceStatus == null ? null : attendanceStatus.getLabel());
                response.setCheckInAt(attendance.getCheckInAt());
                response.setCheckOutAt(attendance.getCheckOutAt());
                return response;
            })
            .toList();
        return CompletableFuture.completedFuture(latestAttendances);
    }
}
