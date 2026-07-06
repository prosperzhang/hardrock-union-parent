package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.PmhubProjectParticipantCreateRequest;
import com.hardrockunion.business.project.dto.PmhubProjectParticipantQueryRequest;
import com.hardrockunion.business.project.dto.PmhubProjectParticipantResponse;
import com.hardrockunion.business.project.service.PmhubProjectParticipantService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pmhub/project-participants")
@Tag(name = "PMHUB-项目参建关系", description = "PMHub 项目与参建单位之间的参与关系管理")
public class PmhubProjectParticipantController {

    private final PmhubProjectParticipantService projectParticipantService;

    public PmhubProjectParticipantController(PmhubProjectParticipantService projectParticipantService) {
        this.projectParticipantService = projectParticipantService;
    }

    @Operation(summary = "项目参建关系分页列表", description = "支持按项目、参建单位、角色和关键词分页查看项目参建关系。")
    @GetMapping
    public Result<PageResponse<PmhubProjectParticipantResponse>> list(PmhubProjectParticipantQueryRequest request,
                                                                     LoginUser loginUser) {
        return Result.success(projectParticipantService.list(request, loginUser));
    }

    @Operation(summary = "项目参建关系详情", description = "查看某一条项目参建关系的项目、单位和角色信息。")
    @GetMapping("/{id}")
    public Result<PmhubProjectParticipantResponse> get(@Parameter(description = "项目参建关系 ID", example = "68262140034686977")
                                                      @PathVariable("id") Long id,
                                                      LoginUser loginUser) {
        return Result.success(projectParticipantService.getById(id, loginUser));
    }

    @Operation(summary = "创建项目参建关系", description = "建立某个参建单位与某个项目之间的参与关系，例如把甲劳务公司挂到某个项目下。")
    @PostMapping
    public Result<PmhubProjectParticipantResponse> create(@RequestBody PmhubProjectParticipantCreateRequest request,
                                                         LoginUser loginUser) {
        return Result.success(projectParticipantService.create(request, loginUser));
    }
}
