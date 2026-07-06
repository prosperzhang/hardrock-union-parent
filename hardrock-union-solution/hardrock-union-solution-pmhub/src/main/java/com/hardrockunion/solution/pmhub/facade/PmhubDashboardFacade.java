package com.hardrockunion.solution.pmhub.facade;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.hardrockunion.business.project.service.PmhubAccessGuard;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.pmhub.dashboard.dto.PmhubAttendanceSnapshotResponse;
import com.hardrockunion.solution.pmhub.dashboard.dto.PmhubDashboardOverviewResponse;
import com.hardrockunion.solution.pmhub.dashboard.dto.PmhubSiteSnapshotResponse;

@Service
public class PmhubDashboardFacade {

    private final PmhubAccessGuard pmhubAccessGuard;
    private final PmhubDashboardAsyncFacade pmhubDashboardAsyncFacade;

    public PmhubDashboardFacade(PmhubAccessGuard pmhubAccessGuard, PmhubDashboardAsyncFacade pmhubDashboardAsyncFacade) {
        this.pmhubAccessGuard = pmhubAccessGuard;
        this.pmhubDashboardAsyncFacade = pmhubDashboardAsyncFacade;
    }

    public PmhubDashboardOverviewResponse overview(LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);

        CompletableFuture<Long> siteCountFuture = pmhubDashboardAsyncFacade.countSites(loginUser.getTenantId());
        CompletableFuture<Long> activeSiteCountFuture = pmhubDashboardAsyncFacade.countActiveSites(loginUser.getTenantId());
        CompletableFuture<Long> managerCountFuture = pmhubDashboardAsyncFacade.countManagers(loginUser.getTenantId());
        CompletableFuture<Long> teamCountFuture = pmhubDashboardAsyncFacade.countTeams(loginUser.getTenantId());
        CompletableFuture<Long> workerCountFuture = pmhubDashboardAsyncFacade.countWorkers(loginUser.getTenantId());
        CompletableFuture<Long> currentEntryCountFuture = pmhubDashboardAsyncFacade.countCurrentEntries(loginUser.getTenantId());
        CompletableFuture<Long> todayAttendanceCountFuture = pmhubDashboardAsyncFacade.countTodayAttendances(loginUser.getTenantId());
        CompletableFuture<Long> todayCheckedInCountFuture = pmhubDashboardAsyncFacade.countTodayCheckedIn(loginUser.getTenantId());
        CompletableFuture<Long> todayCheckedOutCountFuture = pmhubDashboardAsyncFacade.countTodayCheckedOut(loginUser.getTenantId());
        CompletableFuture<List<PmhubSiteSnapshotResponse>> latestSitesFuture = pmhubDashboardAsyncFacade.latestSites(loginUser.getTenantId());
        CompletableFuture<List<PmhubAttendanceSnapshotResponse>> latestAttendancesFuture = pmhubDashboardAsyncFacade.latestAttendances(loginUser.getTenantId());

        PmhubDashboardOverviewResponse response = new PmhubDashboardOverviewResponse();
        response.setTenantId(loginUser.getTenantId());
        response.setSiteCount(siteCountFuture.join());
        response.setActiveSiteCount(activeSiteCountFuture.join());
        response.setManagerCount(managerCountFuture.join());
        response.setTeamCount(teamCountFuture.join());
        response.setWorkerCount(workerCountFuture.join());
        response.setCurrentEntryCount(currentEntryCountFuture.join());
        response.setTodayAttendanceCount(todayAttendanceCountFuture.join());
        response.setTodayCheckedInCount(todayCheckedInCountFuture.join());
        response.setTodayCheckedOutCount(todayCheckedOutCountFuture.join());
        response.setLatestSites(latestSitesFuture.join());
        response.setLatestAttendances(latestAttendancesFuture.join());
        return response;
    }
}
