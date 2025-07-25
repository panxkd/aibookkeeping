package com.xik.aibookkeeping.server.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.constant.ImageConstant;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.exception.AdminException;
import com.xik.aibookkeeping.common.exception.CategoryException;
import com.xik.aibookkeeping.pojo.dto.CategoryDTO;
import com.xik.aibookkeeping.pojo.dto.CategoryPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Admin;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.entity.Category;
import com.xik.aibookkeeping.server.mapper.BillMapper;
import com.xik.aibookkeeping.server.mapper.CategoryMapper;
import com.xik.aibookkeeping.server.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 账单分类表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Resource
    private BillMapper billMapper;

    @Override
    public Page<Category> pageCategory(CategoryPageQueryDTO categoryPageQueryDTO) {
        if (categoryPageQueryDTO == null || categoryPageQueryDTO.getPage() == 0 || categoryPageQueryDTO.getPageSize() == 0) {
            throw new CategoryException(MessageConstant.CATEGORY_PAGE_QUERY_NOT_NULL);
        }
        try {
            Page<Category> pageQuery = Page.of(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
            // 构建查询条件
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            // 添加模糊查询
            if (StringUtils.isNoneBlank(categoryPageQueryDTO.getCategory())) {
                queryWrapper.like(Category::getCategory, categoryPageQueryDTO.getCategory());
            }
            if (StringUtils.isNoneBlank(categoryPageQueryDTO.getDescription())) {
                queryWrapper.like(Category::getDescription, categoryPageQueryDTO.getDescription());
            }
            if (StringUtils.isNoneBlank(categoryPageQueryDTO.getType())) {
                queryWrapper.like(Category::getType, categoryPageQueryDTO.getType());
            }
            if (categoryPageQueryDTO.getId()  != null) {
                queryWrapper.like(Category::getId, categoryPageQueryDTO.getId());
            }
            // 查询未被删除的 并且按照时间排序
            queryWrapper.eq(Category::getIsDeleted, 0).orderByDesc(Category::getCreateTime);

            return this.page(pageQuery, queryWrapper);
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_PAGE_QUERY_ERR + e.getMessage());
        }
    }

    @Override
    public Category getCategoryById(Long id) {
        try {
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getId, id).eq(Category::getIsDeleted, 0);
            return this.getOne(queryWrapper);
        } catch (Exception e){
            throw new CategoryException(MessageConstant.CATEGORY_QUERY_NOT_NULL + e.getMessage());
        }

    }

    @Override
    @OperationLog(OperationType.ADMIN_INSERT)
    public void saveCategoryByAdmin(Category category) {
        try {
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getIsDeleted, 0).eq(Category::getCategory, category.getCategory()).eq(Category::getType, category.getType());
            Category CategoryByCategory = this.getOne(queryWrapper);
            if (CategoryByCategory != null) {
                throw new CategoryException(MessageConstant.CATEGORY_FOUND);
            }
            this.save(category);
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_INSERT_ERR + e.getMessage());
        }
    }

    @Override
    @OperationLog(OperationType.ADMIN_UPDATE)
    public void updateCategory(Category category) {
        try {
            // 查询当前数据库中已有的分类
            Category originalCategory = this.getById(category.getId());
            if (originalCategory == null) {
                throw new CategoryException("待更新的分类不存在");
            }

            boolean isNameChanged = !Objects.equals(originalCategory.getCategory(), category.getCategory());
            boolean isTypeChanged = !Objects.equals(originalCategory.getType(), category.getType());

            // 情况一：修改了“分类名”或“类型”，都需要校验是否冲突
            if (isNameChanged || isTypeChanged) {
                LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Category::getCategory, category.getCategory())
                        .eq(Category::getType, category.getType())
                        .eq(Category::getIsDeleted, 0)
                        .ne(Category::getId, category.getId()); // 排除自己

                Category conflict = this.getOne(queryWrapper);
                if (conflict != null) {
                    throw new CategoryException("该类型下已存在相同名称的分类");
                }
            }

            // 情况二：仅修改图标，不校验分类名重复
            this.updateById(category);
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_UPDATE_ERR + e.getMessage());
        }
    }


    @Override
    public void removeCategoryById(Long id) {
        try {
            LambdaQueryWrapper<Bill> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Bill::getCategoryId, id).eq(Bill::getIsDeleted, 0);
            List<Bill> categoryList = billMapper.selectList(queryWrapper);
            if (!categoryList.isEmpty()) {
                // 不能删除该分类下有账单的分类
                throw new CategoryException(MessageConstant.CATEGORY_BILL_DELETE_ERR);
            }
            LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Category::getIsDeleted, 1).eq(Category::getId, id);
            this.update(null, updateWrapper);
        }  catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_DELETE_ERR + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCategoryByIds(List<Long> ids) {
        // 1. 参数校验
        if (CollectionUtils.isEmpty(ids)) {
            throw new CategoryException(MessageConstant.CATEGORY_DELETE_IDS_NOT_NULL);
        }
        // 2. 查询所有待删除分类中，哪些分类下有未删除的账单
        LambdaQueryWrapper<Bill> billQueryWrapper = new LambdaQueryWrapper<>();
        billQueryWrapper
                .in(Bill::getCategoryId, ids) // 一次性查询所有分类
                .eq(Bill::getIsDeleted, 0);  // 未删除的账单
        List<Bill> bills = billMapper.selectList(billQueryWrapper);

        // 3. 如果有账单，找出关联的分类ID并报错
        if (!bills.isEmpty()) {
            // 提取有账单的分类ID（去重）
            Set<Long> usedCategoryIds = bills.stream()
                    .map(Bill::getCategoryId)
                    .collect(Collectors.toSet());

            // 构造错误信息（如："分类ID[1,2]下存在账单，无法删除"）
            String errorMsg = String.format(
                    MessageConstant.CATEGORY_BILL_DELETE_ERR,
                    usedCategoryIds
            );
            throw new CategoryException(errorMsg);
        }
        try {
            // 4. 逻辑删除分类（批量更新）
            List<Category> entities = ids.stream()
                    .map(id -> new Category().setId(id).setIsDeleted(1))
                    .collect(Collectors.toList());
            this.updateBatchById(entities); // 批量更新
        }  catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_DELETE_IDS_ERR);
        }
    }

    @Override
    @OperationLog(OperationType.ADMIN_UPDATE)
    public void updateCategoryStatus(Category category) {
        try {
            this.updateById(category);
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_UPDATE_ERR + e.getMessage());
        }
    }

    /**
     * 用户根据类型查询
     * @param type
     * @return
     */
    @Override
    public List<Category> getCategoryUserByType(String type) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

            queryWrapper.eq(Category::getIsDeleted, 0)
                    .eq(Category::getStatus, 1)
                    .and(wrapper -> wrapper
                            .eq(Category::getIsSystem, 1)  // 系统分类（所有用户可见）
                            .or()
                            .eq(Category::getUserId, currentUserId)  // 或当前用户自己的分类
                    );
            if (type != null) {
                queryWrapper.eq(Category::getType, type);  // 按类型筛选（可选）
            }
            // 添加 isSystem 倒序排序（系统分类在前）
            queryWrapper.orderByDesc(Category::getIsSystem);
            return this.list(queryWrapper);
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_QUERY_NOT_NULL + e.getMessage());
        }
    }

    /**
     * 用户添加自己的分类
     * @param category
     */
    @Override
    @OperationLog(OperationType.USER_INSERT)
    public void addCategoryUser(Category category) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getCategory, category.getCategory()).eq(Category::getIsDeleted, 0);
            Category CategoryByCategory = this.getOne(queryWrapper);
            if (CategoryByCategory != null) {
                throw new CategoryException(MessageConstant.CATEGORY_FOUND);
            }
            category.setUserId(currentUserId).setIsSystem(0);
            this.save(category);
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_INSERT_ERR + e.getMessage());
        }
    }

    /**
     * 用户更新自己的分类
     * @param category
     */
    @Override
    @OperationLog(OperationType.USER_UPDATE)
    public void updateCategoryUser(Category category) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getCategory, category.getCategory()).eq(Category::getIsDeleted, 0);
            Category CategoryByCategory = this.getOne(queryWrapper);
            if (CategoryByCategory != null) {
                throw new CategoryException(MessageConstant.CATEGORY_FOUND);
            }
            this.updateById(category);
        }  catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_UPDATE_ERR + e.getMessage());
        }
    }

    @Override
    public long getNumber() {
        try {
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getIsDeleted, 0);
            return baseMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_QUERY_NOT_NULL + e.getMessage());
        }
    }

    /**
     * 删除该分类
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCategoryUserById(Long id) {
        try {
            // 查询待删除分类是否存在
            Category categoryByCategory = this.getOne(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getId, id)
                            .eq(Category::getIsDeleted, 0)
            );
            if (categoryByCategory == null) {
                throw new CategoryException("要删除的分类不存在！");
            }
            if (Integer.valueOf(1).equals(categoryByCategory.getIsSystem())) {
                throw new CategoryException(MessageConstant.SYSTEM_CATEGORY);
            }
            // 查找对应类型的“其他”分类
            Category otherCategory = this.getOne(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getType, categoryByCategory.getType())
                            .eq(Category::getCategory, "其他"),
                    false
            );
            if (otherCategory == null) {
                throw new CategoryException("未找到类型为 " + categoryByCategory.getType() + " 的“其他”分类");
            }
            // 迁移账单到“其他”分类
            billMapper.update(
                    new LambdaUpdateWrapper<Bill>()
                            .eq(Bill::getCategoryId, id)
                            .eq(Bill::getIsDeleted, 0)
                            .set(Bill::getCategoryId, otherCategory.getId())
            );
            // 删除分类（软删）
            this.update(null,
                    new LambdaUpdateWrapper<Category>()
                            .eq(Category::getId, id)
                            .set(Category::getIsDeleted, 1)
            );
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.CATEGORY_DELETE_ERR + e.getMessage());
        }

    }


}
