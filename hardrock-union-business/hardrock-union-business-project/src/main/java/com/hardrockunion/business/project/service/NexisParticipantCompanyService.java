package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.NexisParticipantCompany;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyBindRequest;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyCreateRequest;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyQueryRequest;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyResponse;
import com.hardrockunion.business.project.enums.NexisParticipantCompanyType;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.mapper.NexisParticipantCompanyMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantMemberFlowService;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;

/**
 * 施工整改参建单位服务。
 *
 * <p>这里统一承接总包、专业分包、劳务分包、供应商等参建主体，避免把组织身份直接塞进项目或标段。
 */
@Service
public class NexisParticipantCompanyService {

    private static final String TENANT_TYPE_COMPANY = "COMPANY";

    private final NexisParticipantCompanyMapper participantCompanyMapper;
    private final NexisAccessGuard nexisAccessGuard;
    private final TenantRegistryService tenantRegistryService;
    private final TenantMemberFlowService tenantMemberFlowService;

    public NexisParticipantCompanyService(NexisParticipantCompanyMapper participantCompanyMapper,
                                          NexisAccessGuard nexisAccessGuard,
                                          TenantRegistryService tenantRegistryService,
                                          TenantMemberFlowService tenantMemberFlowService) {
        this.participantCompanyMapper = participantCompanyMapper;
        this.nexisAccessGuard = nexisAccessGuard;
        this.tenantRegistryService = tenantRegistryService;
        this.tenantMemberFlowService = tenantMemberFlowService;
    }

    public PageResponse<NexisParticipantCompanyResponse> list(NexisParticipantCompanyQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisParticipantCompanyQueryRequest query = request == null ? new NexisParticipantCompanyQueryRequest() : request;
        LambdaQueryWrapper<NexisParticipantCompany> wrapper = new LambdaQueryWrapper<NexisParticipantCompany>()
            .eq(NexisParticipantCompany::getTenantId, loginUser.getTenantId())
            .eq(NexisParticipantCompany::getDeleted, 0);
        if (StringUtils.isNotBlank(query.getCompanyType())) {
            wrapper.eq(NexisParticipantCompany::getCompanyType, StringUtils.upperCase(StringUtils.trim(query.getCompanyType())));
        }
        if (query.getBindTenantId() != null) {
            wrapper.eq(NexisParticipantCompany::getBindTenantId, query.getBindTenantId());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            wrapper.and(w -> w.like(NexisParticipantCompany::getCompanyName, keyword)
                .or()
                .like(NexisParticipantCompany::getCompanyCode, keyword)
                .or()
                .like(NexisParticipantCompany::getContactName, keyword)
                .or()
                .like(NexisParticipantCompany::getContactPhone, keyword));
        }
        wrapper.orderByDesc(NexisParticipantCompany::getId);
        Page<NexisParticipantCompany> page = participantCompanyMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(this::toResponse));
    }

    public NexisParticipantCompanyResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()));
    }

    public NexisParticipantCompanyResponse create(NexisParticipantCompanyCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.PARTICIPANT_MANAGE);
        if (request == null || StringUtils.isBlank(request.getCompanyName())) {
            throw new BusinessException("companyName 不能为空");
        }
        if (StringUtils.isBlank(request.getCompanyType())) {
            throw new BusinessException("companyType 不能为空");
        }
        NexisParticipantCompanyType companyType = NexisParticipantCompanyType.fromCode(request.getCompanyType());
        if (companyType == null) {
            throw new BusinessException("companyType 不合法");
        }
        String companyName = StringUtils.trim(request.getCompanyName());
        Long duplicateCount = participantCompanyMapper.selectCount(new LambdaQueryWrapper<NexisParticipantCompany>()
            .eq(NexisParticipantCompany::getTenantId, loginUser.getTenantId())
            .eq(NexisParticipantCompany::getCompanyName, companyName)
            .eq(NexisParticipantCompany::getDeleted, 0));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException("当前项目已存在同名参建单位");
        }

        NexisParticipantCompany company = new NexisParticipantCompany();
        company.setTenantId(loginUser.getTenantId());
        if (request.getBindTenantId() != null) {
            validateBindableTenant(loginUser.getAppCode(), request.getBindTenantId());
            tenantMemberFlowService.ensureTenantRoleAdmin(loginUser.getAppCode(), request.getBindTenantId(), loginUser);
            ensureBindTenantNotDuplicated(null, loginUser.getTenantId(), request.getBindTenantId());
        }
        company.setBindTenantId(request.getBindTenantId());
        company.setCompanyName(companyName);
        company.setCompanyCode(StringUtils.trimToNull(request.getCompanyCode()));
        company.setCompanyType(companyType.getCode());
        company.setContactName(StringUtils.trimToNull(request.getContactName()));
        company.setContactPhone(StringUtils.trimToNull(request.getContactPhone()));
        company.setStatus(1);
        company.setDeleted(0);
        company.setCreatedBy(loginUser.getUserId());
        participantCompanyMapper.insert(company);
        return getById(company.getId(), loginUser);
    }

    public NexisParticipantCompanyResponse bindTenant(Long id, NexisParticipantCompanyBindRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.PARTICIPANT_MANAGE);
        if (request == null || request.getBindTenantId() == null) {
            throw new BusinessException("bindTenantId 不能为空");
        }
        NexisParticipantCompany company = loadEntity(id, loginUser.getTenantId());
        TenantRegistryResponse tenant = validateBindableTenant(loginUser.getAppCode(), request.getBindTenantId());
        tenantMemberFlowService.ensureTenantRoleAdmin(loginUser.getAppCode(), tenant.getId(), loginUser);
        ensureBindTenantNotDuplicated(company.getId(), loginUser.getTenantId(), tenant.getId());
        company.setBindTenantId(tenant.getId());
        participantCompanyMapper.updateById(company);
        return getById(company.getId(), loginUser);
    }

    public NexisParticipantCompanyResponse unbindTenant(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.PARTICIPANT_MANAGE);
        NexisParticipantCompany company = loadEntity(id, loginUser.getTenantId());
        participantCompanyMapper.update(null, new LambdaUpdateWrapper<NexisParticipantCompany>()
            .set(NexisParticipantCompany::getBindTenantId, null)
            .eq(NexisParticipantCompany::getId, company.getId())
            .eq(NexisParticipantCompany::getTenantId, loginUser.getTenantId())
            .eq(NexisParticipantCompany::getDeleted, 0));
        return getById(company.getId(), loginUser);
    }

    public void bindTenantFromApprovedLink(Long id, Long sourceProjectTenantId, Long bindTenantId) {
        NexisParticipantCompany company = loadEntity(id, sourceProjectTenantId);
        if (company.getBindTenantId() != null && !company.getBindTenantId().equals(bindTenantId)) {
            throw new BusinessException("外部参建单位已绑定其他正式租户");
        }
        ensureBindTenantNotDuplicated(company.getId(), sourceProjectTenantId, bindTenantId);
        company.setBindTenantId(bindTenantId);
        participantCompanyMapper.updateById(company);
    }

    public void unbindTenantFromExternalLink(Long id, Long sourceProjectTenantId, Long bindTenantId) {
        NexisParticipantCompany company = loadEntity(id, sourceProjectTenantId);
        if (!bindTenantId.equals(company.getBindTenantId())) {
            return;
        }
        participantCompanyMapper.update(null, new LambdaUpdateWrapper<NexisParticipantCompany>()
            .set(NexisParticipantCompany::getBindTenantId, null)
            .eq(NexisParticipantCompany::getId, id)
            .eq(NexisParticipantCompany::getTenantId, sourceProjectTenantId)
            .eq(NexisParticipantCompany::getBindTenantId, bindTenantId)
            .eq(NexisParticipantCompany::getDeleted, 0));
    }

    public NexisParticipantCompany loadEntity(Long id, Long tenantId) {
        NexisParticipantCompany company = participantCompanyMapper.selectOne(new LambdaQueryWrapper<NexisParticipantCompany>()
            .eq(NexisParticipantCompany::getId, id)
            .eq(NexisParticipantCompany::getTenantId, tenantId)
            .eq(NexisParticipantCompany::getDeleted, 0)
            .last("limit 1"));
        if (company == null) {
            throw new BusinessException("参建单位不存在");
        }
        return company;
    }

    public List<Long> findIdsByKeyword(String keyword, Long tenantId) {
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }
        String trimmedKeyword = StringUtils.trim(keyword);
        return participantCompanyMapper.selectList(new LambdaQueryWrapper<NexisParticipantCompany>()
                .select(NexisParticipantCompany::getId)
                .eq(NexisParticipantCompany::getTenantId, tenantId)
                .eq(NexisParticipantCompany::getDeleted, 0)
                .and(wrapper -> wrapper.like(NexisParticipantCompany::getCompanyName, trimmedKeyword)
                    .or()
                    .like(NexisParticipantCompany::getCompanyCode, trimmedKeyword)
                    .or()
                    .like(NexisParticipantCompany::getContactName, trimmedKeyword)
                    .or()
                    .like(NexisParticipantCompany::getContactPhone, trimmedKeyword)))
            .stream()
            .map(NexisParticipantCompany::getId)
            .toList();
    }

    private NexisParticipantCompanyResponse toResponse(NexisParticipantCompany company) {
        NexisParticipantCompanyResponse response = new NexisParticipantCompanyResponse();
        response.setId(company.getId());
        response.setTenantId(company.getTenantId());
        response.setBindTenantId(company.getBindTenantId());
        if (company.getBindTenantId() != null) {
            TenantRegistryResponse bindTenant = tenantRegistryService.getById(company.getBindTenantId());
            response.setBindTenantName(bindTenant.getTenantName());
            response.setBindTenantType(bindTenant.getTenantType());
        }
        response.setCompanyName(company.getCompanyName());
        response.setCompanyCode(company.getCompanyCode());
        response.setCompanyType(company.getCompanyType());
        NexisParticipantCompanyType companyType = NexisParticipantCompanyType.fromCode(company.getCompanyType());
        response.setCompanyTypeLabel(companyType == null ? null : companyType.getLabel());
        response.setContactName(company.getContactName());
        response.setContactPhone(company.getContactPhone());
        response.setStatus(company.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(company.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }

    private TenantRegistryResponse validateBindableTenant(String appCode, Long bindTenantId) {
        TenantRegistryResponse tenant = tenantRegistryService.getByAppAndId(appCode, bindTenantId);
        if (!TENANT_TYPE_COMPANY.equals(tenant.getTenantType())) {
            throw new BusinessException("参建单位只能绑定公司租户");
        }
        if (tenant.getStatus() == null || tenant.getStatus() != 1) {
            throw new BusinessException("绑定租户未启用");
        }
        return tenant;
    }

    private void ensureBindTenantNotDuplicated(Long currentId, Long tenantId, Long bindTenantId) {
        LambdaQueryWrapper<NexisParticipantCompany> wrapper = new LambdaQueryWrapper<NexisParticipantCompany>()
            .eq(NexisParticipantCompany::getTenantId, tenantId)
            .eq(NexisParticipantCompany::getBindTenantId, bindTenantId)
            .eq(NexisParticipantCompany::getDeleted, 0);
        if (currentId != null) {
            wrapper.ne(NexisParticipantCompany::getId, currentId);
        }
        Long count = participantCompanyMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("当前项目下已有参建单位绑定该租户");
        }
    }
}
