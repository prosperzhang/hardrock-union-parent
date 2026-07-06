package com.hardrockunion.business.merchant.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.merchant.domain.entity.MerchantCategory;
import com.hardrockunion.business.merchant.domain.entity.MerchantProduct;
import com.hardrockunion.business.merchant.dto.MerchantCategoryCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantCategoryQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantCategoryResponse;
import com.hardrockunion.business.merchant.dto.MerchantCategoryUpdateRequest;
import com.hardrockunion.business.merchant.mapper.MerchantCategoryMapper;
import com.hardrockunion.business.merchant.mapper.MerchantProductMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

@Service
public class MerchantCategoryService {

    private static final DateTimeFormatter CATEGORY_CODE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MerchantCategoryMapper categoryMapper;
    private final MerchantProductMapper productMapper;
    private final MerchantAccessGuard merchantAccessGuard;

    public MerchantCategoryService(MerchantCategoryMapper categoryMapper,
                                   MerchantProductMapper productMapper,
                                   MerchantAccessGuard merchantAccessGuard) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.merchantAccessGuard = merchantAccessGuard;
    }

    public PageResponse<MerchantCategoryResponse> list(MerchantCategoryQueryRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantCategoryQueryRequest query = request == null ? new MerchantCategoryQueryRequest() : request;
        LambdaQueryWrapper<MerchantCategory> wrapper = new LambdaQueryWrapper<MerchantCategory>()
            .eq(MerchantCategory::getTenantId, loginUser.getTenantId())
            .eq(MerchantCategory::getDeleted, 0);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            wrapper.and(w -> w.like(MerchantCategory::getCategoryCode, keyword)
                .or()
                .like(MerchantCategory::getCategoryName, keyword));
        }
        if (query.getStatus() != null) {
            ensureStatus(query.getStatus());
            wrapper.eq(MerchantCategory::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(MerchantCategory::getSortNo)
            .orderByAsc(MerchantCategory::getId);
        Page<MerchantCategory> page = categoryMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        var responsePage = page.convert(this::toResponse);
        return PageResponse.from(responsePage);
    }

    public MerchantCategoryResponse create(MerchantCategoryCreateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (StringUtils.isBlank(request.getCategoryName())) {
            throw new BusinessException("categoryName 不能为空");
        }

        String categoryCode = resolveCategoryCode(loginUser.getTenantId(), request.getCategoryCode());
        String categoryName = normalizeAndEnsureCategoryName(loginUser.getTenantId(), request.getCategoryName(), null);
        Long parentId = resolveParentId(loginUser.getTenantId(), request.getParentId(), null);
        MerchantCategory existed = findByCode(loginUser.getTenantId(), categoryCode);
        if (existed != null) {
            throw new BusinessException("分类编码已存在");
        }

        MerchantCategory category = new MerchantCategory();
        category.setTenantId(loginUser.getTenantId());
        category.setCategoryCode(categoryCode);
        category.setCategoryName(categoryName);
        category.setParentId(parentId);
        category.setSortNo(normalizeSortNo(request.getSortNo()));
        category.setStatus(1);
        category.setCreatedBy(loginUser.getUserId());
        category.setDeleted(0);
        categoryMapper.insert(category);
        return toResponse(category);
    }

    public MerchantCategoryResponse update(Long id, MerchantCategoryUpdateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getCategoryName())) {
            throw new BusinessException("categoryName 不能为空");
        }
        MerchantCategory category = loadEntity(id, loginUser.getTenantId());
        String categoryName = normalizeAndEnsureCategoryName(loginUser.getTenantId(), request.getCategoryName(), category.getId());
        Long parentId = resolveParentId(loginUser.getTenantId(), request.getParentId(), category.getId());
        category.setCategoryName(categoryName);
        category.setParentId(parentId);
        category.setSortNo(normalizeSortNo(request.getSortNo()));
        if (request.getStatus() != null) {
            ensureStatus(request.getStatus());
            if (request.getStatus() == 0 && (category.getStatus() == null || category.getStatus() != 0)) {
                ensureCanDisable(category, loginUser.getTenantId());
            }
            category.setStatus(request.getStatus());
        }
        categoryMapper.updateById(category);
        return toResponse(category);
    }

    public void remove(Long id, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantCategory category = loadEntity(id, loginUser.getTenantId());
        Long childCount = countChildren(loginUser.getTenantId(), category.getId());
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该分类下还有子分类，不能删除");
        }
        Long productCount = countProducts(loginUser.getTenantId(), category.getCategoryCode());
        if (productCount != null && productCount > 0) {
            throw new BusinessException("该分类下还有商品，不能删除");
        }
        categoryMapper.deleteById(category.getId());
    }

    public MerchantCategory findEnabledByCode(Long tenantId, String categoryCode) {
        if (tenantId == null || StringUtils.isBlank(categoryCode)) {
            return null;
        }
        return categoryMapper.selectOne(new LambdaQueryWrapper<MerchantCategory>()
            .eq(MerchantCategory::getTenantId, tenantId)
            .eq(MerchantCategory::getCategoryCode, StringUtils.upperCase(StringUtils.trim(categoryCode)))
            .eq(MerchantCategory::getDeleted, 0)
            .eq(MerchantCategory::getStatus, 1)
            .last("limit 1"));
    }

    private MerchantCategory findByCode(Long tenantId, String categoryCode) {
        return categoryMapper.selectOne(new LambdaQueryWrapper<MerchantCategory>()
            .eq(MerchantCategory::getTenantId, tenantId)
            .eq(MerchantCategory::getCategoryCode, categoryCode)
            .eq(MerchantCategory::getDeleted, 0)
            .last("limit 1"));
    }

    private String resolveCategoryCode(Long tenantId, String requestedCategoryCode) {
        if (StringUtils.isNotBlank(requestedCategoryCode)) {
            return StringUtils.upperCase(StringUtils.trim(requestedCategoryCode));
        }
        for (int i = 0; i < 5; i++) {
            String generatedCode = "CAT" + LocalDateTime.now().format(CATEGORY_CODE_TIME_FORMATTER)
                + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (findByCode(tenantId, generatedCode) == null) {
                return generatedCode;
            }
        }
        throw new BusinessException("分类编码生成失败，请重试");
    }

    private Long resolveParentId(Long tenantId, Long requestedParentId, Long currentCategoryId) {
        Long parentId = requestedParentId == null ? 0L : requestedParentId;
        if (parentId == 0L) {
            return 0L;
        }
        if (parentId < 0) {
            throw new BusinessException("parentId 不能小于 0");
        }
        if (currentCategoryId != null && parentId.equals(currentCategoryId)) {
            throw new BusinessException("父级分类不能是自己");
        }

        MerchantCategory parent = loadEntity(parentId, tenantId);
        if (parent.getStatus() == null || parent.getStatus() != 1) {
            throw new BusinessException("父级分类不可用");
        }
        int depth = 0;
        while (currentCategoryId != null && parent.getParentId() != null && parent.getParentId() != 0L) {
            if (parent.getParentId().equals(currentCategoryId)) {
                throw new BusinessException("父级分类不能是自己的子分类");
            }
            if (++depth > 50) {
                throw new BusinessException("分类层级过深");
            }
            parent = loadEntity(parent.getParentId(), tenantId);
        }
        return parentId;
    }

    private String normalizeAndEnsureCategoryName(Long tenantId, String categoryName, Long currentCategoryId) {
        String normalizedCategoryName = StringUtils.trimToNull(categoryName);
        if (normalizedCategoryName == null) {
            throw new BusinessException("categoryName 不能为空");
        }
        LambdaQueryWrapper<MerchantCategory> wrapper = new LambdaQueryWrapper<MerchantCategory>()
            .eq(MerchantCategory::getTenantId, tenantId)
            .eq(MerchantCategory::getCategoryName, normalizedCategoryName)
            .eq(MerchantCategory::getDeleted, 0)
            .last("limit 1");
        if (currentCategoryId != null) {
            wrapper.ne(MerchantCategory::getId, currentCategoryId);
        }
        MerchantCategory existed = categoryMapper.selectOne(wrapper);
        if (existed != null) {
            throw new BusinessException("分类名称已存在");
        }
        return normalizedCategoryName;
    }

    private Integer normalizeSortNo(Integer sortNo) {
        if (sortNo == null) {
            return 0;
        }
        if (sortNo < 0) {
            throw new BusinessException("sortNo 不能小于 0");
        }
        return sortNo;
    }

    private void ensureCanDisable(MerchantCategory category, Long tenantId) {
        Long childCount = countChildren(tenantId, category.getId());
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该分类下还有子分类，不能停用");
        }
        Long productCount = countProducts(tenantId, category.getCategoryCode());
        if (productCount != null && productCount > 0) {
            throw new BusinessException("该分类下还有商品，不能停用");
        }
    }

    private Long countChildren(Long tenantId, Long categoryId) {
        return categoryMapper.selectCount(new LambdaQueryWrapper<MerchantCategory>()
            .eq(MerchantCategory::getTenantId, tenantId)
            .eq(MerchantCategory::getParentId, categoryId)
            .eq(MerchantCategory::getDeleted, 0));
    }

    private Long countProducts(Long tenantId, String categoryCode) {
        return productMapper.selectCount(new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getTenantId, tenantId)
            .eq(MerchantProduct::getCategoryCode, categoryCode)
            .eq(MerchantProduct::getDeleted, 0));
    }

    private MerchantCategory loadEntity(Long id, Long tenantId) {
        MerchantCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<MerchantCategory>()
            .eq(MerchantCategory::getId, id)
            .eq(MerchantCategory::getTenantId, tenantId)
            .eq(MerchantCategory::getDeleted, 0)
            .last("limit 1"));
        if (category == null) {
            throw new BusinessException("商品分类不存在");
        }
        return category;
    }

    private void ensureStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("status 仅支持 0 或 1");
        }
    }

    private MerchantCategoryResponse toResponse(MerchantCategory category) {
        MerchantCategoryResponse response = new MerchantCategoryResponse();
        response.setId(category.getId());
        response.setTenantId(category.getTenantId());
        response.setCategoryCode(category.getCategoryCode());
        response.setCategoryName(category.getCategoryName());
        response.setParentId(category.getParentId());
        response.setSortNo(category.getSortNo());
        response.setStatus(category.getStatus());
        return response;
    }
}
