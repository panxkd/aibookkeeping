package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.dto.CategoryDTO;
import com.xik.aibookkeeping.pojo.dto.CategoryPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Category;


import java.util.List;

/**
 * <p>
 * 账单分类表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface ICategoryService extends IService<Category> {

    Page<Category> pageCategory(CategoryPageQueryDTO categoryPageQueryDTO);

    Category getCategoryById(Long id);

    void saveCategoryByAdmin(Category category);

    void updateCategory(Category category);

    void removeCategoryById(Long id);

    void removeCategoryByIds(List<Long> list);

    void updateCategoryStatus(Category category);

    List<Category> getCategoryUserByType(String type);

    void addCategoryUser(Category category);

    void updateCategoryUser(Category category);


    long getNumber();

    void removeCategoryUserById(Long id);
}
