package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.NexisParticipantCompany;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyCreateRequest;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyQueryRequest;
import com.hardrockunion.business.project.dto.NexisParticipantCompanyResponse;
import com.hardrockunion.business.project.enums.NexisParticipantCompanyType;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.mapper.NexisParticipantCompanyMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 施工整改参建单位服务。
 *
 * <p>这里统一承接总包、专业分包、劳务分包、供应商等参建主体，避免把组织身份直接塞进项目或标段。
 */
@Service
public class NexisParticipantCompanyService {

    private final NexisParticipantCompanyMapper participantCompanyMapper;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisParticipantCompanyService(NexisParticipantCompanyMapper participantCompanyMapper,
                                         NexisAccessGuard nexisAccessGuard) {
        this.participantCompanyMapper = participantCompanyMapper;
        this.nexisAccessGuard = nexisAccessGuard;
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
        nexisAccessGuard.ensureLogin(loginUser);
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

        NexisParticipantCompany company = new NexisParticipantCompany();
        company.setTenantId(loginUser.getTenantId());
        company.setBindTenantId(request.getBindTenantId());
        company.setCompanyName(StringUtils.trim(request.getCompanyName()));
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
}
