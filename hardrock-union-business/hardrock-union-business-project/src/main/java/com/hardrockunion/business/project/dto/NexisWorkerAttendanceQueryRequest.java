package com.hardrockunion.business.project.dto;

import java.time.LocalDate;

import com.hardrockunion.infrastructure.db.page.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisWorkerAttendanceQueryRequest", description = "nexis 工人考勤分页查询条件")
public class NexisWorkerAttendanceQueryRequest extends PageRequest {

    @Schema(description = "项目 ID", example = "20010001")
    private Long projectId;
    @Schema(description = "标段/工地 ID", example = "2001001")
    private Long siteId;
    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;
    @Schema(description = "施工范围 ID", example = "68262140034686979")
    private Long workScopeId;
    @Schema(description = "班组 ID", example = "68270000000000001")
    private Long teamId;
    @Schema(description = "工人 ID", example = "68270000000000002")
    private Long workerId;
    @Schema(description = "实名进场记录 ID", example = "68280000000000001")
    private Long entryId;
    @Schema(description = "考勤日期起", example = "2026-04-06")
    private LocalDate attendanceDateFrom;
    @Schema(description = "考勤日期止", example = "2026-04-06")
    private LocalDate attendanceDateTo;
    @Schema(description = "考勤状态", example = "CHECKED_IN", allowableValues = { "CHECKED_IN", "CHECKED_OUT" })
    private String attendanceStatus;
    @Schema(description = "关键词，匹配考勤单号、工人姓名、手机号、身份证号和班组名称", example = "赵师傅")
    private String keyword;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public Long getWorkScopeId() { return workScopeId; }
    public void setWorkScopeId(Long workScopeId) { this.workScopeId = workScopeId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }
    public LocalDate getAttendanceDateFrom() { return attendanceDateFrom; }
    public void setAttendanceDateFrom(LocalDate attendanceDateFrom) { this.attendanceDateFrom = attendanceDateFrom; }
    public LocalDate getAttendanceDateTo() { return attendanceDateTo; }
    public void setAttendanceDateTo(LocalDate attendanceDateTo) { this.attendanceDateTo = attendanceDateTo; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
