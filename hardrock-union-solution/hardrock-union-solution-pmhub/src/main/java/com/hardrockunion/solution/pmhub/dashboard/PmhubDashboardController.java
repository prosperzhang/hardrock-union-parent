package com.hardrockunion.solution.pmhub.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.pmhub.dashboard.dto.PmhubDashboardOverviewResponse;
import com.hardrockunion.solution.pmhub.facade.PmhubDashboardFacade;

@RestController
@RequestMapping("/api/pmhub/dashboard")
public class PmhubDashboardController {

    private final PmhubDashboardFacade pmhubDashboardFacade;

    public PmhubDashboardController(PmhubDashboardFacade pmhubDashboardFacade) {
        this.pmhubDashboardFacade = pmhubDashboardFacade;
    }

    @GetMapping("/overview")
    public Result<PmhubDashboardOverviewResponse> overview(LoginUser loginUser) {
        return Result.success(pmhubDashboardFacade.overview(loginUser));
    }
}
