package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.NexisWorkerAttendanceActionRequest;
import com.hardrockunion.business.project.dto.NexisWorkerAttendanceCheckInRequest;
import com.hardrockunion.business.project.dto.NexisWorkerAttendanceQueryRequest;
import com.hardrockunion.business.project.dto.NexisWorkerAttendanceResponse;
import com.hardrockunion.business.project.service.NexisWorkerAttendanceService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/nexis/worker-attendances")
@Tag(name = "NEXIS-工人考勤", description = "Nexis 工人考勤管理，基于实名进场记录承接签到和签退流程")
public class NexisWorkerAttendanceController {

    private final NexisWorkerAttendanceService workerAttendanceService;

    public NexisWorkerAttendanceController(NexisWorkerAttendanceService workerAttendanceService) {
        this.workerAttendanceService = workerAttendanceService;
    }

    @Operation(summary = "工人考勤分页列表", description = "支持按项目、标段、参建单位、施工范围、班组、工人、日期和考勤状态分页查询。")
    @GetMapping
    public Result<PageResponse<NexisWorkerAttendanceResponse>> list(NexisWorkerAttendanceQueryRequest request, LoginUser loginUser) {
        return Result.success(workerAttendanceService.list(request, loginUser));
    }

    @Operation(summary = "工人考勤详情", description = "查看单条工人考勤记录的签到、签退和实名进场关联信息。")
    @GetMapping("/{id}")
    public Result<NexisWorkerAttendanceResponse> get(@Parameter(description = "考勤记录 ID", example = "68290000000000001")
                                                    @PathVariable("id") Long id,
                                                    LoginUser loginUser) {
        return Result.success(workerAttendanceService.getById(id, loginUser));
    }

    @Operation(summary = "工人签到", description = "基于已进场的实名进场记录创建当天考勤记录，默认状态为已签到。")
    @PostMapping("/check-in")
    public Result<NexisWorkerAttendanceResponse> checkIn(@RequestBody NexisWorkerAttendanceCheckInRequest request,
                                                        LoginUser loginUser) {
        return Result.success(workerAttendanceService.checkIn(request, loginUser));
    }

    @Operation(summary = "工人签退", description = "将当天已签到的考勤记录推进到已签退状态，并记录签退时间。")
    @PostMapping("/{id}/check-out")
    public Result<NexisWorkerAttendanceResponse> checkOut(@Parameter(description = "考勤记录 ID", example = "68290000000000001")
                                                         @PathVariable("id") Long id,
                                                         @RequestBody(required = false) NexisWorkerAttendanceActionRequest request,
                                                         LoginUser loginUser) {
        return Result.success(workerAttendanceService.checkOut(id, request, loginUser));
    }
}
