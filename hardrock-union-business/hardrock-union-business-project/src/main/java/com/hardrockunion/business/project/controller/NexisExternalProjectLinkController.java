package com.hardrockunion.business.project.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.NexisExternalProjectLinkCreateRequest;
import com.hardrockunion.business.project.dto.NexisExternalProjectLinkResponse;
import com.hardrockunion.business.project.dto.NexisExternalProjectLinkReviewRequest;
import com.hardrockunion.business.project.dto.NexisExternalProjectOptionResponse;
import com.hardrockunion.business.project.service.NexisExternalProjectLinkService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/nexis/external-project-links")
@Tag(name = "NEXIS-外部项目认领", description = "外部上级单位未入驻时先施工，入驻后由双方确认并关联正式项目")
public class NexisExternalProjectLinkController {

    private final NexisExternalProjectLinkService linkService;

    public NexisExternalProjectLinkController(NexisExternalProjectLinkService linkService) {
        this.linkService = linkService;
    }

    @Operation(summary = "当前承包项目发起的关联记录")
    @GetMapping("/outgoing")
    public Result<List<NexisExternalProjectLinkResponse>> outgoing(LoginUser loginUser) {
        return Result.success(linkService.listOutgoing(loginUser));
    }

    @Operation(summary = "当前公司待审核的外部项目关联")
    @GetMapping("/incoming")
    public Result<List<NexisExternalProjectLinkResponse>> incoming(LoginUser loginUser) {
        return Result.success(linkService.listIncoming(loginUser));
    }

    @Operation(summary = "当前正式项目已经接入的承包段")
    @GetMapping("/linked-contracts")
    public Result<List<NexisExternalProjectLinkResponse>> linkedContracts(LoginUser loginUser) {
        return Result.success(linkService.listLinkedContracts(loginUser));
    }

    @Operation(summary = "当前公司可用于审核关联的正式项目")
    @GetMapping("/reviewable-projects")
    public Result<List<NexisExternalProjectOptionResponse>> reviewableProjects(LoginUser loginUser) {
        return Result.success(linkService.listReviewableProjects(loginUser));
    }

    @Operation(summary = "发起外部上级单位和项目认领申请")
    @PostMapping
    public Result<NexisExternalProjectLinkResponse> create(@RequestBody NexisExternalProjectLinkCreateRequest request,
                                                          LoginUser loginUser) {
        return Result.success(linkService.create(request, loginUser));
    }

    @Operation(summary = "审核外部项目关联", description = "上级公司管理员审核；通过时必须选择自己的正式项目。")
    @PostMapping("/{id}/review")
    public Result<NexisExternalProjectLinkResponse> review(@Parameter(description = "关联申请ID") @PathVariable("id") Long id,
                                                          @RequestBody NexisExternalProjectLinkReviewRequest request,
                                                          LoginUser loginUser) {
        return Result.success(linkService.review(id, request, loginUser));
    }

    @Operation(summary = "撤回待审核关联申请")
    @PostMapping("/{id}/cancel")
    public Result<NexisExternalProjectLinkResponse> cancel(@PathVariable("id") Long id, LoginUser loginUser) {
        return Result.success(linkService.cancel(id, loginUser));
    }

    @Operation(summary = "解除已生效的项目关联", description = "双方管理员均可解除。不会删除承包项目及其历史数据。")
    @PostMapping("/{id}/unlink")
    public Result<NexisExternalProjectLinkResponse> unlink(@PathVariable("id") Long id, LoginUser loginUser) {
        return Result.success(linkService.unlink(id, loginUser));
    }
}
