package com.hardrockunion.solution.primeloadlogistics.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/primeload-logistics/dashboard")
@Tag(name = "PRIMELOAD-LOGISTICS-物流首页", description = "一车好料物流应用首页入口。")
public class PrimeloadLogisticsDashboardController {

    @GetMapping("/overview")
    @Operation(summary = "物流首页概览", description = "返回当前一车好料物流租户的基础登录上下文。")
    public Result<OverviewResponse> overview(LoginUser loginUser) {
        OverviewResponse response = new OverviewResponse();
        response.setAppCode("PRIMELOAD-LOGISTICS");
        response.setTenantId(loginUser.getTenantId());
        response.setUserId(loginUser.getUserId());
        return Result.success(response);
    }

    public static class OverviewResponse {

        private String appCode;

        private Long tenantId;

        private Long userId;

        public String getAppCode() {
            return appCode;
        }

        public void setAppCode(String appCode) {
            this.appCode = appCode;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}
