package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.project.domain.entity.NexisParticipantCompany;
import com.hardrockunion.business.project.domain.entity.NexisProject;
import com.hardrockunion.business.project.domain.entity.NexisProjectParticipant;
import com.hardrockunion.business.project.dto.NexisProjectParticipantCreateRequest;
import com.hardrockunion.business.project.dto.NexisProjectParticipantQueryRequest;
import com.hardrockunion.business.project.dto.NexisProjectParticipantResponse;
import com.hardrockunion.business.project.enums.NexisParticipantRole;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.mapper.NexisProjectParticipantMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 项目参建关系服务。
 *
 * <p>这一层只负责“谁参与了项目”，不负责具体标段和承包范围。
 */
@Service
public class NexisProjectParticipantService {

    private final NexisProjectParticipantMapper projectParticipantMapper;
    private final NexisProjectLookupService projectLookupService;
    private final NexisParticipantCompanyService participantCompanyService;
    private final NexisAccessGuard nexisAccessGuard;

    public NexisProjectParticipantService(NexisProjectParticipantMapper projectParticipantMapper,
                                         NexisProjectLookupService projectLookupService,
                                         NexisParticipantCompanyService participantCompanyService,
                                         NexisAccessGuard nexisAccessGuard) {
        this.projectParticipantMapper = projectParticipantMapper;
        this.projectLookupService = projectLookupService;
        this.participantCompanyService = participantCompanyService;
        this.nexisAccessGuard = nexisAccessGuard;
    }

    public PageResponse<NexisProjectParticipantResponse> list(NexisProjectParticipantQueryRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        NexisProjectParticipantQueryRequest query = request == null ? new NexisProjectParticipantQueryRequest() : request;
        LambdaQueryWrapper<NexisProjectParticipant> wrapper = new LambdaQueryWrapper<NexisProjectParticipant>()
            .eq(NexisProjectParticipant::getTenantId, loginUser.getTenantId())
            .eq(NexisProjectParticipant::getDeleted, 0)
            .orderByDesc(NexisProjectParticipant::getId);
        if (query.getParticipantCompanyId() != null) {
            wrapper.eq(NexisProjectParticipant::getParticipantCompanyId, query.getParticipantCompanyId());
        }
        if (StringUtils.isNotBlank(query.getParticipantRole())) {
            wrapper.eq(NexisProjectParticipant::getParticipantRole, StringUtils.upperCase(StringUtils.trim(query.getParticipantRole())));
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            List<Long> companyIds = participantCompanyService.findIdsByKeyword(query.getKeyword(), loginUser.getTenantId());
            if (companyIds.isEmpty()) {
                return PageResponse.from(Page.<NexisProjectParticipantResponse>of(query.getPageNum(), query.getPageSize()));
            }
            wrapper.in(NexisProjectParticipant::getParticipantCompanyId, companyIds);
        }
        Page<NexisProjectParticipant> page = projectParticipantMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(item -> toResponse(item, loginUser.getTenantId())));
    }

    public NexisProjectParticipantResponse getById(Long id, LoginUser loginUser) {
        nexisAccessGuard.ensureLogin(loginUser);
        return toResponse(loadEntity(id, loginUser.getTenantId()), loginUser.getTenantId());
    }

    public NexisProjectParticipantResponse create(NexisProjectParticipantCreateRequest request, LoginUser loginUser) {
        nexisAccessGuard.ensurePermission(loginUser, NexisPermissionCodes.PARTICIPANT_MANAGE);
        if (request == null) {
            throw new BusinessException("请求不能为空");
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

        projectLookupService.loadEntity(loginUser.getTenantId(), loginUser.getTenantId());
        participantCompanyService.loadEntity(request.getParticipantCompanyId(), loginUser.getTenantId());
        Long duplicateCount = projectParticipantMapper.selectCount(new LambdaQueryWrapper<NexisProjectParticipant>()
            .eq(NexisProjectParticipant::getTenantId, loginUser.getTenantId())
            .eq(NexisProjectParticipant::getParticipantCompanyId, request.getParticipantCompanyId())
            .eq(NexisProjectParticipant::getParticipantRole, participantRole.getCode())
            .eq(NexisProjectParticipant::getDeleted, 0));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException("该单位已按此角色加入当前项目");
        }

        NexisProjectParticipant participant = new NexisProjectParticipant();
        participant.setTenantId(loginUser.getTenantId());
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

    public NexisProjectParticipant loadEntity(Long id, Long tenantId) {
        NexisProjectParticipant participant = projectParticipantMapper.selectOne(new LambdaQueryWrapper<NexisProjectParticipant>()
            .eq(NexisProjectParticipant::getId, id)
            .eq(NexisProjectParticipant::getTenantId, tenantId)
            .eq(NexisProjectParticipant::getDeleted, 0)
            .last("limit 1"));
        if (participant == null) {
            throw new BusinessException("项目参建关系不存在");
        }
        return participant;
    }

    public void ensureActiveParticipantCompany(Long participantCompanyId, Long tenantId) {
        Long count = projectParticipantMapper.selectCount(new LambdaQueryWrapper<NexisProjectParticipant>()
            .eq(NexisProjectParticipant::getTenantId, tenantId)
            .eq(NexisProjectParticipant::getParticipantCompanyId, participantCompanyId)
            .eq(NexisProjectParticipant::getStatus, 1)
            .eq(NexisProjectParticipant::getDeleted, 0));
        if (count == null || count == 0) {
            throw new BusinessException("参建单位尚未加入当前项目");
        }
    }

    private NexisProjectParticipantResponse toResponse(NexisProjectParticipant item, Long tenantId) {
        NexisProject project = projectLookupService.loadEntity(item.getTenantId(), tenantId);
        NexisParticipantCompany company = participantCompanyService.loadEntity(item.getParticipantCompanyId(), tenantId);
        NexisProjectParticipantResponse response = new NexisProjectParticipantResponse();
        response.setId(item.getId());
        response.setTenantId(item.getTenantId());
        response.setProjectName(project.getProjectName());
        response.setParticipantCompanyId(item.getParticipantCompanyId());
        response.setParticipantCompanyName(company.getCompanyName());
        response.setParticipantRole(item.getParticipantRole());
        NexisParticipantRole participantRole = NexisParticipantRole.fromCode(item.getParticipantRole());
        response.setParticipantRoleLabel(participantRole == null ? null : participantRole.getLabel());
        response.setIsLead(item.getIsLead());
        response.setStatus(item.getStatus());
        NexisRecordStatus status = NexisRecordStatus.fromCode(item.getStatus());
        response.setStatusLabel(status == null ? null : status.getLabel());
        response.setRemark(item.getRemark());
        return response;
    }
}
