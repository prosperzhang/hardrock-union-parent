package com.hardrockunion.solution.pmhub.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.project.domain.entity.PmhubSite;
import com.hardrockunion.business.project.domain.entity.PmhubTeam;
import com.hardrockunion.business.project.domain.entity.PmhubWorker;
import com.hardrockunion.business.project.domain.entity.PmhubWorkerAttendance;
import com.hardrockunion.business.project.domain.entity.PmhubWorkerEntry;
import com.hardrockunion.business.project.enums.PmhubWorkerAttendanceStatus;
import com.hardrockunion.business.project.enums.PmhubWorkerEntryStatus;
import com.hardrockunion.business.project.mapper.PmhubSiteMapper;
import com.hardrockunion.business.project.mapper.PmhubTeamMapper;
import com.hardrockunion.business.project.mapper.PmhubWorkerAttendanceMapper;
import com.hardrockunion.business.project.mapper.PmhubWorkerEntryMapper;
import com.hardrockunion.business.project.mapper.PmhubWorkerMapper;
import com.hardrockunion.business.project.service.PmhubProjectLookupService;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.solution.pmhub.dashboard.dto.PmhubAttendanceSnapshotResponse;
import com.hardrockunion.solution.pmhub.dashboard.dto.PmhubSiteSnapshotResponse;

@Service
public class PmhubDashboardAsyncFacade {

    private final PmhubSiteMapper pmhubSiteMapper;
    private final PmhubTeamMapper pmhubTeamMapper;
    private final PmhubWorkerMapper pmhubWorkerMapper;
    private final PmhubWorkerEntryMapper pmhubWorkerEntryMapper;
    private final PmhubWorkerAttendanceMapper pmhubWorkerAttendanceMapper;
    private final PmhubProjectLookupService pmhubProjectLookupService;

    public PmhubDashboardAsyncFacade(PmhubSiteMapper pmhubSiteMapper,
                                     PmhubTeamMapper pmhubTeamMapper,
                                     PmhubWorkerMapper pmhubWorkerMapper,
                                     PmhubWorkerEntryMapper pmhubWorkerEntryMapper,
                                     PmhubWorkerAttendanceMapper pmhubWorkerAttendanceMapper,
                                     PmhubProjectLookupService pmhubProjectLookupService) {
        this.pmhubSiteMapper = pmhubSiteMapper;
        this.pmhubTeamMapper = pmhubTeamMapper;
        this.pmhubWorkerMapper = pmhubWorkerMapper;
        this.pmhubWorkerEntryMapper = pmhubWorkerEntryMapper;
        this.pmhubWorkerAttendanceMapper = pmhubWorkerAttendanceMapper;
        this.pmhubProjectLookupService = pmhubProjectLookupService;
    }

    @Async
    public CompletableFuture<Long> countSites(Long tenantId) {
        long siteCount = pmhubSiteMapper.selectCount(new LambdaQueryWrapper<PmhubSite>()
            .eq(PmhubSite::getTenantId, tenantId)
            .eq(PmhubSite::getDeleted, 0));
        return CompletableFuture.completedFuture(siteCount);
    }

    @Async
    public CompletableFuture<Long> countActiveSites(Long tenantId) {
        long activeSiteCount = pmhubSiteMapper.selectCount(new LambdaQueryWrapper<PmhubSite>()
            .eq(PmhubSite::getTenantId, tenantId)
            .eq(PmhubSite::getDeleted, 0)
            .eq(PmhubSite::getStatus, 1));
        return CompletableFuture.completedFuture(activeSiteCount);
    }

    @Async
    public CompletableFuture<Long> countManagers(Long tenantId) {
        long managerCount = pmhubSiteMapper.selectList(new LambdaQueryWrapper<PmhubSite>()
                .select(PmhubSite::getManagerName)
                .eq(PmhubSite::getTenantId, tenantId)
                .eq(PmhubSite::getDeleted, 0))
            .stream()
            .map(PmhubSite::getManagerName)
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .distinct()
            .count();
        return CompletableFuture.completedFuture(managerCount);
    }

    @Async
    public CompletableFuture<List<PmhubSiteSnapshotResponse>> latestSites(Long tenantId) {
        List<PmhubSiteSnapshotResponse> latestSites = pmhubSiteMapper.selectList(new LambdaQueryWrapper<PmhubSite>()
                .eq(PmhubSite::getTenantId, tenantId)
                .eq(PmhubSite::getDeleted, 0)
                .orderByDesc(PmhubSite::getId)
                .last("limit 3"))
            .stream()
            .map(site -> {
                PmhubSiteSnapshotResponse response = new PmhubSiteSnapshotResponse();
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

    private String resolveProjectName(PmhubSite site) {
        Long projectId = site.getProjectId() == null ? site.getTenantId() : site.getProjectId();
        if (projectId == null) {
            return null;
        }
        try {
            return pmhubProjectLookupService.loadEntity(projectId, site.getTenantId()).getProjectName();
        } catch (BusinessException ex) {
            return null;
        }
    }

    @Async
    public CompletableFuture<Long> countTeams(Long tenantId) {
        long teamCount = pmhubTeamMapper.selectCount(new LambdaQueryWrapper<PmhubTeam>()
            .eq(PmhubTeam::getTenantId, tenantId)
            .eq(PmhubTeam::getDeleted, 0));
        return CompletableFuture.completedFuture(teamCount);
    }

    @Async
    public CompletableFuture<Long> countWorkers(Long tenantId) {
        long workerCount = pmhubWorkerMapper.selectCount(new LambdaQueryWrapper<PmhubWorker>()
            .eq(PmhubWorker::getTenantId, tenantId)
            .eq(PmhubWorker::getDeleted, 0));
        return CompletableFuture.completedFuture(workerCount);
    }

    @Async
    public CompletableFuture<Long> countCurrentEntries(Long tenantId) {
        long entryCount = pmhubWorkerEntryMapper.selectCount(new LambdaQueryWrapper<PmhubWorkerEntry>()
            .eq(PmhubWorkerEntry::getTenantId, tenantId)
            .eq(PmhubWorkerEntry::getDeleted, 0)
            .eq(PmhubWorkerEntry::getEntryStatus, PmhubWorkerEntryStatus.ENTERED.getCode()));
        return CompletableFuture.completedFuture(entryCount);
    }

    @Async
    public CompletableFuture<Long> countTodayAttendances(Long tenantId) {
        long attendanceCount = pmhubWorkerAttendanceMapper.selectCount(new LambdaQueryWrapper<PmhubWorkerAttendance>()
            .eq(PmhubWorkerAttendance::getTenantId, tenantId)
            .eq(PmhubWorkerAttendance::getDeleted, 0)
            .eq(PmhubWorkerAttendance::getAttendanceDate, LocalDate.now()));
        return CompletableFuture.completedFuture(attendanceCount);
    }

    @Async
    public CompletableFuture<Long> countTodayCheckedIn(Long tenantId) {
        long checkedInCount = pmhubWorkerAttendanceMapper.selectCount(new LambdaQueryWrapper<PmhubWorkerAttendance>()
            .eq(PmhubWorkerAttendance::getTenantId, tenantId)
            .eq(PmhubWorkerAttendance::getDeleted, 0)
            .eq(PmhubWorkerAttendance::getAttendanceDate, LocalDate.now())
            .eq(PmhubWorkerAttendance::getAttendanceStatus, PmhubWorkerAttendanceStatus.CHECKED_IN.getCode()));
        return CompletableFuture.completedFuture(checkedInCount);
    }

    @Async
    public CompletableFuture<Long> countTodayCheckedOut(Long tenantId) {
        long checkedOutCount = pmhubWorkerAttendanceMapper.selectCount(new LambdaQueryWrapper<PmhubWorkerAttendance>()
            .eq(PmhubWorkerAttendance::getTenantId, tenantId)
            .eq(PmhubWorkerAttendance::getDeleted, 0)
            .eq(PmhubWorkerAttendance::getAttendanceDate, LocalDate.now())
            .eq(PmhubWorkerAttendance::getAttendanceStatus, PmhubWorkerAttendanceStatus.CHECKED_OUT.getCode()));
        return CompletableFuture.completedFuture(checkedOutCount);
    }

    @Async
    public CompletableFuture<List<PmhubAttendanceSnapshotResponse>> latestAttendances(Long tenantId) {
        List<PmhubSite> sites = pmhubSiteMapper.selectList(new LambdaQueryWrapper<PmhubSite>()
            .select(PmhubSite::getId, PmhubSite::getSiteName)
            .eq(PmhubSite::getTenantId, tenantId)
            .eq(PmhubSite::getDeleted, 0));
        List<PmhubTeam> teams = pmhubTeamMapper.selectList(new LambdaQueryWrapper<PmhubTeam>()
            .select(PmhubTeam::getId, PmhubTeam::getTeamName)
            .eq(PmhubTeam::getTenantId, tenantId)
            .eq(PmhubTeam::getDeleted, 0));
        List<PmhubWorker> workers = pmhubWorkerMapper.selectList(new LambdaQueryWrapper<PmhubWorker>()
            .select(PmhubWorker::getId, PmhubWorker::getWorkerName)
            .eq(PmhubWorker::getTenantId, tenantId)
            .eq(PmhubWorker::getDeleted, 0));
        List<PmhubAttendanceSnapshotResponse> latestAttendances = pmhubWorkerAttendanceMapper.selectList(new LambdaQueryWrapper<PmhubWorkerAttendance>()
                .eq(PmhubWorkerAttendance::getTenantId, tenantId)
                .eq(PmhubWorkerAttendance::getDeleted, 0)
                .orderByDesc(PmhubWorkerAttendance::getAttendanceDate)
                .orderByDesc(PmhubWorkerAttendance::getId)
                .last("limit 5"))
            .stream()
            .map(attendance -> {
                PmhubAttendanceSnapshotResponse response = new PmhubAttendanceSnapshotResponse();
                response.setId(attendance.getId());
                response.setAttendanceNo(attendance.getAttendanceNo());
                response.setAttendanceDate(attendance.getAttendanceDate());
                response.setSiteName(sites.stream()
                    .filter(item -> item.getId().equals(attendance.getSiteId()))
                    .map(PmhubSite::getSiteName)
                    .findFirst()
                    .orElse(null));
                response.setTeamName(teams.stream()
                    .filter(item -> item.getId().equals(attendance.getTeamId()))
                    .map(PmhubTeam::getTeamName)
                    .findFirst()
                    .orElse(null));
                response.setWorkerName(workers.stream()
                    .filter(item -> item.getId().equals(attendance.getWorkerId()))
                    .map(PmhubWorker::getWorkerName)
                    .findFirst()
                    .orElse(null));
                response.setAttendanceStatus(attendance.getAttendanceStatus());
                PmhubWorkerAttendanceStatus attendanceStatus = PmhubWorkerAttendanceStatus.fromCode(attendance.getAttendanceStatus());
                response.setAttendanceStatusLabel(attendanceStatus == null ? null : attendanceStatus.getLabel());
                response.setCheckInAt(attendance.getCheckInAt());
                response.setCheckOutAt(attendance.getCheckOutAt());
                return response;
            })
            .toList();
        return CompletableFuture.completedFuture(latestAttendances);
    }
}
