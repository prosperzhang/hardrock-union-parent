package com.hardrockunion.business.project.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.project.domain.entity.NexisExternalProjectLink;
import com.hardrockunion.business.project.domain.entity.NexisParticipantCompany;
import com.hardrockunion.business.project.dto.NexisExternalProjectLinkCreateRequest;
import com.hardrockunion.business.project.dto.NexisExternalProjectLinkResponse;
import com.hardrockunion.business.project.dto.NexisExternalProjectLinkReviewRequest;
import com.hardrockunion.business.project.dto.NexisExternalProjectOptionResponse;
import com.hardrockunion.business.project.enums.NexisExternalProjectLinkStatus;
import com.hardrockunion.business.project.mapper.NexisExternalProjectLinkMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantMemberFlowService;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;

import jakarta.annotation.PostConstruct;

@Service
public class NexisExternalProjectLinkService {

    private static final String APP_CODE = "NEXIS";
    private static final String TYPE_COMPANY = "COMPANY";
    private static final String TYPE_PROJECT = "PROJECT";
    private static final String SHARE_SUMMARY = "SUMMARY";

    private final NexisExternalProjectLinkMapper linkMapper;
    private final TenantRegistryService tenantRegistryService;
    private final TenantMemberFlowService tenantMemberFlowService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final DataSource dataSource;

    public NexisExternalProjectLinkService(NexisExternalProjectLinkMapper linkMapper,
                                           TenantRegistryService tenantRegistryService,
                                           TenantMemberFlowService tenantMemberFlowService,
                                           NexisParticipantCompanyService participantCompanyService,
                                           DataSource dataSource) {
        this.linkMapper = linkMapper;
        this.tenantRegistryService = tenantRegistryService;
        this.tenantMemberFlowService = tenantMemberFlowService;
        this.participantCompanyService = participantCompanyService;
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initSchema() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
            new ClassPathResource("db/mysql/project-external-link-schema.sql")
        );
        populator.execute(dataSource);
    }

    public List<NexisExternalProjectLinkResponse> listOutgoing(LoginUser loginUser) {
        TenantRegistryResponse current = requireCurrentTenant(loginUser);
        if (!TYPE_PROJECT.equals(current.getTenantType())) {
            return List.of();
        }
        return linkMapper.selectList(new LambdaQueryWrapper<NexisExternalProjectLink>()
                .eq(NexisExternalProjectLink::getSourceProjectTenantId, current.getId())
                .eq(NexisExternalProjectLink::getDeleted, 0)
                .orderByDesc(NexisExternalProjectLink::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<NexisExternalProjectLinkResponse> listIncoming(LoginUser loginUser) {
        TenantRegistryResponse current = requireCurrentTenant(loginUser);
        if (!isOrganization(current) || !tenantMemberFlowService.isTenantRoleAdmin(APP_CODE, current.getId(), loginUser)) {
            return List.of();
        }
        return linkMapper.selectList(new LambdaQueryWrapper<NexisExternalProjectLink>()
                .eq(NexisExternalProjectLink::getTargetOrganizationTenantId, current.getId())
                .eq(NexisExternalProjectLink::getLinkStatus, NexisExternalProjectLinkStatus.PENDING.name())
                .eq(NexisExternalProjectLink::getDeleted, 0)
                .orderByAsc(NexisExternalProjectLink::getCreatedAt))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<NexisExternalProjectLinkResponse> listLinkedContracts(LoginUser loginUser) {
        TenantRegistryResponse current = requireCurrentTenant(loginUser);
        if (!TYPE_PROJECT.equals(current.getTenantType())) {
            return List.of();
        }
        return linkMapper.selectList(new LambdaQueryWrapper<NexisExternalProjectLink>()
                .eq(NexisExternalProjectLink::getTargetProjectTenantId, current.getId())
                .eq(NexisExternalProjectLink::getLinkStatus, NexisExternalProjectLinkStatus.LINKED.name())
                .eq(NexisExternalProjectLink::getDeleted, 0)
                .orderByDesc(NexisExternalProjectLink::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<NexisExternalProjectOptionResponse> listReviewableProjects(LoginUser loginUser) {
        TenantRegistryResponse current = requireCurrentTenant(loginUser);
        if (!isOrganization(current)) {
            return List.of();
        }
        tenantMemberFlowService.ensureTenantRoleAdmin(APP_CODE, current.getId(), loginUser);
        List<TenantRegistryResponse> tenants = tenantRegistryService.listEnabledByApp(APP_CODE);
        return tenants.stream()
            .filter(tenant -> TYPE_PROJECT.equals(tenant.getTenantType()))
            .filter(project -> belongsToCompany(project, current))
            .map(this::toProjectOption)
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public NexisExternalProjectLinkResponse create(NexisExternalProjectLinkCreateRequest request, LoginUser loginUser) {
        TenantRegistryResponse sourceProject = requireCurrentTenant(loginUser);
        if (!TYPE_PROJECT.equals(sourceProject.getTenantType())) {
            throw new BusinessException("请先进入要发起关联的承包项目");
        }
        tenantMemberFlowService.ensureTenantRoleAdmin(APP_CODE, sourceProject.getId(), loginUser);
        if (request == null || request.getTargetOrganizationTenantId() == null) {
            throw new BusinessException("targetOrganizationTenantId 不能为空");
        }
        TenantRegistryResponse targetOrganization = tenantRegistryService.getByAppAndId(
            APP_CODE,
            request.getTargetOrganizationTenantId()
        );
        if (!isOrganization(targetOrganization)) {
            throw new BusinessException("上级单位必须是正式公司");
        }
        ensureNoActiveLink(sourceProject.getId());

        if (request.getExternalParticipantCompanyId() != null) {
            NexisParticipantCompany participant = participantCompanyService.loadEntity(
                request.getExternalParticipantCompanyId(),
                sourceProject.getId()
            );
            if (participant.getBindTenantId() != null
                && !participant.getBindTenantId().equals(targetOrganization.getId())) {
                throw new BusinessException("所选外部参建单位已绑定其他正式租户");
            }
        }

        NexisExternalProjectLink link = new NexisExternalProjectLink();
        link.setSourceProjectTenantId(sourceProject.getId());
        link.setSourceOrganizationTenantId(resolveSourceOrganizationId(sourceProject));
        link.setExternalParticipantCompanyId(request.getExternalParticipantCompanyId());
        link.setTargetOrganizationTenantId(targetOrganization.getId());
        link.setExternalOwnerNameSnapshot(StringUtils.defaultIfBlank(
            sourceProject.getExternalOwnerName(),
            targetOrganization.getTenantName()
        ));
        link.setExternalProjectNameSnapshot(StringUtils.trimToNull(sourceProject.getExternalProjectName()));
        link.setContractScopeNameSnapshot(StringUtils.defaultIfBlank(
            sourceProject.getContractScopeName(),
            sourceProject.getTenantName()
        ));
        link.setLinkStatus(NexisExternalProjectLinkStatus.PENDING.name());
        link.setShareScope(SHARE_SUMMARY);
        link.setRequestedBy(loginUser.getUserId());
        link.setDeleted(0);
        linkMapper.insert(link);
        return toResponse(link);
    }

    @Transactional(rollbackFor = Exception.class)
    public NexisExternalProjectLinkResponse review(Long id,
                                                   NexisExternalProjectLinkReviewRequest request,
                                                   LoginUser loginUser) {
        NexisExternalProjectLink link = load(id);
        if (!link.getTargetOrganizationTenantId().equals(loginUser.getTenantId())) {
            throw new BusinessException("请先进入收到申请的公司空间");
        }
        tenantMemberFlowService.ensureTenantRoleAdmin(APP_CODE, link.getTargetOrganizationTenantId(), loginUser);
        if (!NexisExternalProjectLinkStatus.PENDING.name().equals(link.getLinkStatus())) {
            throw new BusinessException("该关联申请已处理");
        }
        if (request == null || request.getApproved() == null) {
            throw new BusinessException("approved 不能为空");
        }

        link.setReviewedBy(loginUser.getUserId());
        link.setReviewedAt(LocalDateTime.now());
        link.setReviewRemark(StringUtils.trimToNull(request.getRemark()));
        if (!request.getApproved()) {
            link.setLinkStatus(NexisExternalProjectLinkStatus.REJECTED.name());
            linkMapper.updateById(link);
            return toResponse(link);
        }

        if (request.getTargetProjectTenantId() == null) {
            throw new BusinessException("通过申请时必须选择正式上级项目");
        }
        TenantRegistryResponse targetOrganization = tenantRegistryService.getByAppAndId(
            APP_CODE,
            link.getTargetOrganizationTenantId()
        );
        TenantRegistryResponse targetProject = tenantRegistryService.getByAppAndId(
            APP_CODE,
            request.getTargetProjectTenantId()
        );
        if (!TYPE_PROJECT.equals(targetProject.getTenantType())
            || !belongsToCompany(targetProject, targetOrganization)) {
            throw new BusinessException("所选正式项目不属于当前公司");
        }

        link.setTargetProjectTenantId(targetProject.getId());
        link.setShareScope(normalizeShareScope(request.getShareScope()));
        link.setLinkStatus(NexisExternalProjectLinkStatus.LINKED.name());
        if (link.getExternalParticipantCompanyId() != null) {
            participantCompanyService.bindTenantFromApprovedLink(
                link.getExternalParticipantCompanyId(),
                link.getSourceProjectTenantId(),
                link.getTargetOrganizationTenantId()
            );
        }
        linkMapper.updateById(link);
        return toResponse(link);
    }

    @Transactional(rollbackFor = Exception.class)
    public NexisExternalProjectLinkResponse cancel(Long id, LoginUser loginUser) {
        NexisExternalProjectLink link = load(id);
        ensureSourceProjectAdmin(link, loginUser);
        if (!NexisExternalProjectLinkStatus.PENDING.name().equals(link.getLinkStatus())) {
            throw new BusinessException("只有待审核申请可以撤回");
        }
        link.setLinkStatus(NexisExternalProjectLinkStatus.CANCELLED.name());
        linkMapper.updateById(link);
        return toResponse(link);
    }

    @Transactional(rollbackFor = Exception.class)
    public NexisExternalProjectLinkResponse unlink(Long id, LoginUser loginUser) {
        NexisExternalProjectLink link = load(id);
        if (!NexisExternalProjectLinkStatus.LINKED.name().equals(link.getLinkStatus())) {
            throw new BusinessException("只有已关联记录可以解除");
        }
        boolean sourceSide = link.getSourceProjectTenantId().equals(loginUser.getTenantId());
        boolean targetSide = link.getTargetOrganizationTenantId().equals(loginUser.getTenantId());
        if (!sourceSide && !targetSide) {
            throw new BusinessException("请进入关联双方的项目或公司空间操作");
        }
        tenantMemberFlowService.ensureTenantRoleAdmin(
            APP_CODE,
            sourceSide ? link.getSourceProjectTenantId() : link.getTargetOrganizationTenantId(),
            loginUser
        );
        if (link.getExternalParticipantCompanyId() != null) {
            participantCompanyService.unbindTenantFromExternalLink(
                link.getExternalParticipantCompanyId(),
                link.getSourceProjectTenantId(),
                link.getTargetOrganizationTenantId()
            );
        }
        link.setLinkStatus(NexisExternalProjectLinkStatus.UNLINKED.name());
        link.setUnlinkedBy(loginUser.getUserId());
        link.setUnlinkedAt(LocalDateTime.now());
        linkMapper.updateById(link);
        return toResponse(link);
    }

    private void ensureNoActiveLink(Long sourceProjectTenantId) {
        Long count = linkMapper.selectCount(new LambdaQueryWrapper<NexisExternalProjectLink>()
            .eq(NexisExternalProjectLink::getSourceProjectTenantId, sourceProjectTenantId)
            .in(NexisExternalProjectLink::getLinkStatus,
                NexisExternalProjectLinkStatus.PENDING.name(),
                NexisExternalProjectLinkStatus.LINKED.name())
            .eq(NexisExternalProjectLink::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BusinessException("当前承包项目已有待审核或已生效的上级项目关联");
        }
    }

    private void ensureSourceProjectAdmin(NexisExternalProjectLink link, LoginUser loginUser) {
        if (!link.getSourceProjectTenantId().equals(loginUser.getTenantId())) {
            throw new BusinessException("请先进入发起申请的承包项目");
        }
        tenantMemberFlowService.ensureTenantRoleAdmin(APP_CODE, link.getSourceProjectTenantId(), loginUser);
    }

    private NexisExternalProjectLink load(Long id) {
        if (id == null) {
            throw new BusinessException("关联记录ID不能为空");
        }
        NexisExternalProjectLink link = linkMapper.selectOne(new LambdaQueryWrapper<NexisExternalProjectLink>()
            .eq(NexisExternalProjectLink::getId, id)
            .eq(NexisExternalProjectLink::getDeleted, 0)
            .last("limit 1"));
        if (link == null) {
            throw new BusinessException("外部项目关联记录不存在");
        }
        return link;
    }

    private TenantRegistryResponse requireCurrentTenant(LoginUser loginUser) {
        if (loginUser == null || loginUser.getTenantId() == null) {
            throw new BusinessException("请先进入 Nexis 工作空间");
        }
        return tenantRegistryService.getByAppAndId(APP_CODE, loginUser.getTenantId());
    }

    private Long resolveSourceOrganizationId(TenantRegistryResponse project) {
        if (project.getParentTenantId() == null) {
            return null;
        }
        TenantRegistryResponse parent = tenantRegistryService.getByAppAndId(APP_CODE, project.getParentTenantId());
        return isOrganization(parent) ? parent.getId() : null;
    }

    private boolean belongsToCompany(TenantRegistryResponse project,
                                     TenantRegistryResponse company) {
        return company.getId().equals(project.getParentTenantId());
    }

    private boolean isOrganization(TenantRegistryResponse tenant) {
        return TYPE_COMPANY.equals(tenant.getTenantType());
    }

    private String normalizeShareScope(String value) {
        String normalized = StringUtils.upperCase(StringUtils.trimToEmpty(value));
        if (StringUtils.isBlank(normalized) || SHARE_SUMMARY.equals(normalized)) {
            return SHARE_SUMMARY;
        }
        throw new BusinessException("当前仅支持 SUMMARY 数据共享范围");
    }

    private NexisExternalProjectOptionResponse toProjectOption(TenantRegistryResponse tenant) {
        NexisExternalProjectOptionResponse response = new NexisExternalProjectOptionResponse();
        response.setTenantId(tenant.getId());
        response.setParentTenantId(tenant.getParentTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setTenantName(tenant.getTenantName());
        return response;
    }

    private NexisExternalProjectLinkResponse toResponse(NexisExternalProjectLink link) {
        NexisExternalProjectLinkResponse response = new NexisExternalProjectLinkResponse();
        response.setId(link.getId());
        response.setSourceProjectTenantId(link.getSourceProjectTenantId());
        response.setSourceOrganizationTenantId(link.getSourceOrganizationTenantId());
        response.setExternalParticipantCompanyId(link.getExternalParticipantCompanyId());
        response.setTargetOrganizationTenantId(link.getTargetOrganizationTenantId());
        response.setTargetProjectTenantId(link.getTargetProjectTenantId());
        response.setExternalOwnerName(link.getExternalOwnerNameSnapshot());
        response.setExternalProjectName(link.getExternalProjectNameSnapshot());
        response.setContractScopeName(link.getContractScopeNameSnapshot());
        response.setLinkStatus(link.getLinkStatus());
        NexisExternalProjectLinkStatus status = NexisExternalProjectLinkStatus.valueOf(link.getLinkStatus());
        response.setLinkStatusLabel(status.getLabel());
        response.setShareScope(link.getShareScope());
        response.setReviewRemark(link.getReviewRemark());
        response.setCreatedAt(link.getCreatedAt());
        response.setReviewedAt(link.getReviewedAt());
        response.setUnlinkedAt(link.getUnlinkedAt());

        TenantRegistryResponse sourceProject = tenantRegistryService.getByAppAndId(APP_CODE, link.getSourceProjectTenantId());
        response.setSourceProjectTenantName(sourceProject.getTenantName());
        if (link.getSourceOrganizationTenantId() != null) {
            response.setSourceOrganizationTenantName(
                tenantRegistryService.getByAppAndId(APP_CODE, link.getSourceOrganizationTenantId()).getTenantName()
            );
        }
        if (link.getExternalParticipantCompanyId() != null) {
            try {
                response.setExternalParticipantCompanyName(
                    participantCompanyService.loadEntity(
                        link.getExternalParticipantCompanyId(),
                        link.getSourceProjectTenantId()
                    ).getCompanyName()
                );
            } catch (BusinessException ignored) {
                // The relation history remains readable if the local participant was later removed.
            }
        }
        response.setTargetOrganizationTenantName(
            tenantRegistryService.getByAppAndId(APP_CODE, link.getTargetOrganizationTenantId()).getTenantName()
        );
        if (link.getTargetProjectTenantId() != null) {
            response.setTargetProjectTenantName(
                tenantRegistryService.getByAppAndId(APP_CODE, link.getTargetProjectTenantId()).getTenantName()
            );
        }
        return response;
    }
}
