package com.hardrockunion.platform.tenant.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "当前用户可进入的 NEXIS 空间分组。")
public class TenantWorkspaceMyResponse {

    @Schema(description = "当前用户直接加入的公司空间。")
    private List<TenantSummaryResponse> organizations = new ArrayList<>();
    @Schema(description = "当前用户直接加入的独立项目。")
    private List<TenantSummaryResponse> independentProjects = new ArrayList<>();
    @Schema(description = "当前用户直接加入且已归属公司的项目。")
    private List<TenantSummaryResponse> joinedProjects = new ArrayList<>();
    @Schema(description = "当前用户所在公司下的直属项目，不要求用户直接加入项目。")
    private List<TenantSummaryResponse> organizationProjects = new ArrayList<>();

    public List<TenantSummaryResponse> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<TenantSummaryResponse> organizations) {
        this.organizations = organizations == null ? new ArrayList<>() : organizations;
    }

    public List<TenantSummaryResponse> getIndependentProjects() {
        return independentProjects;
    }

    public void setIndependentProjects(List<TenantSummaryResponse> independentProjects) {
        this.independentProjects = independentProjects == null ? new ArrayList<>() : independentProjects;
    }

    public List<TenantSummaryResponse> getJoinedProjects() {
        return joinedProjects;
    }

    public void setJoinedProjects(List<TenantSummaryResponse> joinedProjects) {
        this.joinedProjects = joinedProjects == null ? new ArrayList<>() : joinedProjects;
    }

    public List<TenantSummaryResponse> getOrganizationProjects() {
        return organizationProjects;
    }

    public void setOrganizationProjects(List<TenantSummaryResponse> organizationProjects) {
        this.organizationProjects = organizationProjects == null ? new ArrayList<>() : organizationProjects;
    }
}
