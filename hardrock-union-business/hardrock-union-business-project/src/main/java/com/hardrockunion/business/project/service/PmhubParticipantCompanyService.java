package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.PmhubParticipantCompany;
import com.hardrockunion.business.project.dto.PmhubParticipantCompanyCreateRequest;
import com.hardrockunion.business.project.dto.PmhubParticipantCompanyQueryRequest;
import com.hardrockunion.business.project.dto.PmhubParticipantCompanyResponse;
import com.hardrockunion.business.project.enums.PmhubParticipantCompanyType;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.mapper.PmhubParticipantCompanyMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 施工整改参建单位服务。
 *
 * <p>这里统一承接总包、专业分包、劳务分包、供应商等参建主体，避免把组织身份直接塞进项目或标段。
 */
@Service
public class PmhubParticipantCompanyService {

    private final PmhubParticipantCompanyMapper participantCompanyMapper;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubParticipantCompanyService(PmhubParticipantCompanyMapper participantCompanyMapper,
                                         PmhubAccessGuard pmhubAccessGuard) {
        this.participantCompanyMapper = participantCompanyMapper;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public PageResponse<PmhubParticipantCompanyResponse> list(PmhubParticipantCompanyQueryRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubParticipantCompanyQueryRequest query = request == null ? new PmhubParticipantCompanyQueryRequest() : request;
        LambdaQueryWrapper<PmhubParticipantCompany> wrapper = new LambdaQueryWrapper<PmhubParticipantCompany>()
            .eq(PmhubParticipantCompany::getTenantId, loginUser.getTenantId())
            .eq(PmhubParticipantCompany::getDeleted, 0);
        if (StringUtils.isNotBlank(query.getCompanyType())) {
            wrapper.eq(PmhubParticipantCompany::getCompanyType, StringUtils.upperCase(StringUtils.trim(query.getCompanyType())));
        }
        if (query.getBindTenantId() != null) {
            wrapper.eq(PmhubParticipantCompany::getBindTenantId, query.getBindTenantId());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            wrapper.and(w -> w.like(PmhubParticipantCompany::getCompanyName, keyword)
                .or()
                .like(PmhubParticipantCompany::getCompanyCode, keyword)
                .or()
                .like(PmhubParticipantCompany::getContactName, keyword)
                .or()
                .like(PmhubParticipantCompany::getContactPhone, keyword));
        }
        wrapper.orderByDesc(PmhubParticipantCompany::getId);
        Page<PmhubParticipantCompany> page = participantCompanyMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(this::toResponse));
    }

    public PmhubParticipantCompanyResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()));
    }

    public PmhubParticipantCompanyResponse create(PmhubParticipantCompanyCreateRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getCompanyName())) {
            throw new BusinessException("companyName 不能为空");
        }
        if (StringUtils.isBlank(request.getCompanyType())) {
            throw new BusinessException("companyType 不能为空");
        }
        PmhubParticipantCompanyType companyType = PmhubParticipantCompanyType.fromCode(request.getCompanyType());
        if (companyType == null) {
            throw new BusinessException("companyType 不合法");
        }

        PmhubParticipantCompany company = new PmhubParticipantCompany();
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

    public PmhubParticipantCompany loadEntity(Long id, Long tenantId) {
        PmhubParticipantCompany company = participantCompanyMapper.selectOne(new LambdaQueryWrapper<PmhubParticipantCompany>()
            .eq(PmhubParticipantCompany::getId, id)
            .eq(PmhubParticipantCompany::getTenantId, tenantId)
            .eq(PmhubParticipantCompany::getDeleted, 0)
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
        return participantCompanyMapper.selectList(new LambdaQueryWrapper<PmhubParticipantCompany>()
                .select(PmhubParticipantCompany::getId)
                .eq(PmhubParticipantCompany::getTenantId, tenantId)
                .eq(PmhubParticipantCompany::getDeleted, 0)
                .and(wrapper -> wrapper.like(PmhubParticipantCompany::getCompanyName, trimmedKeyword)
                    .or()
                    .like(PmhubParticipantCompany::getCompanyCode, trimmedKeyword)
                    .or()
                    .like(PmhubParticipantCompany::getContactName, trimmedKeyword)
                    .or()
                    .like(PmhubParticipantCompany::getContactPhone, trimmedKeyword)))
            .stream()
            .map(PmhubParticipantCompany::getId)
            .toList();
    }

    private PmhubParticipantCompanyResponse toResponse(PmhubParticipantCompany company) {
        PmhubParticipantCompanyResponse response = new PmhubParticipantCompanyResponse();
        response.setId(company.getId());
        response.setTenantId(company.getTenantId());
        response.setBindTenantId(company.getBindTenantId());
        response.setCompanyName(company.getCompanyName());
        response.setCompanyCode(company.getCompanyCode());
        response.setCompanyType(company.getCompanyType());
        PmhubParticipantCompanyType companyType = PmhubParticipantCompanyType.fromCode(company.getCompanyType());
        response.setCompanyTypeLabel(companyType == null ? null : companyType.getLabel());
        response.setContactName(company.getContactName());
        response.setContactPhone(company.getContactPhone());
        response.setStatus(company.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(company.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        return response;
    }
}
