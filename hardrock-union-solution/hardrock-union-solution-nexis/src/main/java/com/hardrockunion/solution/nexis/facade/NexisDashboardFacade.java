package com.hardrockunion.solution.nexis.facade;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.hardrockunion.business.project.service.NexisAccessGuard;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.nexis.dashboard.dto.NexisAttendanceSnapshotResponse;
import com.hardrockunion.solution.nexis.dashboard.dto.NexisDashboardOverviewResponse;
import com.hardrockunion.solution.nexis.dashboard.dto.NexisSiteSnapshotResponse;

@Service
public class NexisDashboardFacade {

    private final NexisAccessGuard nexisAccessGuard;
    private final NexisDashboardAsyncFacade nexisDashboardAsyncFacade;

    public NexisDashboardFacade(NexisAccessGuard nexisAccessGuard, NexisDashboardAsyncFacade nexisDashboardAsyncFacade) {
        this.nexisAccessGuard = nexisAccessGuard;
        this.nexisDashboardAsyncFacade = nexisDashboardAsyncFacade;
    }

    public NexisDashboardOverviewResponse overview(LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);

        CompletableFuture<Long> siteCountFuture = nexisDashboardAsyncFacade.countSites(loginUser.getTenantId());
        CompletableFuture<Long> activeSiteCountFuture = nexisDashboardAsyncFacade.countActiveSites(loginUser.getTenantId());
        CompletableFuture<Long> managerCountFuture = nexisDashboardAsyncFacade.countManagers(loginUser.getTenantId());
        CompletableFuture<Long> teamCountFuture = nexisDashboardAsyncFacade.countTeams(loginUser.getTenantId());
        CompletableFuture<Long> workerCountFuture = nexisDashboardAsyncFacade.countWorkers(loginUser.getTenantId());
        CompletableFuture<Long> currentEntryCountFuture = nexisDashboardAsyncFacade.countCurrentEntries(loginUser.getTenantId());
        CompletableFuture<Long> todayAttendanceCountFuture = nexisDashboardAsyncFacade.countTodayAttendances(loginUser.getTenantId());
        CompletableFuture<Long> todayCheckedInCountFuture = nexisDashboardAsyncFacade.countTodayCheckedIn(loginUser.getTenantId());
        CompletableFuture<Long> todayCheckedOutCountFuture = nexisDashboardAsyncFacade.countTodayCheckedOut(loginUser.getTenantId());
        CompletableFuture<List<NexisSiteSnapshotResponse>> latestSitesFuture = nexisDashboardAsyncFacade.latestSites(loginUser.getTenantId());
        CompletableFuture<List<NexisAttendanceSnapshotResponse>> latestAttendancesFuture = nexisDashboardAsyncFacade.latestAttendances(loginUser.getTenantId());

        NexisDashboardOverviewResponse response = new NexisDashboardOverviewResponse();
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
