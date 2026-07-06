package com.hardrockunion.platform.iam.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;
import com.hardrockunion.platform.iam.domain.entity.IamUser;
import com.hardrockunion.platform.iam.mapper.IamTenantMemberMapper;

@Service
public class IamTenantMemberService {

    private final IamTenantMemberMapper iamTenantMemberMapper;

    public IamTenantMemberService(IamTenantMemberMapper iamTenantMemberMapper) {
        this.iamTenantMemberMapper = iamTenantMemberMapper;
    }

    public IamTenantMember getActiveMember(Long appId, Long tenantId, Long userId) {
        if (appId == null || tenantId == null || userId == null) {
            return null;
        }
        return iamTenantMemberMapper.selectOne(new LambdaQueryWrapper<IamTenantMember>()
            .eq(IamTenantMember::getAppId, appId)
            .eq(IamTenantMember::getTenantId, tenantId)
            .eq(IamTenantMember::getUserId, userId)
            .eq(IamTenantMember::getDeleted, 0)
            .eq(IamTenantMember::getMemberStatus, "ACTIVE")
            .last("limit 1"));
    }

    public IamTenantMember ensureActiveMember(IamUser user, Long appId, Long tenantId, boolean allowBackfill) {
        if (user == null || user.getId() == null || appId == null || tenantId == null) {
            return null;
        }
        IamTenantMember member = getActiveMember(appId, tenantId, user.getId());
        return member;
    }

    public IamTenantMember upsertActiveMember(Long appId, Long tenantId, Long userId, boolean primary) {
        return upsertMember(appId, tenantId, userId, "ACTIVE", primary, LocalDateTime.now(), null);
    }

    public IamTenantMember upsertMember(Long appId,
                                        Long tenantId,
                                        Long userId,
                                        String memberStatus,
                                        boolean primary,
                                        LocalDateTime joinedAt,
                                        String remark) {
        if (appId == null || tenantId == null || userId == null) {
            return null;
        }
        IamTenantMember member = iamTenantMemberMapper.selectOne(new LambdaQueryWrapper<IamTenantMember>()
            .eq(IamTenantMember::getAppId, appId)
            .eq(IamTenantMember::getTenantId, tenantId)
            .eq(IamTenantMember::getUserId, userId)
            .last("limit 1"));
        if (member == null) {
            member = new IamTenantMember();
            member.setAppId(appId);
            member.setTenantId(tenantId);
            member.setUserId(userId);
            member.setMemberStatus(memberStatus);
            member.setIsPrimary(primary ? 1 : 0);
            member.setJoinedAt(joinedAt);
            member.setRemark(remark);
            member.setDeleted(0);
            iamTenantMemberMapper.insert(member);
            return member;
        }
        member.setAppId(appId);
        member.setMemberStatus(memberStatus);
        member.setIsPrimary(primary ? 1 : 0);
        if (joinedAt != null) {
            member.setJoinedAt(joinedAt);
        }
        member.setRemark(remark);
        member.setDeleted(0);
        iamTenantMemberMapper.updateById(member);
        return member;
    }

    public void setPrimaryMember(Long appId, Long userId, Long tenantId) {
        if (appId == null || userId == null || tenantId == null) {
            return;
        }
        List<IamTenantMember> members = iamTenantMemberMapper.selectList(new LambdaQueryWrapper<IamTenantMember>()
            .eq(IamTenantMember::getAppId, appId)
            .eq(IamTenantMember::getUserId, userId)
            .eq(IamTenantMember::getDeleted, 0));
        for (IamTenantMember member : members) {
            member.setAppId(appId);
            member.setIsPrimary(tenantId.equals(member.getTenantId()) ? 1 : 0);
            if (tenantId.equals(member.getTenantId())) {
                member.setMemberStatus("ACTIVE");
            }
            iamTenantMemberMapper.updateById(member);
        }
    }

    public List<Long> listActiveUserIds(Long appId, Long tenantId) {
        if (appId == null || tenantId == null) {
            return List.of();
        }
        return iamTenantMemberMapper.selectList(new LambdaQueryWrapper<IamTenantMember>()
                .eq(IamTenantMember::getAppId, appId)
                .eq(IamTenantMember::getTenantId, tenantId)
                .eq(IamTenantMember::getDeleted, 0)
                .eq(IamTenantMember::getMemberStatus, "ACTIVE"))
            .stream()
            .map(IamTenantMember::getUserId)
            .distinct()
            .toList();
    }

    public List<IamTenantMember> listActiveMembersByUser(Long appId, Long userId) {
        if (appId == null || userId == null) {
            return List.of();
        }
        return iamTenantMemberMapper.selectList(new LambdaQueryWrapper<IamTenantMember>()
                .eq(IamTenantMember::getAppId, appId)
                .eq(IamTenantMember::getUserId, userId)
                .eq(IamTenantMember::getDeleted, 0)
                .eq(IamTenantMember::getMemberStatus, "ACTIVE")
                .orderByDesc(IamTenantMember::getIsPrimary)
                .orderByAsc(IamTenantMember::getId));
    }

    public List<IamTenantMember> listMembersByTenant(Long appId, Long tenantId) {
        if (appId == null || tenantId == null) {
            return List.of();
        }
        return iamTenantMemberMapper.selectList(new LambdaQueryWrapper<IamTenantMember>()
            .eq(IamTenantMember::getAppId, appId)
            .eq(IamTenantMember::getTenantId, tenantId)
            .eq(IamTenantMember::getDeleted, 0)
            .orderByDesc(IamTenantMember::getIsPrimary)
            .orderByDesc(IamTenantMember::getJoinedAt)
            .orderByDesc(IamTenantMember::getId));
    }

    public IamTenantMember getMemberById(Long appId, Long tenantId, Long memberId) {
        if (appId == null || tenantId == null || memberId == null) {
            return null;
        }
        return iamTenantMemberMapper.selectOne(new LambdaQueryWrapper<IamTenantMember>()
            .eq(IamTenantMember::getAppId, appId)
            .eq(IamTenantMember::getTenantId, tenantId)
            .eq(IamTenantMember::getId, memberId)
            .eq(IamTenantMember::getDeleted, 0)
            .last("limit 1"));
    }

    public void save(IamTenantMember member) {
        if (member == null || member.getId() == null) {
            return;
        }
        iamTenantMemberMapper.updateById(member);
    }
}
