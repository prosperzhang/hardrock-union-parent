package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.NexisParticipantCompanyBindRequest;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyCreateRequest;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyQueryRequest;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyResponse;
import com.hardrockunion.business.project.service.NexisParticipantCompanyService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/nexis/participant-companies")
@Tag(name = "NEXIS-参建单位", description = "Nexis 项目参建单位主数据管理，支持平台租户单位和外部单位")
public class NexisParticipantCompanyController {

    private final NexisParticipantCompanyService participantCompanyService;

    public NexisParticipantCompanyController(NexisParticipantCompanyService participantCompanyService) {
        this.participantCompanyService = participantCompanyService;
    }

    @Operation(summary = "参建单位分页列表", description = "支持按单位类型、绑定租户和关键词分页查看当前 Nexis 租户下维护的参建单位。")
    @GetMapping
    public Result<PageResponse<NexisParticipantCompanyResponse>> list(NexisParticipantCompanyQueryRequest request,
                                                                    LoginUser loginUser) {
        return Result.success(participantCompanyService.list(request, loginUser));
    }

    @Operation(summary = "参建单位详情", description = "查看单个参建单位的基础信息、单位类型和租户绑定情况。")
    @GetMapping("/{id}")
    public Result<NexisParticipantCompanyResponse> get(@Parameter(description = "参建单位 ID", example = "68262140034686976")
                                                      @PathVariable("id") Long id,
                                                      LoginUser loginUser) {
        return Result.success(participantCompanyService.getById(id, loginUser));
    }

    @Operation(summary = "创建参建单位", description = "新增一个项目参建单位，可用于表示总包、专业分包、劳务分包或外部合作单位。")
    @PostMapping
    public Result<NexisParticipantCompanyResponse> create(@RequestBody NexisParticipantCompanyCreateRequest request,
                                                         LoginUser loginUser) {
        return Result.success(participantCompanyService.create(request, loginUser));
    }

    @Operation(summary = "绑定真实 Nexis 租户", description = "把当前项目下的外部参建单位绑定到已经入驻 Nexis 的公司租户。绑定不改变历史数据归属。")
    @PostMapping("/{id}/bind-tenant")
    public Result<NexisParticipantCompanyResponse> bindTenant(@Parameter(description = "参建单位 ID", example = "68262140034686976")
                                                              @PathVariable("id") Long id,
                                                              @RequestBody NexisParticipantCompanyBindRequest request,
                                                              LoginUser loginUser) {
        return Result.success(participantCompanyService.bindTenant(id, request, loginUser));
    }

    @Operation(summary = "解除真实租户绑定", description = "解除参建单位与真实 Nexis 租户的绑定。历史数据仍保留在当前项目。")
    @PostMapping("/{id}/unbind-tenant")
    public Result<NexisParticipantCompanyResponse> unbindTenant(@Parameter(description = "参建单位 ID", example = "68262140034686976")
                                                                @PathVariable("id") Long id,
                                                                LoginUser loginUser) {
        return Result.success(participantCompanyService.unbindTenant(id, loginUser));
    }
}
