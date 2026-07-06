package com.hardrockunion.platform.iam.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.iam.domain.entity.IamUserInfo;
import com.hardrockunion.platform.iam.mapper.IamUserInfoMapper;

@Service
@DS("master")
public class IamUserInfoService {

    private final IamUserInfoMapper iamUserInfoMapper;

    public IamUserInfoService(IamUserInfoMapper iamUserInfoMapper) {
        this.iamUserInfoMapper = iamUserInfoMapper;
    }

    public IamUserInfo getActiveUserInfo(Long appId, Long userId) {
        if (appId == null || userId == null) {
            return null;
        }
        return iamUserInfoMapper.selectOne(new LambdaQueryWrapper<IamUserInfo>()
            .eq(IamUserInfo::getAppId, appId)
            .eq(IamUserInfo::getUserId, userId)
            .eq(IamUserInfo::getDeleted, 0)
            .last("limit 1"));
    }

    private IamUserInfo getAnyUserInfo(Long appId, Long userId) {
        if (appId == null || userId == null) {
            return null;
        }
        return iamUserInfoMapper.selectOne(new LambdaQueryWrapper<IamUserInfo>()
            .eq(IamUserInfo::getAppId, appId)
            .eq(IamUserInfo::getUserId, userId)
            .last("limit 1"));
    }

    @Transactional(rollbackFor = Exception.class)
    public IamUserInfo ensureBlankProfile(Long appId, Long userId) {
        return upsertProfile(appId, userId, StringUtils.EMPTY, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamUserInfo upsertProfile(Long appId, Long userId, String nickName, String avatarUrl) {
        if (appId == null || userId == null) {
            throw new BusinessException("appId、userId 不能为空");
        }
        String normalizedNickName = StringUtils.trimToEmpty(nickName);
        String normalizedAvatarUrl = StringUtils.trimToNull(avatarUrl);
        IamUserInfo userInfo = getAnyUserInfo(appId, userId);
        if (userInfo == null) {
            userInfo = new IamUserInfo();
            userInfo.setAppId(appId);
            userInfo.setUserId(userId);
            userInfo.setNickName(normalizedNickName);
            userInfo.setAvatarUrl(normalizedAvatarUrl);
            userInfo.setDeleted(0);
            iamUserInfoMapper.insert(userInfo);
            return userInfo;
        }
        userInfo.setAppId(appId);
        userInfo.setNickName(normalizedNickName);
        userInfo.setAvatarUrl(normalizedAvatarUrl);
        userInfo.setDeleted(0);
        iamUserInfoMapper.updateById(userInfo);
        return userInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamUserInfo saveRequiredProfile(Long appId, Long userId, String nickName, String avatarUrl) {
        String normalizedNickName = StringUtils.trimToNull(nickName);
        if (normalizedNickName == null) {
            throw new BusinessException("nickName 不能为空");
        }
        return upsertProfile(appId, userId, normalizedNickName, avatarUrl);
    }

    public String resolveNickName(Long appId, Long userId) {
        IamUserInfo userInfo = getActiveUserInfo(appId, userId);
        return userInfo == null ? null : userInfo.getNickName();
    }

    public String resolveAvatarUrl(Long appId, Long userId) {
        IamUserInfo userInfo = getActiveUserInfo(appId, userId);
        return userInfo == null ? null : userInfo.getAvatarUrl();
    }
}
