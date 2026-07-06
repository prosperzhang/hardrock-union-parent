package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.PmhubParticipantCompany;
import com.hardrockunion.business.project.domain.entity.PmhubProject;
import com.hardrockunion.business.project.domain.entity.PmhubSite;
import com.hardrockunion.business.project.domain.entity.PmhubSiteParticipant;
import com.hardrockunion.business.project.dto.PmhubSiteParticipantCreateRequest;
import com.hardrockunion.business.project.dto.PmhubSiteParticipantQueryRequest;
import com.hardrockunion.business.project.dto.PmhubSiteParticipantResponse;
import com.hardrockunion.business.project.enums.PmhubParticipantRole;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.mapper.PmhubSiteParticipantMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 标段参建关系服务。
 *
 * <p>这一层负责“谁参与了哪个标段”，但还不下钻到 `A-001 ~ A-5000` 这种具体范围。
 */
@Service
public class PmhubSiteParticipantService {

    private final PmhubSiteParticipantMapper siteParticipantMapper;
    private final PmhubSiteService siteService;
    private final PmhubProjectLookupService projectLookupService;
    private final PmhubParticipantCompanyService participantCompanyService;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubSiteParticipantService(PmhubSiteParticipantMapper siteParticipantMapper,
                                      PmhubSiteService siteService,
                                      PmhubProjectLookupService projectLookupService,
                                      PmhubParticipantCompanyService participantCompanyService,
                                      PmhubAccessGuard pmhubAccessGuard) {
        this.siteParticipantMapper = siteParticipantMapper;
        this.siteService = siteService;
        this.projectLookupService = projectLookupService;
        this.participantCompanyService = participantCompanyService;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public PageResponse<PmhubSiteParticipantResponse> list(PmhubSiteParticipantQueryRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubSiteParticipantQueryRequest query = request == null ? new PmhubSiteParticipantQueryRequest() : request;
        LambdaQueryWrapper<PmhubSiteParticipant> wrapper = new LambdaQueryWrapper<PmhubSiteParticipant>()
            .eq(PmhubSiteParticipant::getTenantId, loginUser.getTenantId())
            .eq(PmhubSiteParticipant::getDeleted, 0)
            .orderByDesc(PmhubSiteParticipant::getId);
        if (query.getProjectId() != null) {
            wrapper.eq(PmhubSiteParticipant::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(PmhubSiteParticipant::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(PmhubSiteParticipant::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (StringUtils.isNotBlank(query.getParticipantRole())) {
            wrapper.eq(PmhubSiteParticipant::getParticipantRole, StringUtils.upperCase(StringUtils.trim(query.getParticipantRole())));
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            List<Long> siteIds = siteService.findIdsByKeyword(query.getKeyword(), loginUser.getTenantId());
            List<Long> companyIds = participantCompanyService.findIdsByKeyword(query.getKeyword(), loginUser.getTenantId());
            if (siteIds.isEmpty() && companyIds.isEmpty()) {
                return PageResponse.from(Page.<PmhubSiteParticipantResponse>of(query.getPageNum(), query.getPageSize()));
            }
            wrapper.and(w -> {
                boolean hasCondition = false;
                if (!siteIds.isEmpty()) {
                    w.in(PmhubSiteParticipant::getSiteId, siteIds);
                    hasCondition = true;
                }
                if (!companyIds.isEmpty()) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.in(PmhubSiteParticipant::getParticipantCompanyId, companyIds);
                }
            });
        }
        Page<PmhubSiteParticipant> page = siteParticipantMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public PmhubSiteParticipantResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public PmhubSiteParticipantResponse create(PmhubSiteParticipantCreateRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getSiteId() == null) {
            throw new BusinessException("siteId 不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getParticipantRole())) {
            throw new BusinessException("participantRole 不能为空");
        }
        PmhubParticipantRole participantRole = PmhubParticipantRole.fromCode(request.getParticipantRole());
        if (participantRole == null) {
            throw new BusinessException("participantRole 不合法");
        }

        PmhubSite site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
        PmhubParticipantCompany company = participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long projectId = site.getProjectId() != null ? site.getProjectId() : request.getProjectId();
        if (projectId == null) {
            throw new BusinessException("标段尚未关联项目，无法建立标段参建关系");
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());

        PmhubSiteParticipant participant = new PmhubSiteParticipant();
        participant.setTenantId(loginUser.getTenantId());
        participant.setProjectId(projectId);
        participant.setSiteId(site.getId());
        participant.setParticipantCompanyId(company.getId());
        participant.setParticipantRole(participantRole.getCode());
        participant.setRemark(StringUtils.trimToNull(request.getRemark()));
        participant.setStatus(1);
        participant.setDeleted(0);
        participant.setCreatedBy(loginUser.getUserId());
        siteParticipantMapper.insert(participant);
        return getById(participant.getId(), loginUser);
    }

    public PmhubSiteParticipant loadEntity(Long id, Long tenantId) {
        PmhubSiteParticipant participant = siteParticipantMapper.selectOne(new LambdaQueryWrapper<PmhubSiteParticipant>()
            .eq(PmhubSiteParticipant::getId, id)
            .eq(PmhubSiteParticipant::getTenantId, tenantId)
            .eq(PmhubSiteParticipant::getDeleted, 0)
            .last("limit 1"));
        if (participant == null) {
            throw new BusinessException("标段参建关系不存在");
        }
        return participant;
    }

    private PmhubSiteParticipantResponse toResponse(PmhubSiteParticipant item, Long tenantId) {
        PmhubSite site = siteService.loadEntity(item.getSiteId(), tenantId);
        PmhubProject project = projectLookupService.loadEntity(item.getProjectId(), tenantId);
        PmhubParticipantCompany company = participantCompanyService.loadEntity(item.getParticipantCompanyId(), tenantId);
        PmhubSiteParticipantResponse response = new PmhubSiteParticipantResponse();
        response.setId(item.getId());
        response.setTenantId(item.getTenantId());
        response.setProjectId(item.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(item.getSiteId());
        response.setSiteName(site.getSiteName());
        response.setParticipantCompanyId(item.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setParticipantRole(item.getParticipantRole());
        PmhubParticipantRole participantRole = PmhubParticipantRole.fromCode(item.getParticipantRole());
        response.setParticipantRoleLabel(participantRole == null ? null : participantRole.getLabel());
        response.setStatus(item.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(item.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        response.setRemark(item.getRemark());
        return response;
    }
}
