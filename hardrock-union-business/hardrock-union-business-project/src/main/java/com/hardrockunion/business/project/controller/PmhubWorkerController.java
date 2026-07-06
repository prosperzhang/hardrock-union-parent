package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.PmhubWorkerCreateRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerQueryRequest;
import com.hardrockunion.business.project.dto.PmhubWorkerResponse;
import com.hardrockunion.business.project.service.PmhubWorkerService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pmhub/workers")
@Tag(name = "PMHUB-工人", description = "PMHub 工人管理，工人可挂在班组、施工范围和参建单位下面")
public class PmhubWorkerController {

    private final PmhubWorkerService workerService;

    public PmhubWorkerController(PmhubWorkerService workerService) {
        this.workerService = workerService;
    }

    @Operation(summary = "工人分页列表", description = "支持按项目、标段、参建单位、施工范围、班组和关键词分页查看工人。")
    @GetMapping
    public Result<PageResponse<PmhubWorkerResponse>> list(PmhubWorkerQueryRequest request, LoginUser loginUser) {
        return Result.success(workerService.list(request, loginUser));
    }

    @Operation(summary = "工人详情", description = "查看单个工人的项目、标段、班组、施工范围和工种信息。")
    @GetMapping("/{id}")
    public Result<PmhubWorkerResponse> get(@Parameter(description = "工人 ID", example = "68270000000000002")
                                          @PathVariable("id") Long id,
                                          LoginUser loginUser) {
        return Result.success(workerService.getById(id, loginUser));
    }

    @Operation(summary = "创建工人", description = "在某个标段、参建单位、施工范围或班组下创建工人，用于表达现场实际作业人员。")
    @PostMapping
    public Result<PmhubWorkerResponse> create(@RequestBody PmhubWorkerCreateRequest request, LoginUser loginUser) {
        return Result.success(workerService.create(request, loginUser));
    }
}
