package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.PmhubWorkerEntryActionRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerEntryCreateRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerEntryQueryRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerEntryResponse;
import com.hardrockunion.business.project.service.PmhubWorkerEntryService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pmhub/worker-entries")
@Tag(name = "PMHUB-实名进场", description = "PMHub 工人实名进场管理，用于承接实名登记、进场和退场流程")
public class PmhubWorkerEntryController {

    private final PmhubWorkerEntryService workerEntryService;

    public PmhubWorkerEntryController(PmhubWorkerEntryService workerEntryService) {
        this.workerEntryService = workerEntryService;
    }

    @Operation(summary = "实名进场分页列表", description = "支持按项目、标段、参建单位、施工范围、班组、工人和实名进场状态分页查询。")
    @GetMapping
    public Result<PageResponse<PmhubWorkerEntryResponse>> list(PmhubWorkerEntryQueryRequest request, LoginUser loginUser) {
        return Result.success(workerEntryService.list(request, loginUser));
    }

    @Operation(summary = "实名进场详情", description = "查看单个实名进场记录的项目、标段、班组、工人和进退场状态。")
    @GetMapping("/{id}")
    public Result<PmhubWorkerEntryResponse> get(@Parameter(description = "实名进场记录 ID", example = "68280000000000001")
                                               @PathVariable("id") Long id,
                                               LoginUser loginUser) {
        return Result.success(workerEntryService.getById(id, loginUser));
    }

    @Operation(summary = "登记实名进场", description = "为已完成实名登记的工人创建实名进场记录，默认状态为已登记。")
    @PostMapping
    public Result<PmhubWorkerEntryResponse> create(@RequestBody PmhubWorkerEntryCreateRequest request, LoginUser loginUser) {
        return Result.success(workerEntryService.create(request, loginUser));
    }

    @Operation(summary = "确认进场", description = "将已登记的实名进场记录推进到已进场状态，并记录实际进场时间。")
    @PostMapping("/{id}/enter")
    public Result<PmhubWorkerEntryResponse> enter(@Parameter(description = "实名进场记录 ID", example = "68280000000000001")
                                                 @PathVariable("id") Long id,
                                                 @RequestBody(required = false) PmhubWorkerEntryActionRequest request,
                                                 LoginUser loginUser) {
        return Result.success(workerEntryService.enter(id, request, loginUser));
    }

    @Operation(summary = "确认退场", description = "将已进场的实名进场记录推进到已退场状态，并记录实际退场时间。")
    @PostMapping("/{id}/exit")
    public Result<PmhubWorkerEntryResponse> exit(@Parameter(description = "实名进场记录 ID", example = "68280000000000001")
                                                @PathVariable("id") Long id,
                                                @RequestBody(required = false) PmhubWorkerEntryActionRequest request,
                                                LoginUser loginUser) {
        return Result.success(workerEntryService.exit(id, request, loginUser));
    }
}
