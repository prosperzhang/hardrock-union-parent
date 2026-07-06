package com.hardrockunion.platform.iam.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamUser;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;
import com.hardrockunion.platform.iam.dto.IamTenantMemberResponse;
import com.hardrockunion.platform.iam.dto.IamTenantMemberSwitchRequest;
import com.hardrockunion.platform.iam.domain.entity.IamUserInfo;
import com.hardrockunion.platform.iam.dto.LoginRequest;
import com.hardrockunion.platform.iam.dto.LoginResponse;
import com.hardrockunion.platform.iam.dto.RegisterRequest;
import com.hardrockunion.platform.iam.mapper.IamUserMapper;

@Service
public class IamAuthService {

    private static final String WSGM_APP_CODE = "WSGM";
    private static final String WSGM_SUPER_ADMIN_ROLE_CODE = "WSGM_SUPER_ADMIN";

    private final IamUserMapper iamUserMapper;
    private final JwtTokenService jwtTokenService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamTenantMemberService iamTenantMemberService;
    private final IamUserInfoService iamUserInfoService;
    private final IamRoleQueryService iamRoleQueryService;

    public IamAuthService(IamUserMapper iamUserMapper,
                          JwtTokenService jwtTokenService,
                          AppRegistryQueryService appRegistryQueryService,
                          IamTenantMemberService iamTenantMemberService,
                          IamUserInfoService iamUserInfoService,
                          IamRoleQueryService iamRoleQueryService) {
        this.iamUserMapper = iamUserMapper;
        this.jwtTokenService = jwtTokenService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamTenantMemberService = iamTenantMemberService;
        this.iamUserInfoService = iamUserInfoService;
        this.iamRoleQueryService = iamRoleQueryService;
    }

    public LoginResponse login(String appCode, LoginRequest request) {
        if (request == null || StringUtils.isAnyBlank(request.getUsername(), request.getPassword())) {
            throw new BusinessException("username、password 不能为空");
        }
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        String normalizedAppCode = app.getAppCode();
        if (StringUtils.isNotBlank(request.getAppCode())
            && !StringUtils.equalsIgnoreCase(normalizedAppCode, request.getAppCode().trim())) {
            throw new BusinessException("请求中的 appCode 与登录入口不一致");
        }
        Long appId = app.getId();

        IamUser user = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUser>()
            .eq(IamUser::getAppId, appId)
            .eq(IamUser::getUsername, request.getUsername())
            .eq(IamUser::getDeleted, 0)
            .last("limit 1"));

        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已禁用");
        }

        String passwordHash = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!StringUtils.equalsIgnoreCase(passwordHash, user.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }

        IamTenantMember member = resolveLoginTenantMember(appId, user.getId(), request.getTenantId());
        Long tenantId = member == null ? null : member.getTenantId();
        ensureAppAccessible(app, user.getId(), tenantId);

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setAppCode(user.getAppCode());
        response.setTenantId(tenantId);
        response.setUsername(user.getUsername());
        IamUserInfo profile = iamUserInfoService.getActiveUserInfo(appId, user.getId());
        response.setNickName(profile == null ? null : profile.getNickName());
        response.setAvatarUrl(profile == null ? null : profile.getAvatarUrl());
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenService.getExpireSeconds());
        response.setAccessToken(jwtTokenService.createToken(user.getId(), user.getAppCode(), tenantId, user.getUsername()));
        return response;
    }

    public LoginResponse register(String appCode, RegisterRequest request) {
        if (request == null || StringUtils.isAnyBlank(request.getUsername(), request.getPassword())) {
            throw new BusinessException("username、password 不能为空");
        }
        String normalizedAppCode = normalizeAppCode(appCode);
        if (StringUtils.isNotBlank(request.getAppCode())
            && !StringUtils.equalsIgnoreCase(normalizedAppCode, StringUtils.trim(request.getAppCode()))) {
            throw new BusinessException("请求中的 appCode 与注册入口不一致");
        }
        Long appId = getAppId(normalizedAppCode);
        String username = StringUtils.trim(request.getUsername());
        String password = StringUtils.trim(request.getPassword());
        IamUser existed = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUser>()
            .eq(IamUser::getAppId, appId)
            .eq(IamUser::getUsername, username)
            .eq(IamUser::getDeleted, 0)
            .last("limit 1"));
        if (existed != null) {
            throw new BusinessException("账号已存在");
        }

        IamUser user = new IamUser();
        user.setAppId(appId);
        user.setAppCode(normalizedAppCode);
        user.setUsername(username);
        user.setPasswordHash(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setStatus(1);
        user.setDeleted(0);
        iamUserMapper.insert(user);
        iamUserInfoService.ensureBlankProfile(appId, user.getId());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setAppCode(user.getAppCode());
        response.setTenantId(null);
        response.setUsername(user.getUsername());
        response.setNickName(StringUtils.EMPTY);
        response.setAvatarUrl(null);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenService.getExpireSeconds());
        response.setAccessToken(jwtTokenService.createToken(user.getId(), user.getAppCode(), null, user.getUsername()));
        return response;
    }

    public List<IamTenantMemberResponse> listCurrentUserTenants(String appCode, Long userId) {
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        String normalizedAppCode = app.getAppCode();
        Long appId = app.getId();
        IamUser user = iamUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("当前用户不存在");
        }
        if (!StringUtils.equals(normalizedAppCode, appRegistryQueryService.getAppByCode(user.getAppCode()).getAppCode())) {
            throw new BusinessException("当前用户不属于该 app");
        }
        List<IamTenantMember> members = iamTenantMemberService.listActiveMembersByUser(appId, userId);
        Long currentTenantId = members.isEmpty() ? null : members.getFirst().getTenantId();
        ensureAppAccessible(app, userId, currentTenantId);
        return members.stream()
            .map(member -> toTenantMemberResponse(appCode, member))
            .toList();
    }

    public LoginResponse switchCurrentUserTenant(String appCode, Long userId, IamTenantMemberSwitchRequest request) {
        if (request == null || request.getTenantId() == null) {
            throw new BusinessException("tenantId 不能为空");
        }
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        String normalizedAppCode = app.getAppCode();
        Long appId = app.getId();
        IamUser user = iamUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("当前用户不存在");
        }
        if (!StringUtils.equals(normalizedAppCode, appRegistryQueryService.getAppByCode(user.getAppCode()).getAppCode())) {
            throw new BusinessException("当前用户不属于该 app");
        }
        IamTenantMember member = iamTenantMemberService.getActiveMember(appId, request.getTenantId(), userId);
        if (member == null) {
            throw new BusinessException("当前用户未加入该租户");
        }
        ensureAppAccessible(app, userId, request.getTenantId());
        iamTenantMemberService.setPrimaryMember(appId, userId, request.getTenantId());
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setAppCode(user.getAppCode());
        response.setTenantId(request.getTenantId());
        response.setUsername(user.getUsername());
        IamUserInfo profile = iamUserInfoService.getActiveUserInfo(appId, userId);
        response.setNickName(profile == null ? null : profile.getNickName());
        response.setAvatarUrl(profile == null ? null : profile.getAvatarUrl());
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenService.getExpireSeconds());
        response.setAccessToken(jwtTokenService.createToken(user.getId(), user.getAppCode(), request.getTenantId(), user.getUsername()));
        return response;
    }

    private String normalizeAppCode(String appCode) {
        return appRegistryQueryService.normalizeAppCode(appCode);
    }

    private Long getAppId(String appCode) {
        return appRegistryQueryService.getEnabledAppByCode(appCode).getId();
    }

    private void ensureAppAccessible(AppRegistry app, Long userId, Long tenantId) {
        if (app == null || Integer.valueOf(1).equals(app.getStatus())) {
            return;
        }
        if (StringUtils.equalsIgnoreCase(WSGM_APP_CODE, app.getAppCode())
            && hasWsgmSuperAdminRole(app.getId(), userId, tenantId)) {
            return;
        }
        throw new BusinessException("应用已禁用");
    }

    private boolean hasWsgmSuperAdminRole(Long appId, Long userId, Long tenantId) {
        return iamRoleQueryService.listRoleEntitiesByUser(userId, appId, tenantId)
            .stream()
            .anyMatch(role -> StringUtils.equalsIgnoreCase(WSGM_SUPER_ADMIN_ROLE_CODE, role.getRoleCode()));
    }

    private IamTenantMember resolveLoginTenantMember(Long appId, Long userId, Long tenantId) {
        if (tenantId != null) {
            IamTenantMember member = iamTenantMemberService.getActiveMember(appId, tenantId, userId);
            if (member == null) {
                throw new BusinessException("账号未加入该租户");
            }
            return member;
        }
        List<IamTenantMember> members = iamTenantMemberService.listActiveMembersByUser(appId, userId);
        return members.isEmpty() ? null : members.getFirst();
    }

    private IamTenantMemberResponse toTenantMemberResponse(String appCode, IamTenantMember member) {
        IamTenantMemberResponse response = new IamTenantMemberResponse();
        response.setTenantId(member.getTenantId());
        response.setMemberStatus(member.getMemberStatus());
        response.setIsPrimary(member.getIsPrimary());
        return response;
    }
}
