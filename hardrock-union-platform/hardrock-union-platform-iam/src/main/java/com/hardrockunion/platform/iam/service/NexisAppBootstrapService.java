package com.hardrockunion.platform.iam.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class NexisAppBootstrapService {

    private static final String APP_CODE = "NEXIS";
    private static final String APP_NAME = "矩石";
    private static final String LEGACY_PROJECT_ADMIN_ROLE_CODE = "NEXIS_PROJECT_MANAGER";
    private static final String PROJECT_ADMIN_ROLE_CODE = "NEXIS_PROJECT_ADMIN";
    private static final String PROJECT_MANAGER_ROLE_CODE = "NEXIS_PROJECT_MANAGER";

    private static final List<DepartmentSeed> DEPARTMENTS = List.of(
        new DepartmentSeed("NEXIS_GROUP_MANAGEMENT_DEPT", "集团管理层", "MANAGEMENT", "GROUP", 1),
        new DepartmentSeed("NEXIS_GROUP_STRATEGY_DEPT", "战略管理部", "STRATEGY", "GROUP", 2),
        new DepartmentSeed("NEXIS_GROUP_INVESTMENT_DEPT", "投资发展部", "INVESTMENT", "GROUP", 3),
        new DepartmentSeed("NEXIS_GROUP_AUDIT_DEPT", "审计部", "AUDIT", "GROUP", 4),
        new DepartmentSeed("NEXIS_GROUP_LEGAL_RISK_DEPT", "法务风控部", "LEGAL_RISK", "GROUP", 5),
        new DepartmentSeed("NEXIS_COMPANY_MANAGEMENT_DEPT", "公司管理层", "MANAGEMENT", "COMPANY", 10),
        new DepartmentSeed("NEXIS_COMPANY_LEGAL_DEPT", "法务部", "LEGAL", "COMPANY", 20),
        new DepartmentSeed("NEXIS_COMPANY_AUDIT_DEPT", "审计部", "AUDIT", "COMPANY", 30),
        new DepartmentSeed("NEXIS_ORG_OPERATION_DEPT", "运营管理中心", "OPERATION", "ORGANIZATION", 20),
        new DepartmentSeed("NEXIS_ORG_ENGINEERING_DEPT", "工程管理中心", "ENGINEERING", "ORGANIZATION", 30),
        new DepartmentSeed("NEXIS_ORG_BUSINESS_DEPT", "商务成本中心", "BUSINESS", "ORGANIZATION", 40),
        new DepartmentSeed("NEXIS_ORG_FINANCE_DEPT", "财务部", "FINANCE", "ORGANIZATION", 50),
        new DepartmentSeed("NEXIS_ORG_HR_ADMIN_DEPT", "人力行政部", "HR_ADMIN", "ORGANIZATION", 60),
        new DepartmentSeed("NEXIS_ORG_PROCUREMENT_DEPT", "采购供应部", "PROCUREMENT", "ORGANIZATION", 70),
        new DepartmentSeed("NEXIS_PROJECT_DEPT", "项目管理部", "PROJECT", "PROJECT", 110),
        new DepartmentSeed("NEXIS_ENGINEERING_DEPT", "工程部", "ENGINEERING", "PROJECT", 120),
        new DepartmentSeed("NEXIS_LABOR_DEPT", "劳务管理部", "LABOR", "PROJECT", 130),
        new DepartmentSeed("NEXIS_TECH_QUALITY_DEPT", "技术质量部", "TECH_QUALITY", "PROJECT", 140),
        new DepartmentSeed("NEXIS_SAFETY_ENV_DEPT", "安全环保部", "SAFETY", "PROJECT", 150),
        new DepartmentSeed("NEXIS_BUSINESS_CONTRACT_DEPT", "商务合约部", "BUSINESS", "PROJECT", 160),
        new DepartmentSeed("NEXIS_MATERIAL_EQUIP_DEPT", "物资设备部", "MATERIAL", "PROJECT", 170),
        new DepartmentSeed("NEXIS_GENERAL_ADMIN_DEPT", "综合管理部", "GENERAL_ADMIN", "PROJECT", 180),
        new DepartmentSeed("NEXIS_DOCUMENT_DEPT", "资料部", "DOCUMENT", "PROJECT", 190)
    );

    private static final List<RoleSeed> ROLES = List.of(
        new RoleSeed("NEXIS_GROUP_ADMIN", "集团负责人", true),
        new RoleSeed("NEXIS_GROUP_BOARD_MEMBER", "董事/股东", false),
        new RoleSeed("NEXIS_GROUP_STRATEGY_DIRECTOR", "战略负责人", false),
        new RoleSeed("NEXIS_GROUP_STRATEGY_MANAGER", "战略经理", false),
        new RoleSeed("NEXIS_GROUP_INVESTMENT_DIRECTOR", "投资负责人", false),
        new RoleSeed("NEXIS_GROUP_INVESTMENT_MANAGER", "投资经理", false),
        new RoleSeed("NEXIS_GROUP_AUDIT_DIRECTOR", "集团审计负责人", false),
        new RoleSeed("NEXIS_GROUP_AUDITOR", "集团审计专员", false),
        new RoleSeed("NEXIS_GROUP_LEGAL_RISK_DIRECTOR", "法务风控负责人", false),
        new RoleSeed("NEXIS_GROUP_LEGAL_COUNSEL", "集团法务", false),
        new RoleSeed("NEXIS_GROUP_RISK_MANAGER", "集团风控经理", false),
        new RoleSeed("NEXIS_COMPANY_ADMIN", "公司负责人", true),
        new RoleSeed("NEXIS_COMPANY_DEPUTY_MANAGER", "公司副总经理", false),
        new RoleSeed("NEXIS_COMPANY_LEGAL_DIRECTOR", "公司法务负责人", false),
        new RoleSeed("NEXIS_COMPANY_LEGAL_SPECIALIST", "公司法务专员", false),
        new RoleSeed("NEXIS_COMPANY_AUDIT_DIRECTOR", "公司审计负责人", false),
        new RoleSeed("NEXIS_COMPANY_AUDITOR", "公司审计专员", false),
        new RoleSeed("NEXIS_OPERATION_DIRECTOR", "运营负责人", false),
        new RoleSeed("NEXIS_MULTI_PROJECT_DIRECTOR", "多项目负责人", false),
        new RoleSeed("NEXIS_COMPANY_ENGINEERING_DIRECTOR", "工程管理负责人", false),
        new RoleSeed("NEXIS_PROJECT_SUPERVISOR", "项目督导", false),
        new RoleSeed("NEXIS_COMPANY_BUSINESS_DIRECTOR", "商务成本负责人", false),
        new RoleSeed("NEXIS_COMPANY_COST_MANAGER", "公司成本管理员", false),
        new RoleSeed("NEXIS_FINANCE_DIRECTOR", "财务负责人", false),
        new RoleSeed("NEXIS_ACCOUNTANT", "会计", false),
        new RoleSeed("NEXIS_CASHIER", "出纳", false),
        new RoleSeed("NEXIS_HR_ADMIN_DIRECTOR", "人力行政负责人", false),
        new RoleSeed("NEXIS_HR_SPECIALIST", "人事", false),
        new RoleSeed("NEXIS_ADMIN_SPECIALIST", "行政专员", false),
        new RoleSeed("NEXIS_PROCUREMENT_DIRECTOR", "采购负责人", false),
        new RoleSeed("NEXIS_PURCHASER", "采购员", false),
        new RoleSeed(PROJECT_ADMIN_ROLE_CODE, "项目负责人", true),
        new RoleSeed(PROJECT_MANAGER_ROLE_CODE, "项目经理", false),
        new RoleSeed("NEXIS_PRODUCTION_MANAGER", "生产经理", false),
        new RoleSeed("NEXIS_TECHNICAL_LEADER", "技术负责人", false),
        new RoleSeed("NEXIS_CONSTRUCTION_OFFICER", "施工员", false),
        new RoleSeed("NEXIS_FOREMAN", "工长", false),
        new RoleSeed("NEXIS_CIVIL_ENGINEER", "土建工程师", false),
        new RoleSeed("NEXIS_INSTALLATION_ENGINEER", "安装工程师", false),
        new RoleSeed("NEXIS_DECORATION_ENGINEER", "装饰工程师", false),
        new RoleSeed("NEXIS_MEP_ENGINEER", "机电工程师", false),
        new RoleSeed("NEXIS_LABOR_MANAGER", "劳务负责人", false),
        new RoleSeed("NEXIS_TEAM_LEADER", "班组长", false),
        new RoleSeed("NEXIS_TECHNICIAN", "技术员", false),
        new RoleSeed("NEXIS_QUALITY_OFFICER", "质量员", false),
        new RoleSeed("NEXIS_SURVEYOR", "测量员", false),
        new RoleSeed("NEXIS_TESTER", "试验员", false),
        new RoleSeed("NEXIS_BIM_ENGINEER", "BIM工程师", false),
        new RoleSeed("NEXIS_SAFETY_SUPERVISOR", "安全主管", false),
        new RoleSeed("NEXIS_SAFETY_OFFICER", "安全员", false),
        new RoleSeed("NEXIS_ENVIRONMENTAL_OFFICER", "环保员", false),
        new RoleSeed("NEXIS_CIVILIZED_CONSTRUCTION_ADMIN", "文明施工管理员", false),
        new RoleSeed("NEXIS_BUSINESS_MANAGER", "商务经理", false),
        new RoleSeed("NEXIS_BUDGET_OFFICER", "预算员", false),
        new RoleSeed("NEXIS_COST_ENGINEER", "造价员", false),
        new RoleSeed("NEXIS_CONTRACT_SPECIALIST", "合约专员", false),
        new RoleSeed("NEXIS_COST_MANAGER", "成本管理员", false),
        new RoleSeed("NEXIS_MATERIAL_OFFICER", "材料员", false),
        new RoleSeed("NEXIS_PROJECT_PURCHASER", "项目采购员", false),
        new RoleSeed("NEXIS_MECHANIC", "机械员", false),
        new RoleSeed("NEXIS_EQUIPMENT_ADMIN", "设备管理员", false),
        new RoleSeed("NEXIS_WAREHOUSE_ADMIN", "仓库管理员", false),
        new RoleSeed("NEXIS_OFFICE_DIRECTOR", "办公室主任", false),
        new RoleSeed("NEXIS_PROJECT_ADMIN_SPECIALIST", "项目行政专员", false),
        new RoleSeed("NEXIS_PROJECT_HR_SPECIALIST", "项目人事", false),
        new RoleSeed("NEXIS_LABOR_OFFICER", "劳资员", false),
        new RoleSeed("NEXIS_LOGISTICS_ADMIN", "后勤管理员", false),
        new RoleSeed("NEXIS_CLERK", "文员", false),
        new RoleSeed("NEXIS_DOCUMENT_CLERK", "资料员", false),
        new RoleSeed("NEXIS_ARCHIVE_ADMIN", "档案管理员", false)
    );

    private static final List<PermissionSeed> PERMISSIONS = List.of(
        new PermissionSeed("NEXIS_PROJECT_MANAGE", "项目基础管理", 10),
        new PermissionSeed("NEXIS_PROJECT_MEMBER_MANAGE", "项目成员管理", 20),
        new PermissionSeed("NEXIS_PARTICIPANT_MANAGE", "参建单位管理", 30),
        new PermissionSeed("NEXIS_TEAM_MANAGE", "班组管理", 40),
        new PermissionSeed("NEXIS_WORKER_MANAGE", "工人管理", 50),
        new PermissionSeed("NEXIS_ONBOARDING_MANAGE", "工人进退场管理", 60),
        new PermissionSeed("NEXIS_ATTENDANCE_MANAGE", "考勤管理", 70),
        new PermissionSeed("NEXIS_MATERIAL_MANAGE", "材料业务管理", 80),
        new PermissionSeed("NEXIS_ORGANIZATION_MANAGE", "组织基础管理", 110),
        new PermissionSeed("NEXIS_MULTI_PROJECT_MANAGE", "多项目统筹", 120),
        new PermissionSeed("NEXIS_BUSINESS_COST_MANAGE", "公司商务成本管理", 130),
        new PermissionSeed("NEXIS_FINANCE_MANAGE", "公司财务管理", 140),
        new PermissionSeed("NEXIS_HR_ADMIN_MANAGE", "公司人力行政管理", 150),
        new PermissionSeed("NEXIS_PROCUREMENT_MANAGE", "公司采购供应管理", 160),
        new PermissionSeed("NEXIS_STRATEGY_MANAGE", "集团战略管理", 170),
        new PermissionSeed("NEXIS_INVESTMENT_MANAGE", "集团投资管理", 180),
        new PermissionSeed("NEXIS_AUDIT_MANAGE", "审计管理", 190),
        new PermissionSeed("NEXIS_LEGAL_RISK_MANAGE", "法务风控管理", 200)
    );

    private static final List<DepartmentRoleSeed> DEPARTMENT_ROLES = List.of(
        new DepartmentRoleSeed("NEXIS_GROUP_MANAGEMENT_DEPT", "NEXIS_GROUP_ADMIN"),
        new DepartmentRoleSeed("NEXIS_GROUP_MANAGEMENT_DEPT", "NEXIS_GROUP_BOARD_MEMBER"),
        new DepartmentRoleSeed("NEXIS_GROUP_STRATEGY_DEPT", "NEXIS_GROUP_STRATEGY_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_GROUP_STRATEGY_DEPT", "NEXIS_GROUP_STRATEGY_MANAGER"),
        new DepartmentRoleSeed("NEXIS_GROUP_INVESTMENT_DEPT", "NEXIS_GROUP_INVESTMENT_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_GROUP_INVESTMENT_DEPT", "NEXIS_GROUP_INVESTMENT_MANAGER"),
        new DepartmentRoleSeed("NEXIS_GROUP_AUDIT_DEPT", "NEXIS_GROUP_AUDIT_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_GROUP_AUDIT_DEPT", "NEXIS_GROUP_AUDITOR"),
        new DepartmentRoleSeed("NEXIS_GROUP_LEGAL_RISK_DEPT", "NEXIS_GROUP_LEGAL_RISK_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_GROUP_LEGAL_RISK_DEPT", "NEXIS_GROUP_LEGAL_COUNSEL"),
        new DepartmentRoleSeed("NEXIS_GROUP_LEGAL_RISK_DEPT", "NEXIS_GROUP_RISK_MANAGER"),
        new DepartmentRoleSeed("NEXIS_COMPANY_MANAGEMENT_DEPT", "NEXIS_COMPANY_ADMIN"),
        new DepartmentRoleSeed("NEXIS_COMPANY_MANAGEMENT_DEPT", "NEXIS_COMPANY_DEPUTY_MANAGER"),
        new DepartmentRoleSeed("NEXIS_COMPANY_LEGAL_DEPT", "NEXIS_COMPANY_LEGAL_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_COMPANY_LEGAL_DEPT", "NEXIS_COMPANY_LEGAL_SPECIALIST"),
        new DepartmentRoleSeed("NEXIS_COMPANY_AUDIT_DEPT", "NEXIS_COMPANY_AUDIT_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_COMPANY_AUDIT_DEPT", "NEXIS_COMPANY_AUDITOR"),
        new DepartmentRoleSeed("NEXIS_ORG_OPERATION_DEPT", "NEXIS_OPERATION_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_ORG_OPERATION_DEPT", "NEXIS_MULTI_PROJECT_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_ORG_ENGINEERING_DEPT", "NEXIS_COMPANY_ENGINEERING_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_ORG_ENGINEERING_DEPT", "NEXIS_PROJECT_SUPERVISOR"),
        new DepartmentRoleSeed("NEXIS_ORG_BUSINESS_DEPT", "NEXIS_COMPANY_BUSINESS_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_ORG_BUSINESS_DEPT", "NEXIS_COMPANY_COST_MANAGER"),
        new DepartmentRoleSeed("NEXIS_ORG_FINANCE_DEPT", "NEXIS_FINANCE_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_ORG_FINANCE_DEPT", "NEXIS_ACCOUNTANT"),
        new DepartmentRoleSeed("NEXIS_ORG_FINANCE_DEPT", "NEXIS_CASHIER"),
        new DepartmentRoleSeed("NEXIS_ORG_HR_ADMIN_DEPT", "NEXIS_HR_ADMIN_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_ORG_HR_ADMIN_DEPT", "NEXIS_HR_SPECIALIST"),
        new DepartmentRoleSeed("NEXIS_ORG_HR_ADMIN_DEPT", "NEXIS_ADMIN_SPECIALIST"),
        new DepartmentRoleSeed("NEXIS_ORG_PROCUREMENT_DEPT", "NEXIS_PROCUREMENT_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_ORG_PROCUREMENT_DEPT", "NEXIS_PURCHASER"),
        new DepartmentRoleSeed("NEXIS_PROJECT_DEPT", PROJECT_ADMIN_ROLE_CODE),
        new DepartmentRoleSeed("NEXIS_PROJECT_DEPT", PROJECT_MANAGER_ROLE_CODE),
        new DepartmentRoleSeed("NEXIS_PROJECT_DEPT", "NEXIS_PRODUCTION_MANAGER"),
        new DepartmentRoleSeed("NEXIS_PROJECT_DEPT", "NEXIS_TECHNICAL_LEADER"),
        new DepartmentRoleSeed("NEXIS_ENGINEERING_DEPT", "NEXIS_CONSTRUCTION_OFFICER"),
        new DepartmentRoleSeed("NEXIS_ENGINEERING_DEPT", "NEXIS_FOREMAN"),
        new DepartmentRoleSeed("NEXIS_ENGINEERING_DEPT", "NEXIS_CIVIL_ENGINEER"),
        new DepartmentRoleSeed("NEXIS_ENGINEERING_DEPT", "NEXIS_INSTALLATION_ENGINEER"),
        new DepartmentRoleSeed("NEXIS_ENGINEERING_DEPT", "NEXIS_DECORATION_ENGINEER"),
        new DepartmentRoleSeed("NEXIS_ENGINEERING_DEPT", "NEXIS_MEP_ENGINEER"),
        new DepartmentRoleSeed("NEXIS_LABOR_DEPT", "NEXIS_LABOR_MANAGER"),
        new DepartmentRoleSeed("NEXIS_LABOR_DEPT", "NEXIS_TEAM_LEADER"),
        new DepartmentRoleSeed("NEXIS_TECH_QUALITY_DEPT", "NEXIS_TECHNICIAN"),
        new DepartmentRoleSeed("NEXIS_TECH_QUALITY_DEPT", "NEXIS_QUALITY_OFFICER"),
        new DepartmentRoleSeed("NEXIS_TECH_QUALITY_DEPT", "NEXIS_SURVEYOR"),
        new DepartmentRoleSeed("NEXIS_TECH_QUALITY_DEPT", "NEXIS_TESTER"),
        new DepartmentRoleSeed("NEXIS_TECH_QUALITY_DEPT", "NEXIS_BIM_ENGINEER"),
        new DepartmentRoleSeed("NEXIS_SAFETY_ENV_DEPT", "NEXIS_SAFETY_SUPERVISOR"),
        new DepartmentRoleSeed("NEXIS_SAFETY_ENV_DEPT", "NEXIS_SAFETY_OFFICER"),
        new DepartmentRoleSeed("NEXIS_SAFETY_ENV_DEPT", "NEXIS_ENVIRONMENTAL_OFFICER"),
        new DepartmentRoleSeed("NEXIS_SAFETY_ENV_DEPT", "NEXIS_CIVILIZED_CONSTRUCTION_ADMIN"),
        new DepartmentRoleSeed("NEXIS_BUSINESS_CONTRACT_DEPT", "NEXIS_BUSINESS_MANAGER"),
        new DepartmentRoleSeed("NEXIS_BUSINESS_CONTRACT_DEPT", "NEXIS_BUDGET_OFFICER"),
        new DepartmentRoleSeed("NEXIS_BUSINESS_CONTRACT_DEPT", "NEXIS_COST_ENGINEER"),
        new DepartmentRoleSeed("NEXIS_BUSINESS_CONTRACT_DEPT", "NEXIS_CONTRACT_SPECIALIST"),
        new DepartmentRoleSeed("NEXIS_BUSINESS_CONTRACT_DEPT", "NEXIS_COST_MANAGER"),
        new DepartmentRoleSeed("NEXIS_MATERIAL_EQUIP_DEPT", "NEXIS_MATERIAL_OFFICER"),
        new DepartmentRoleSeed("NEXIS_MATERIAL_EQUIP_DEPT", "NEXIS_PROJECT_PURCHASER"),
        new DepartmentRoleSeed("NEXIS_MATERIAL_EQUIP_DEPT", "NEXIS_MECHANIC"),
        new DepartmentRoleSeed("NEXIS_MATERIAL_EQUIP_DEPT", "NEXIS_EQUIPMENT_ADMIN"),
        new DepartmentRoleSeed("NEXIS_MATERIAL_EQUIP_DEPT", "NEXIS_WAREHOUSE_ADMIN"),
        new DepartmentRoleSeed("NEXIS_GENERAL_ADMIN_DEPT", "NEXIS_OFFICE_DIRECTOR"),
        new DepartmentRoleSeed("NEXIS_GENERAL_ADMIN_DEPT", "NEXIS_PROJECT_ADMIN_SPECIALIST"),
        new DepartmentRoleSeed("NEXIS_GENERAL_ADMIN_DEPT", "NEXIS_PROJECT_HR_SPECIALIST"),
        new DepartmentRoleSeed("NEXIS_GENERAL_ADMIN_DEPT", "NEXIS_LABOR_OFFICER"),
        new DepartmentRoleSeed("NEXIS_GENERAL_ADMIN_DEPT", "NEXIS_LOGISTICS_ADMIN"),
        new DepartmentRoleSeed("NEXIS_GENERAL_ADMIN_DEPT", "NEXIS_CLERK"),
        new DepartmentRoleSeed("NEXIS_DOCUMENT_DEPT", "NEXIS_DOCUMENT_CLERK"),
        new DepartmentRoleSeed("NEXIS_DOCUMENT_DEPT", "NEXIS_ARCHIVE_ADMIN")
    );

    private static final List<RolePermissionSeed> ROLE_PERMISSIONS = List.of(
        new RolePermissionSeed("NEXIS_GROUP_MANAGEMENT_DEPT", "NEXIS_GROUP_ADMIN", List.of(
            "NEXIS_ORGANIZATION_MANAGE", "NEXIS_MULTI_PROJECT_MANAGE", "NEXIS_PROJECT_MEMBER_MANAGE",
            "NEXIS_BUSINESS_COST_MANAGE", "NEXIS_FINANCE_MANAGE", "NEXIS_HR_ADMIN_MANAGE",
            "NEXIS_PROCUREMENT_MANAGE", "NEXIS_STRATEGY_MANAGE", "NEXIS_INVESTMENT_MANAGE",
            "NEXIS_AUDIT_MANAGE", "NEXIS_LEGAL_RISK_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_GROUP_MANAGEMENT_DEPT", "NEXIS_GROUP_BOARD_MEMBER", List.of(
            "NEXIS_ORGANIZATION_MANAGE", "NEXIS_MULTI_PROJECT_MANAGE", "NEXIS_STRATEGY_MANAGE",
            "NEXIS_INVESTMENT_MANAGE", "NEXIS_AUDIT_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_GROUP_STRATEGY_DEPT", "NEXIS_GROUP_STRATEGY_DIRECTOR", List.of("NEXIS_STRATEGY_MANAGE")),
        new RolePermissionSeed("NEXIS_GROUP_STRATEGY_DEPT", "NEXIS_GROUP_STRATEGY_MANAGER", List.of("NEXIS_STRATEGY_MANAGE")),
        new RolePermissionSeed("NEXIS_GROUP_INVESTMENT_DEPT", "NEXIS_GROUP_INVESTMENT_DIRECTOR", List.of("NEXIS_INVESTMENT_MANAGE")),
        new RolePermissionSeed("NEXIS_GROUP_INVESTMENT_DEPT", "NEXIS_GROUP_INVESTMENT_MANAGER", List.of("NEXIS_INVESTMENT_MANAGE")),
        new RolePermissionSeed("NEXIS_GROUP_AUDIT_DEPT", "NEXIS_GROUP_AUDIT_DIRECTOR", List.of("NEXIS_AUDIT_MANAGE")),
        new RolePermissionSeed("NEXIS_GROUP_AUDIT_DEPT", "NEXIS_GROUP_AUDITOR", List.of("NEXIS_AUDIT_MANAGE")),
        new RolePermissionSeed("NEXIS_GROUP_LEGAL_RISK_DEPT", "NEXIS_GROUP_LEGAL_RISK_DIRECTOR", List.of("NEXIS_LEGAL_RISK_MANAGE")),
        new RolePermissionSeed("NEXIS_GROUP_LEGAL_RISK_DEPT", "NEXIS_GROUP_LEGAL_COUNSEL", List.of("NEXIS_LEGAL_RISK_MANAGE")),
        new RolePermissionSeed("NEXIS_GROUP_LEGAL_RISK_DEPT", "NEXIS_GROUP_RISK_MANAGER", List.of("NEXIS_LEGAL_RISK_MANAGE")),
        new RolePermissionSeed("NEXIS_COMPANY_MANAGEMENT_DEPT", "NEXIS_COMPANY_ADMIN", List.of(
            "NEXIS_ORGANIZATION_MANAGE", "NEXIS_MULTI_PROJECT_MANAGE", "NEXIS_PROJECT_MEMBER_MANAGE",
            "NEXIS_BUSINESS_COST_MANAGE", "NEXIS_FINANCE_MANAGE", "NEXIS_HR_ADMIN_MANAGE",
            "NEXIS_PROCUREMENT_MANAGE", "NEXIS_AUDIT_MANAGE", "NEXIS_LEGAL_RISK_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_COMPANY_LEGAL_DEPT", "NEXIS_COMPANY_LEGAL_DIRECTOR", List.of("NEXIS_LEGAL_RISK_MANAGE")),
        new RolePermissionSeed("NEXIS_COMPANY_LEGAL_DEPT", "NEXIS_COMPANY_LEGAL_SPECIALIST", List.of("NEXIS_LEGAL_RISK_MANAGE")),
        new RolePermissionSeed("NEXIS_COMPANY_AUDIT_DEPT", "NEXIS_COMPANY_AUDIT_DIRECTOR", List.of("NEXIS_AUDIT_MANAGE")),
        new RolePermissionSeed("NEXIS_COMPANY_AUDIT_DEPT", "NEXIS_COMPANY_AUDITOR", List.of("NEXIS_AUDIT_MANAGE")),
        new RolePermissionSeed("NEXIS_COMPANY_MANAGEMENT_DEPT", "NEXIS_COMPANY_DEPUTY_MANAGER", List.of(
            "NEXIS_ORGANIZATION_MANAGE", "NEXIS_MULTI_PROJECT_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_OPERATION_DEPT", "NEXIS_OPERATION_DIRECTOR", List.of(
            "NEXIS_ORGANIZATION_MANAGE", "NEXIS_MULTI_PROJECT_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_OPERATION_DEPT", "NEXIS_MULTI_PROJECT_DIRECTOR", List.of(
            "NEXIS_MULTI_PROJECT_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_ENGINEERING_DEPT", "NEXIS_COMPANY_ENGINEERING_DIRECTOR", List.of(
            "NEXIS_MULTI_PROJECT_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_ENGINEERING_DEPT", "NEXIS_PROJECT_SUPERVISOR", List.of(
            "NEXIS_MULTI_PROJECT_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_BUSINESS_DEPT", "NEXIS_COMPANY_BUSINESS_DIRECTOR", List.of(
            "NEXIS_BUSINESS_COST_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_BUSINESS_DEPT", "NEXIS_COMPANY_COST_MANAGER", List.of(
            "NEXIS_BUSINESS_COST_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_FINANCE_DEPT", "NEXIS_FINANCE_DIRECTOR", List.of(
            "NEXIS_FINANCE_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_FINANCE_DEPT", "NEXIS_ACCOUNTANT", List.of(
            "NEXIS_FINANCE_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_FINANCE_DEPT", "NEXIS_CASHIER", List.of(
            "NEXIS_FINANCE_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_HR_ADMIN_DEPT", "NEXIS_HR_ADMIN_DIRECTOR", List.of(
            "NEXIS_HR_ADMIN_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_HR_ADMIN_DEPT", "NEXIS_HR_SPECIALIST", List.of(
            "NEXIS_HR_ADMIN_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_HR_ADMIN_DEPT", "NEXIS_ADMIN_SPECIALIST", List.of(
            "NEXIS_HR_ADMIN_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_PROCUREMENT_DEPT", "NEXIS_PROCUREMENT_DIRECTOR", List.of(
            "NEXIS_PROCUREMENT_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ORG_PROCUREMENT_DEPT", "NEXIS_PURCHASER", List.of(
            "NEXIS_PROCUREMENT_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_PROJECT_DEPT", PROJECT_ADMIN_ROLE_CODE, List.of(
            "NEXIS_PROJECT_MANAGE", "NEXIS_PARTICIPANT_MANAGE", "NEXIS_TEAM_MANAGE",
            "NEXIS_WORKER_MANAGE", "NEXIS_ONBOARDING_MANAGE", "NEXIS_ATTENDANCE_MANAGE",
            "NEXIS_MATERIAL_MANAGE", "NEXIS_PROJECT_MEMBER_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_PROJECT_DEPT", PROJECT_MANAGER_ROLE_CODE, List.of(
            "NEXIS_PROJECT_MANAGE", "NEXIS_PARTICIPANT_MANAGE", "NEXIS_TEAM_MANAGE",
            "NEXIS_WORKER_MANAGE", "NEXIS_ONBOARDING_MANAGE", "NEXIS_ATTENDANCE_MANAGE",
            "NEXIS_MATERIAL_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_ENGINEERING_DEPT", "NEXIS_CONSTRUCTION_OFFICER", List.of(
            "NEXIS_TEAM_MANAGE", "NEXIS_WORKER_MANAGE", "NEXIS_ONBOARDING_MANAGE",
            "NEXIS_ATTENDANCE_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_LABOR_DEPT", "NEXIS_LABOR_MANAGER", List.of(
            "NEXIS_TEAM_MANAGE", "NEXIS_WORKER_MANAGE", "NEXIS_ONBOARDING_MANAGE",
            "NEXIS_ATTENDANCE_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_LABOR_DEPT", "NEXIS_TEAM_LEADER", List.of(
            "NEXIS_WORKER_MANAGE", "NEXIS_ONBOARDING_MANAGE", "NEXIS_ATTENDANCE_MANAGE"
        )),
        new RolePermissionSeed("NEXIS_MATERIAL_EQUIP_DEPT", "NEXIS_MATERIAL_OFFICER", List.of(
            "NEXIS_MATERIAL_MANAGE"
        ))
    );

    private final JdbcTemplate jdbcTemplate;

    public NexisAppBootstrapService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void bootstrap() {
        if (!tableExists("app_registry")) {
            return;
        }
        ensureDepartmentScopeColumn();
        ensureApp();
        Long appId = findAppId();
        if (appId == null) {
            return;
        }
        migrateProjectAdminRole(appId);
        DEPARTMENTS.forEach(seed -> ensureDepartment(appId, seed));
        ROLES.forEach(seed -> ensureRole(appId, seed));
        PERMISSIONS.forEach(seed -> ensurePermission(appId, seed));
        DEPARTMENT_ROLES.forEach(seed -> ensureDepartmentRoleOption(appId, seed));
        ROLE_PERMISSIONS.forEach(seed -> ensureRolePermissions(appId, seed));
        reconcileDepartmentRoleOptions(appId);
    }

    private void migrateProjectAdminRole(Long appId) {
        if (!tableExists("iam_role")) {
            return;
        }
        Long legacyRoleId = findRoleIdByCodeAndName(
            appId, LEGACY_PROJECT_ADMIN_ROLE_CODE, "项目负责人");
        if (legacyRoleId == null) {
            return;
        }
        Long projectAdminRoleId = findId("iam_role", "app_id", appId, "role_code", PROJECT_ADMIN_ROLE_CODE);
        if (projectAdminRoleId == null) {
            jdbcTemplate.update("""
                UPDATE iam_role
                SET role_code = ?, role_name = '项目负责人', status = 1, assignable = 1,
                    admin_role = 1, deleted = 0
                WHERE id = ?
                """, PROJECT_ADMIN_ROLE_CODE, legacyRoleId);
            return;
        }
        jdbcTemplate.update("""
            UPDATE iam_role
            SET role_name = '项目经理', status = 1, assignable = 1, admin_role = 0, deleted = 0
            WHERE id = ?
            """, legacyRoleId);
    }

    private void ensureDepartmentScopeColumn() {
        if (!tableExists("iam_department") || columnExists("iam_department", "workspace_scope")) {
            return;
        }
        jdbcTemplate.execute("""
            ALTER TABLE iam_department
            ADD COLUMN workspace_scope VARCHAR(32) NOT NULL DEFAULT 'ALL'
            COMMENT '适用空间：ALL/GROUP/COMPANY/ORGANIZATION/PROJECT' AFTER dept_type
            """);
    }

    private void ensureApp() {
        if (findAppId() == null) {
            jdbcTemplate.update("""
                INSERT INTO app_registry (app_code, app_name, app_type, home_path, login_path, icon, sort_no, status, description, deleted)
                VALUES (?, ?, 'PROJECT_PLATFORM', '/nexis', '/nexis/login', NULL, 20, 1, ?, 0)
                """, APP_CODE, APP_NAME, "矩石 NEXIS 项目应用");
            return;
        }
        jdbcTemplate.update("""
            UPDATE app_registry
            SET app_name = ?, app_type = COALESCE(app_type, 'PROJECT_PLATFORM'),
                home_path = COALESCE(home_path, '/nexis'), login_path = COALESCE(login_path, '/nexis/login'),
                description = COALESCE(description, ?), sort_no = CASE WHEN sort_no = 0 THEN 20 ELSE sort_no END,
                status = 1, deleted = 0
            WHERE app_code = ?
            """, APP_NAME, "矩石 NEXIS 项目应用", APP_CODE);
    }

    private void ensureDepartment(Long appId, DepartmentSeed seed) {
        if (!tableExists("iam_department")) {
            return;
        }
        if (exists("iam_department", "app_id", appId, "dept_code", seed.code())) {
            jdbcTemplate.update("""
                UPDATE iam_department
                SET dept_name = ?, dept_short_name = ?, dept_type = ?, workspace_scope = ?,
                    status = 1, sort_no = ?, deleted = 0
                WHERE app_id = ? AND dept_code = ?
                """, seed.name(), seed.name(), seed.type(), seed.workspaceScope(), seed.sortNo(), appId, seed.code());
            return;
        }
        jdbcTemplate.update("""
            INSERT INTO iam_department (app_id, app_code, dept_code, dept_name, dept_short_name, parent_id,
                                        dept_type, workspace_scope, status, sort_no, deleted)
            VALUES (?, ?, ?, ?, ?, 0, ?, ?, 1, ?, 0)
            """, appId, APP_CODE, seed.code(), seed.name(), seed.name(), seed.type(), seed.workspaceScope(), seed.sortNo());
    }

    private void ensureRole(Long appId, RoleSeed seed) {
        if (!tableExists("iam_role")) {
            return;
        }
        if (exists("iam_role", "app_id", appId, "role_code", seed.code())) {
            jdbcTemplate.update("""
                UPDATE iam_role
                SET role_name = ?, status = 1, assignable = 1, admin_role = ?, deleted = 0
                WHERE app_id = ? AND role_code = ?
                """, seed.name(), seed.admin() ? 1 : 0, appId, seed.code());
            return;
        }
        jdbcTemplate.update("""
            INSERT INTO iam_role (app_id, app_code, role_code, role_name, status, assignable, admin_role, deleted)
            VALUES (?, ?, ?, ?, 1, 1, ?, 0)
            """, appId, APP_CODE, seed.code(), seed.name(), seed.admin() ? 1 : 0);
    }

    private void ensurePermission(Long appId, PermissionSeed seed) {
        if (!tableExists("iam_permission") || exists("iam_permission", "app_id", appId, "permission_code", seed.code())) {
            return;
        }
        jdbcTemplate.update("""
            INSERT INTO iam_permission (app_id, app_code, permission_code, permission_name, permission_type,
                                        parent_id, permission_path, http_method, component, status, sort_no, deleted)
            VALUES (?, ?, ?, ?, 'API', 0, NULL, NULL, NULL, 1, ?, 0)
            """, appId, APP_CODE, seed.code(), seed.name(), seed.sortNo());
    }

    private void ensureDepartmentRoleOption(Long appId, DepartmentRoleSeed seed) {
        if (!tableExists("iam_department_role_option")) {
            return;
        }
        Long departmentId = findId("iam_department", "app_id", appId, "dept_code", seed.departmentCode());
        Long roleId = findId("iam_role", "app_id", appId, "role_code", seed.roleCode());
        if (departmentId == null || roleId == null || relationExists(
            "iam_department_role_option", appId, departmentId, roleId, null)) {
            return;
        }
        jdbcTemplate.update("""
            INSERT INTO iam_department_role_option (app_id, app_code, department_id, role_id, deleted)
            VALUES (?, ?, ?, ?, 0)
            """, appId, APP_CODE, departmentId, roleId);
    }

    private void ensureRolePermissions(Long appId, RolePermissionSeed seed) {
        if (!tableExists("iam_department_role_permission")) {
            return;
        }
        Long departmentId = findId("iam_department", "app_id", appId, "dept_code", seed.departmentCode());
        Long roleId = findId("iam_role", "app_id", appId, "role_code", seed.roleCode());
        if (departmentId == null || roleId == null) {
            return;
        }
        for (String permissionCode : seed.permissionCodes()) {
            Long permissionId = findId("iam_permission", "app_id", appId, "permission_code", permissionCode);
            if (permissionId == null || relationExists(
                "iam_department_role_permission", appId, departmentId, roleId, permissionId)) {
                continue;
            }
            jdbcTemplate.update("""
                INSERT INTO iam_department_role_permission (app_id, app_code, department_id, role_id, permission_id, deleted)
                VALUES (?, ?, ?, ?, ?, 0)
                """, appId, APP_CODE, departmentId, roleId, permissionId);
        }
    }

    private boolean relationExists(String tableName,
                                   Long appId,
                                   Long departmentId,
                                   Long roleId,
                                   Long permissionId) {
        String permissionClause = permissionId == null ? "" : " AND permission_id = ?";
        Object[] args = permissionId == null
            ? new Object[]{appId, departmentId, roleId}
            : new Object[]{appId, departmentId, roleId, permissionId};
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM `" + tableName + "` WHERE app_id = ? AND department_id = ? AND role_id = ?"
                + permissionClause + " AND deleted = 0",
            Integer.class,
            args
        );
        return count != null && count > 0;
    }

    private Long findAppId() {
        return jdbcTemplate.query("SELECT id FROM app_registry WHERE app_code = ? AND deleted = 0 LIMIT 1",
            rs -> rs.next() ? rs.getLong("id") : null, APP_CODE);
    }

    private Long findId(String tableName, String appIdColumn, Long appId, String codeColumn, String codeValue) {
        if (!tableExists(tableName)) {
            return null;
        }
        return jdbcTemplate.query("SELECT id FROM `" + tableName + "` WHERE `" + appIdColumn + "` = ? AND `"
                + codeColumn + "` = ? AND deleted = 0 LIMIT 1",
            rs -> rs.next() ? rs.getLong("id") : null, appId, codeValue);
    }

    private Long findRoleIdByCodeAndName(Long appId, String roleCode, String roleName) {
        if (!tableExists("iam_role")) {
            return null;
        }
        return jdbcTemplate.query("""
                SELECT id FROM iam_role
                WHERE app_id = ? AND role_code = ? AND role_name = ? AND deleted = 0
                LIMIT 1
                """,
            rs -> rs.next() ? rs.getLong("id") : null, appId, roleCode, roleName);
    }

    private boolean exists(String tableName, String appIdColumn, Long appId, String codeColumn, String codeValue) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `" + tableName + "` WHERE `"
                + appIdColumn + "` = ? AND `" + codeColumn + "` = ? AND deleted = 0",
            Integer.class, appId, codeValue);
        return count != null && count > 0;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
            """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
            """, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }

    private void reconcileDepartmentRoleOptions(Long appId) {
        Map<String, List<String>> roleCodesByDepartment = DEPARTMENT_ROLES.stream()
            .collect(Collectors.groupingBy(
                DepartmentRoleSeed::departmentCode,
                Collectors.mapping(DepartmentRoleSeed::roleCode, Collectors.toList())
            ));
        roleCodesByDepartment.forEach((departmentCode, roleCodes) -> {
            Long departmentId = findId("iam_department", "app_id", appId, "dept_code", departmentCode);
            if (departmentId == null) {
                return;
            }
            List<Long> desiredRoleIds = roleCodes.stream()
                .map(roleCode -> findId("iam_role", "app_id", appId, "role_code", roleCode))
                .filter(java.util.Objects::nonNull)
                .toList();
            List<Long> existingRoleIds = jdbcTemplate.queryForList("""
                SELECT role_id FROM iam_department_role_option
                WHERE app_id = ? AND department_id = ? AND deleted = 0
                """, Long.class, appId, departmentId);
            existingRoleIds.stream()
                .filter(roleId -> !desiredRoleIds.contains(roleId))
                .forEach(roleId -> {
                    jdbcTemplate.update("DELETE FROM iam_department_role_permission WHERE app_id = ? AND department_id = ? AND role_id = ?",
                        appId, departmentId, roleId);
                    jdbcTemplate.update("DELETE FROM iam_department_role_option WHERE app_id = ? AND department_id = ? AND role_id = ?",
                        appId, departmentId, roleId);
                });
        });
    }

    private record DepartmentSeed(String code, String name, String type, String workspaceScope, int sortNo) {
    }

    private record RoleSeed(String code, String name, boolean admin) {
    }

    private record PermissionSeed(String code, String name, int sortNo) {
    }

    private record DepartmentRoleSeed(String departmentCode, String roleCode) {
    }

    private record RolePermissionSeed(String departmentCode, String roleCode, List<String> permissionCodes) {
    }
}
