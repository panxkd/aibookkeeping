package com.xik.aibookkeeping.server.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.CategoryDTO;
import com.xik.aibookkeeping.pojo.dto.CategoryPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Category;
import com.xik.aibookkeeping.server.service.ICategoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 账单分类表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminCategoryController")
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {

    @Resource
    private ICategoryService categoryService;

    /**
     * 分页查询分类
     * @return
     */
    @GetMapping("/page")
    public Result<Page<Category>> page(CategoryPageQueryDTO  categoryPageQueryDTO) {
        log.info("分页查询分类：{}", categoryPageQueryDTO);
        Page<Category> page = categoryService.pageCategory(categoryPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 根据id获取分类
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        log.info("分类id:{}", id);
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @PostMapping
    public Result addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("添加的分类：{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryService.saveCategoryByAdmin(category);
        return Result.success();
    }

    @PutMapping
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改的分类{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryService.updateCategory(category);
        return Result.success();
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        log.info("删除分类的id:{}", id);
        categoryService.removeCategoryById(id);
        return Result.success();
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result deleteCategories(@RequestBody List<Long> ids) {
        log.info("批量删除分类的ids: {}", ids);
        categoryService.removeCategoryByIds(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result updateCategoryStatus(@PathVariable Integer status, Long id) {
        log.info("修改分类状态：{}，id:{}",status, id);
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryService.updateCategoryStatus(category);
        return Result.success();
    }

    /**
     * 获取总体数
     * @return
     */
    @GetMapping("/number")
    public Result<Long> getCategoryNumber() {
        log.info("获取分类总条数");
        long number = categoryService.getNumber();
        return Result.success(number);
    }

}
