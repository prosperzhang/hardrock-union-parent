package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.NexisParticipantCompany;
import com.hardrockunion.business.project.domain.entity.NexisProject;
import com.hardrockunion.business.project.domain.entity.NexisSite;
import com.hardrockunion.business.project.domain.entity.NexisSiteParticipant;
import com.hardrockunion.business.project.dto.NexisSiteParticipantCreateRequest;
import com.hardrockunion.business.project.dto.NexisSiteParticipantQueryRequest;
import com.hardrockunion.business.project.dto.NexisSiteParticipantResponse;
import com.hardrockunion.business.project.enums.NexisParticipantRole;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.mapper.NexisSiteParticipantMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 标段参建关系服务。
 *
 * <p>这一层负责“谁参与了哪个标段”，但还不下钻到 `A-001 ~ A-5000` 这种具体范围。
 */
@Service
public class NexisSiteParticipantService {

    private final NexisSiteParticipantMapper siteParticipantMapper;
    private final NexisSiteService siteService;
    private final NexisProjectLookupService projectLookupService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisSiteParticipantService(NexisSiteParticipantMapper siteParticipantMapper,
                                      NexisSiteService siteService,
                                      NexisProjectLookupService projectLookupService,
                                      NexisParticipantCompanyService participantCompanyService,
                                      NexisAccessGuard nexisAccessGuard) {
        this.siteParticipantMapper = siteParticipantMapper;
        this.siteService = siteService;
        this.projectLookupService = projectLookupService;
        this.participantCompanyService = participantCompanyService;
        this.nexisAccessGuard = nexisAccessGuard;
    }

    public PageResponse<NexisSiteParticipantResponse> list(NexisSiteParticipantQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisSiteParticipantQueryRequest query = request == null ? new NexisSiteParticipantQueryRequest() : request;
        LambdaQueryWrapper<NexisSiteParticipant> wrapper = new LambdaQueryWrapper<NexisSiteParticipant>()
            .eq(NexisSiteParticipant::getTenantId, loginUser.getTenantId())
            .eq(NexisSiteParticipant::getDeleted, 0)
            .orderByDesc(NexisSiteParticipant::getId);
        if (query.getProjectId() != null) {
            wrapper.eq(NexisSiteParticipant::getProjectId, query.getProjectId());
        }
        if (query.getSiteId() != null) {
            wrapper.eq(NexisSiteParticipant::getSiteId, query.getSiteId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(NexisSiteParticipant::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (StringUtils.isNotBlank(query.getParticipantRole())) {
            wrapper.eq(NexisSiteParticipant::getParticipantRole, StringUtils.upperCase(StringUtils.trim(query.getParticipantRole())));
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            List<Long> siteIds = siteService.findIdsByKeyword(query.getKeyword(), loginUser.getTenantId());
            List<Long> companyIds = participantCompanyService.findIdsByKeyword(query.getKeyword(), loginUser.getTenantId());
            if (siteIds.isEmpty() && companyIds.isEmpty()) {
                return PageResponse.from(Page.<NexisSiteParticipantResponse>of(query.getPageNum(), query.getPageSize()));
            }
            wrapper.and(w -> {
                boolean hasCondition = false;
                if (!siteIds.isEmpty()) {
                    w.in(NexisSiteParticipant::getSiteId, siteIds);
                    hasCondition = true;
                }
                if (!companyIds.isEmpty()) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.in(NexisSiteParticipant::getParticipantCompanyId, companyIds);
                }
            });
        }
        Page<NexisSiteParticipant> page = siteParticipantMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public NexisSiteParticipantResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public NexisSiteParticipantResponse create(NexisSiteParticipantCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.PARTICIPANT_MANAGE);
        if (request == null || request.getSiteId() == null) {
            throw new BusinessException("siteId 不能为空");
        }
        if (request.getParticipantCompanyId() == null) {
            throw new BusinessException("participantCompanyId 不能为空");
        }
        if (StringUtils.isBlank(request.getParticipantRole())) {
            throw new BusinessException("participantRole 不能为空");
        }
        NexisParticipantRole participantRole = NexisParticipantRole.fromCode(request.getParticipantRole());
        if (participantRole == null) {
            throw new BusinessException("participantRole 不合法");
        }

        NexisSite site = siteService.loadEntity(request.getSiteId(), loginUser.getTenantId());
        NexisParticipantCompany company = participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long projectId = site.getProjectId() != null ? site.getProjectId() : request.getProjectId();
        if (projectId == null) {
            throw new BusinessException("标段尚未关联项目，无法建立标段参建关系");
        }
        projectLookupService.loadEntity(projectId, loginUser.getTenantId());

        NexisSiteParticipant participant = new NexisSiteParticipant();
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

    public NexisSiteParticipant loadEntity(Long id, Long tenantId) {
        NexisSiteParticipant participant = siteParticipantMapper.selectOne(new LambdaQueryWrapper<NexisSiteParticipant>()
            .eq(NexisSiteParticipant::getId, id)
            .eq(NexisSiteParticipant::getTenantId, tenantId)
            .eq(NexisSiteParticipant::getDeleted, 0)
            .last("limit 1"));
        if (participant == null) {
            throw new BusinessException("标段参建关系不存在");
        }
        return participant;
    }

    private NexisSiteParticipantResponse toResponse(NexisSiteParticipant item, Long tenantId) {
        NexisSite site = siteService.loadEntity(item.getSiteId(), tenantId);
        NexisProject project = projectLookupService.loadEntity(item.getProjectId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(item.getParticipantCompanyId(), tenantId);
        NexisSiteParticipantResponse response = new NexisSiteParticipantResponse();
        response.setId(item.getId());
        response.setTenantId(item.getTenantId());
        response.setProjectId(item.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setSiteId(item.getSiteId());
        response.setSiteName(site.getSiteName());
        response.setParticipantCompanyId(item.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setParticipantRole(item.getParticipantRole());
        NexisParticipantRole participantRole = NexisParticipantRole.fromCode(item.getParticipantRole());
        response.setParticipantRoleLabel(participantRole == null ? null : participantRole.getLabel());
        response.setStatus(item.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(item.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        response.setRemark(item.getRemark());
        return response;
    }
}
