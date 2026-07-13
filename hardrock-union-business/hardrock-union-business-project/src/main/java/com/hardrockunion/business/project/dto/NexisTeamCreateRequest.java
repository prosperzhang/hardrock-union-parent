package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisTeamCreateRequest", description = "nexis 班组创建请求")
public class NexisTeamCreateRequest {

    @Schema(description = "标段/工地 ID，可为空，仅作为项目内施工位置维度", example = "2001001")
    private Long siteId;
    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;
    @Schema(description = "施工范围 ID，可为空，仅作为项目内施工范围维度", example = "68262140034686979")
    private Long workScopeId;
    @Schema(description = "班组名称", example = "甲劳务 A 段一班")
    private String teamName;
    @Schema(description = "班组编码", example = "TEAM-A-001")
    private String teamCode;
    @Schema(description = "班组长姓名", example = "李班长")
    private String leaderName;
    @Schema(description = "班组长手机号", example = "13800000002")
    private String leaderPhone;
    @Schema(description = "班组长 Nexis 用户 ID；该用户必须是当前项目成员并具备班组长角色", example = "103884809234948118")
    private Long leaderUserId;

    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public Long getWorkScopeId() { return workScopeId; }
    public void setWorkScopeId(Long workScopeId) { this.workScopeId = workScopeId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamCode() { return teamCode; }
    public void setTeamCode(String teamCode) { this.teamCode = teamCode; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public String getLeaderPhone() { return leaderPhone; }
    public void setLeaderPhone(String leaderPhone) { this.leaderPhone = leaderPhone; }
    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
}
