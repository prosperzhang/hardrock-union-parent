package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.PmhubParticipantCompany;
import com.hardrockunion.business.project.domain.entity.PmhubProject;
import com.hardrockunion.business.project.domain.entity.PmhubProjectParticipant;
import com.hardrockunion.business.project.dto.PmhubProjectParticipantCreateRequest;
import com.hardrockunion.business.project.dto.PmhubProjectParticipantQueryRequest;
import com.hardrockunion.business.project.dto.PmhubProjectParticipantResponse;
import com.hardrockunion.business.project.enums.PmhubParticipantRole;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.mapper.PmhubProjectParticipantMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 项目参建关系服务。
 *
 * <p>这一层只负责“谁参与了项目”，不负责具体标段和承包范围。
 */
@Service
public class PmhubProjectParticipantService {

    private final PmhubProjectParticipantMapper projectParticipantMapper;
    private final PmhubProjectLookupService projectLookupService;
    private final PmhubParticipantCompanyService participantCompanyService;
    private final PmhubAccessGuard pmhubAccessGuard;

    public PmhubProjectParticipantService(PmhubProjectParticipantMapper projectParticipantMapper,
                                         PmhubProjectLookupService projectLookupService,
                                         PmhubParticipantCompanyService participantCompanyService,
                                         PmhubAccessGuard pmhubAccessGuard) {
        this.projectParticipantMapper = projectParticipantMapper;
        this.projectLookupService = projectLookupService;
        this.participantCompanyService = participantCompanyService;
        this.pmhubAccessGuard = pmhubAccessGuard;
    }

    public PageResponse<PmhubProjectParticipantResponse> list(PmhubProjectParticipantQueryRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        PmhubProjectParticipantQueryRequest query = request == null ? new PmhubProjectParticipantQueryRequest() : request;
        LambdaQueryWrapper<PmhubProjectParticipant> wrapper = new LambdaQueryWrapper<PmhubProjectParticipant>()
            .eq(PmhubProjectParticipant::getTenantId, loginUser.getTenantId())
            .eq(PmhubProjectParticipant::getDeleted, 0)
            .orderByDesc(PmhubProjectParticipant::getId);
        if (query.getProjectId() != null) {
            wrapper.eq(PmhubProjectParticipant::getProjectId, query.getProjectId());
        }
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(PmhubProjectParticipant::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (StringUtils.isNotBlank(query.getParticipantRole())) {
            wrapper.eq(PmhubProjectParticipant::getParticipantRole, StringUtils.upperCase(StringUtils.trim(query.getParticipantRole())));
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            List<Long> projectIds = projectLookupService.findIdsByKeyword(query.getKeyword(), loginUser.getTenantId());
            List<Long> companyIds = participantCompanyService.findIdsByKeyword(query.getKeyword(), loginUser.getTenantId());
            if (projectIds.isEmpty() && companyIds.isEmpty()) {
                return PageResponse.from(Page.<PmhubProjectParticipantResponse>of(query.getPageNum(), query.getPageSize()));
            }
            wrapper.and(w -> {
                boolean hasCondition = false;
                if (!projectIds.isEmpty()) {
                    w.in(PmhubProjectParticipant::getProjectId, projectIds);
                    hasCondition = true;
                }
                if (!companyIds.isEmpty()) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.in(PmhubProjectParticipant::getParticipantCompanyId, companyIds);
                }
            });
        }
        Page<PmhubProjectParticipant> page = projectParticipantMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public PmhubProjectParticipantResponse getById(Long id, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public PmhubProjectParticipantResponse create(PmhubProjectParticipantCreateRequest request, LoginUser loginUser) {
        pmhubAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getProjectId() == null) {
            throw new BusinessException("projectId 不能为空");
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

        projectLookupService.loadEntity(request.getProjectId(), loginUser.getTenantId());
        participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());

        PmhubProjectParticipant participant = new PmhubProjectParticipant();
        participant.setTenantId(loginUser.getTenantId());
        participant.setProjectId(request.getProjectId());
        participant.setParticipantCompanyId(request.getParticipantCompanyId());
        participant.setParticipantRole(participantRole.getCode());
        participant.setIsLead(request.getIsLead() == null ? 0 : (request.getIsLead() == 1 ? 1 : 0));
        participant.setRemark(StringUtils.trimToNull(request.getRemark()));
        participant.setStatus(1);
        participant.setDeleted(0);
        participant.setCreatedBy(loginUser.getUserId());
        projectParticipantMapper.insert(participant);
        return getById(participant.getId(), loginUser);
    }

    public PmhubProjectParticipant loadEntity(Long id, Long tenantId) {
        PmhubProjectParticipant participant = projectParticipantMapper.selectOne(new LambdaQueryWrapper<PmhubProjectParticipant>()
            .eq(PmhubProjectParticipant::getId, id)
            .eq(PmhubProjectParticipant::getTenantId, tenantId)
            .eq(PmhubProjectParticipant::getDeleted, 0)
            .last("limit 1"));
        if (participant == null) {
            throw new BusinessException("项目参建关系不存在");
        }
        return participant;
    }

    private PmhubProjectParticipantResponse toResponse(PmhubProjectParticipant item, Long tenantId) {
        PmhubProject project = projectLookupService.loadEntity(item.getProjectId(), tenantId);
        PmhubParticipantCompany company = participantCompanyService.loadEntity(item.getParticipantCompanyId(), tenantId);
        PmhubProjectParticipantResponse response = new PmhubProjectParticipantResponse();
        response.setId(item.getId());
        response.setTenantId(item.getTenantId());
        response.setProjectId(item.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setParticipantCompanyId(item.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setParticipantRole(item.getParticipantRole());
        PmhubParticipantRole participantRole = PmhubParticipantRole.fromCode(item.getParticipantRole());
        response.setParticipantRoleLabel(participantRole == null ? null : participantRole.getLabel());
        response.setIsLead(item.getIsLead());
        response.setStatus(item.getStatus());
        PmhubRecordStatus status = PmhubRecordStatus.fromCode(item.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        response.setRemark(item.getRemark());
        return response;
    }
}
