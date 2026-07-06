package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.NexisSiteParticipantCreateRequest;
import com.hardrockunion.business.project.dto.NexisSiteParticipantQueryRequest;
import com.hardrockunion.business.project.dto.NexisSiteParticipantResponse;
import com.hardrockunion.business.project.service.NexisSiteParticipantService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/nexis/site-participants")
@Tag(name = "NEXIS-标段参建关系", description = "Nexis 标段/工地与参建单位之间的参与关系管理")
public class NexisSiteParticipantController {

    private final NexisSiteParticipantService siteParticipantService;

    public NexisSiteParticipantController(NexisSiteParticipantService siteParticipantService) {
        this.siteParticipantService = siteParticipantService;
    }

    @Operation(summary = "标段参建关系分页列表", description = "支持按项目、标段、参建单位、角色和关键词分页查看标段参建关系。")
    @GetMapping
    public Result<PageResponse<NexisSiteParticipantResponse>> list(NexisSiteParticipantQueryRequest request,
                                                                  LoginUser loginUser) {
        return Result.success(siteParticipantService.list(request, loginUser));
    }

    @Operation(summary = "标段参建关系详情", description = "查看某一条标段参建关系的项目、标段、单位和角色信息。")
    @GetMapping("/{id}")
    public Result<NexisSiteParticipantResponse> get(@Parameter(description = "标段参建关系 ID", example = "68262140034686978")
                                                   @PathVariable("id") Long id,
                                                   LoginUser loginUser) {
        return Result.success(siteParticipantService.getById(id, loginUser));
    }

    @Operation(summary = "创建标段参建关系", description = "建立某个参建单位与某个标段之间的参与关系，例如甲劳务公司参与 A 段。")
    @PostMapping
    public Result<NexisSiteParticipantResponse> create(@RequestBody NexisSiteParticipantCreateRequest request,
                                                      LoginUser loginUser) {
        return Result.success(siteParticipantService.create(request, loginUser));
    }
}
