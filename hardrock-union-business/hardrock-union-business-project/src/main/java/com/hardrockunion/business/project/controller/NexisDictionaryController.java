package com.hardrockunion.business.project.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.NexisDictionaryOptionResponse;
import com.hardrockunion.business.project.service.NexisDictionaryService;
import com.hardrockunion.framework.core.domain.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/nexis/dictionaries")
@Tag(name = "NEXIS-字典", description = "Nexis 前端下拉和枚举字典查询")
public class NexisDictionaryController {

    private final NexisDictionaryService dictionaryService;

    public NexisDictionaryController(NexisDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Operation(summary = "参建单位类型字典", description = "返回总包、专业分包、劳务分包、供应商等参建单位类型选项。")
    @GetMapping("/participant-company-types")
    public Result<List<NexisDictionaryOptionResponse>> participantCompanyTypes() {
        return Result.success(dictionaryService.listParticipantCompanyTypes());
    }

    @Operation(summary = "参建角色字典", description = "返回项目/标段参建关系中可使用的角色选项。")
    @GetMapping("/participant-roles")
    public Result<List<NexisDictionaryOptionResponse>> participantRoles() {
        return Result.success(dictionaryService.listParticipantRoles());
    }

    @Operation(summary = "施工范围类型字典", description = "返回施工范围类型选项，例如编码区间、里程区间和楼层区间。")
    @GetMapping("/site-work-scope-types")
    public Result<List<NexisDictionaryOptionResponse>> siteWorkScopeTypes() {
        return Result.success(dictionaryService.listSiteWorkScopeTypes());
    }

    @Operation(summary = "通用状态字典", description = "返回 Nexis 侧通用启停状态选项，当前 1 表示启用，0 表示停用。")
    @GetMapping("/record-statuses")
    public Result<List<NexisDictionaryOptionResponse>> recordStatuses() {
        return Result.success(dictionaryService.listRecordStatuses());
    }

    @Operation(summary = "实名状态字典", description = "返回工人实名登记状态选项，例如已实名、未实名。")
    @GetMapping("/real-name-statuses")
    public Result<List<NexisDictionaryOptionResponse>> realNameStatuses() {
        return Result.success(dictionaryService.listRealNameStatuses());
    }

    @Operation(summary = "实名进场状态字典", description = "返回实名进场记录状态选项，例如已登记、已进场、已退场。")
    @GetMapping("/worker-entry-statuses")
    public Result<List<NexisDictionaryOptionResponse>> workerEntryStatuses() {
        return Result.success(dictionaryService.listWorkerEntryStatuses());
    }

    @Operation(summary = "工人考勤状态字典", description = "返回工人考勤记录状态选项，例如已签到、已签退。")
    @GetMapping("/worker-attendance-statuses")
    public Result<List<NexisDictionaryOptionResponse>> workerAttendanceStatuses() {
        return Result.success(dictionaryService.listWorkerAttendanceStatuses());
    }
}
