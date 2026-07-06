package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.PmhubParticipantCompanyCreateRequest;
import com.hardrockunion.business.project.dto.PmhubParticipantCompanyQueryRequest;
import com.hardrockunion.business.project.dto.PmhubParticipantCompanyResponse;
import com.hardrockunion.business.project.service.PmhubParticipantCompanyService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pmhub/participant-companies")
@Tag(name = "PMHUB-参建单位", description = "PMHub 项目参建单位主数据管理，支持平台租户单位和外部单位")
public class PmhubParticipantCompanyController {

    private final PmhubParticipantCompanyService participantCompanyService;

    public PmhubParticipantCompanyController(PmhubParticipantCompanyService participantCompanyService) {
        this.participantCompanyService = participantCompanyService;
    }

    @Operation(summary = "参建单位分页列表", description = "支持按单位类型、绑定租户和关键词分页查看当前 PMHub 租户下维护的参建单位。")
    @GetMapping
    public Result<PageResponse<PmhubParticipantCompanyResponse>> list(PmhubParticipantCompanyQueryRequest request,
                                                                    LoginUser loginUser) {
        return Result.success(participantCompanyService.list(request, loginUser));
    }

    @Operation(summary = "参建单位详情", description = "查看单个参建单位的基础信息、单位类型和租户绑定情况。")
    @GetMapping("/{id}")
    public Result<PmhubParticipantCompanyResponse> get(@Parameter(description = "参建单位 ID", example = "68262140034686976")
                                                      @PathVariable("id") Long id,
                                                      LoginUser loginUser) {
        return Result.success(participantCompanyService.getById(id, loginUser));
    }

    @Operation(summary = "创建参建单位", description = "新增一个项目参建单位，可用于表示总包、专业分包、劳务分包或外部合作单位。")
    @PostMapping
    public Result<PmhubParticipantCompanyResponse> create(@RequestBody PmhubParticipantCompanyCreateRequest request,
                                                         LoginUser loginUser) {
        return Result.success(participantCompanyService.create(request, loginUser));
    }
}
