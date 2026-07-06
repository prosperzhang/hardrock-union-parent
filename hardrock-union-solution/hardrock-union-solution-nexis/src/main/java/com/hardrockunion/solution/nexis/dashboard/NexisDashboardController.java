package com.hardrockunion.solution.nexis.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.nexis.dashboard.dto.NexisDashboardOverviewResponse;
import com.hardrockunion.solution.nexis.facade.NexisDashboardFacade;

@RestController
@RequestMapping("/api/nexis/dashboard")
public class NexisDashboardController {

    private final NexisDashboardFacade nexisDashboardFacade;

    public NexisDashboardController(NexisDashboardFacade nexisDashboardFacade) {
        this.nexisDashboardFacade = nexisDashboardFacade;
    }

    @GetMapping("/overview")
    public Result<NexisDashboardOverviewResponse> overview(LoginUser loginUser) {
        return Result.success(nexisDashboardFacade.overview(loginUser));
    }
}
