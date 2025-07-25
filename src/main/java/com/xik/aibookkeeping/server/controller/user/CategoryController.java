package com.xik.aibookkeeping.server.controller.user;


import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.CategoryDTO;
import com.xik.aibookkeeping.pojo.entity.Category;
import com.xik.aibookkeeping.server.service.ICategoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 账单分类表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("UserCategoryController")
@RequestMapping("/user/category")
@Slf4j
public class CategoryController {

    @Resource
    private ICategoryService categoryService;

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Category> getCategory(@PathVariable Long id) {
        log.info("用户查询的分类：{}", id);
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     *
     *  类型（revenue收入/expenditures支出/notRecorded不记入收支）
     * @param type
     * @return
     */
    @GetMapping("/type/{type}")
    public Result<List<Category>> getCategoryByType(@PathVariable String type) {
        log.info("查询的类型：{}", type);
        List<Category> categorieList = categoryService.getCategoryUserByType(type);
        return Result.success(categorieList);
    }

    @PostMapping
    public Result<Category> addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("用户添加的分类：{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        categoryService.addCategoryUser(category);
        return Result.success();
    }

    @PutMapping
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("用户修改的分类：{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        categoryService.updateCategoryUser(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        log.info("用户删除的分类");
        categoryService.removeCategoryUserById(id);
        return Result.success();
    }
}
