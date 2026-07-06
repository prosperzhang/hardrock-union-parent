package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.NexisTeamCreateRequest;
import com.hardrockunion.business.project.dto.NexisTeamQueryRequest;
import com.hardrockunion.business.project.dto.NexisTeamResponse;
import com.hardrockunion.business.project.service.NexisTeamService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/nexis/teams")
@Tag(name = "NEXIS-班组", description = "Nexis 班组管理，班组可挂在项目、标段、参建单位和施工范围下面")
public class NexisTeamController {

    private final NexisTeamService teamService;

    public NexisTeamController(NexisTeamService teamService) {
        this.teamService = teamService;
    }

    @Operation(summary = "班组分页列表", description = "支持按项目、标段、参建单位、施工范围和关键词分页查看班组。")
    @GetMapping
    public Result<PageResponse<NexisTeamResponse>> list(NexisTeamQueryRequest request, LoginUser loginUser) {
        return Result.success(teamService.list(request, loginUser));
    }

    @Operation(summary = "班组详情", description = "查看单个班组的项目、标段、参建单位、施工范围和班组长信息。")
    @GetMapping("/{id}")
    public Result<NexisTeamResponse> get(@Parameter(description = "班组 ID", example = "68270000000000001")
                                        @PathVariable("id") Long id,
                                        LoginUser loginUser) {
        return Result.success(teamService.getById(id, loginUser));
    }

    @Operation(summary = "创建班组", description = "在某个标段、参建单位和施工范围下创建班组，用于承接现场执行单元。")
    @PostMapping
    public Result<NexisTeamResponse> create(@RequestBody NexisTeamCreateRequest request, LoginUser loginUser) {
        return Result.success(teamService.create(request, loginUser));
    }
}
