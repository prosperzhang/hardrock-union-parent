package com.hardrockunion.business.project.service;

/**
 * Nexis 权限编码常量。
 *
 * <p>项目域里只保留一小组稳定的权限码，供 service 层做显式授权判断。
 */
public final class NexisPermissionCodes {

    public static final String PROJECT_MANAGE = "NEXIS_PROJECT_MANAGE";
    public static final String PROJECT_MEMBER_MANAGE = "NEXIS_PROJECT_MEMBER_MANAGE";
    public static final String PARTICIPANT_MANAGE = "NEXIS_PARTICIPANT_MANAGE";
    public static final String TEAM_MANAGE = "NEXIS_TEAM_MANAGE";
    public static final String WORKER_MANAGE = "NEXIS_WORKER_MANAGE";
    public static final String ATTENDANCE_MANAGE = "NEXIS_ATTENDANCE_MANAGE";
    public static final String ONBOARDING_MANAGE = "NEXIS_ONBOARDING_MANAGE";
    public static final String MATERIAL_MANAGE = "NEXIS_MATERIAL_MANAGE";

    private NexisPermissionCodes() {
    }
}
