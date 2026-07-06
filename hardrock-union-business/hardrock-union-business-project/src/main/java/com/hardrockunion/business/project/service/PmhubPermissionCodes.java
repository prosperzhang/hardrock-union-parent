package com.hardrockunion.business.project.service;

/**
 * PMHub 权限编码常量。
 *
 * <p>项目域里只保留一小组稳定的权限码，供 service 层做显式授权判断。
 */
public final class PmhubPermissionCodes {

    public static final String PROJECT_MANAGE = "PMHUB_PROJECT_MANAGE";
    public static final String PROJECT_MEMBER_MANAGE = "PMHUB_PROJECT_MEMBER_MANAGE";
    public static final String WORKER_MANAGE = "PMHUB_WORKER_MANAGE";
    public static final String ATTENDANCE_MANAGE = "PMHUB_ATTENDANCE_MANAGE";
    public static final String ONBOARDING_MANAGE = "PMHUB_ONBOARDING_MANAGE";

    private PmhubPermissionCodes() {
    }
}
