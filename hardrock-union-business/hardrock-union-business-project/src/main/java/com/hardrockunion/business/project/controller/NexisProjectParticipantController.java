package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.NexisProjectParticipantCreateRequest;
import com.hardrockunion.business.project.dto.NexisProjectParticipantQueryRequest;
import com.hardrockunion.business.project.dto.NexisProjectParticipantResponse;
import com.hardrockunion.business.project.service.NexisProjectParticipantService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/nexis/project-participants")
@Tag(name = "NEXIS-项目参建关系", description = "Nexis 项目与参建单位之间的参与关系管理")
public class NexisProjectParticipantController {

    private final NexisProjectParticipantService projectParticipantService;

    public NexisProjectParticipantController(NexisProjectParticipantService projectParticipantService) {
        this.projectParticipantService = projectParticipantService;
    }

    @Operation(summary = "项目参建关系分页列表", description = "支持按项目、参建单位、角色和关键词分页查看项目参建关系。")
    @GetMapping
    public Result<PageResponse<NexisProjectParticipantResponse>> list(NexisProjectParticipantQueryRequest request,
                                                                     LoginUser loginUser) {
        return Result.success(projectParticipantService.list(request, loginUser));
    }

    @Operation(summary = "项目参建关系详情", description = "查看某一条项目参建关系的项目、单位和角色信息。")
    @GetMapping("/{id}")
    public Result<NexisProjectParticipantResponse> get(@Parameter(description = "项目参建关系 ID", example = "68262140034686977")
                                                      @PathVariable("id") Long id,
                                                      LoginUser loginUser) {
        return Result.success(projectParticipantService.getById(id, loginUser));
    }

    @Operation(summary = "创建项目参建关系", description = "建立某个参建单位与某个项目之间的参与关系，例如把甲劳务公司挂到某个项目下。")
    @PostMapping
    public Result<NexisProjectParticipantResponse> create(@RequestBody NexisProjectParticipantCreateRequest request,
                                                         LoginUser loginUser) {
        return Result.success(projectParticipantService.create(request, loginUser));
    }
}
