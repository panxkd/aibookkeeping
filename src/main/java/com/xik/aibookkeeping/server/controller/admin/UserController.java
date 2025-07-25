package com.xik.aibookkeeping.server.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.UserPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.User;
import com.xik.aibookkeeping.server.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminUserController")
@RequestMapping("/admin/user")
@Slf4j
public class UserController {

    @Resource
    private IUserService userService;

    @GetMapping("/page")
    public Result<Page<User>>  page(UserPageQueryDTO userPageQueryDTO) {
        log.info("分页查询用户：{}", userPageQueryDTO);
        Page<User> page = userService.pageUser(userPageQueryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        log.info("查询的用户id:{}",id);
        User user = userService.getByUserId(id);
        return Result.success(user);
    }

    /**
     * 获取总条数
     */
    @GetMapping("/number")
    public Result<Long>  getNumber(){
        log.info("获取用户总条数");
        long number = userService.getNumber();
        return Result.success(number);
    }

    @PostMapping("/role/{role}")
    public Result updateRole(@PathVariable String role, Long id){
        log.info("修改用户权限role:{},id:{}",role,id);
        userService.updateRole(role, id);
        return Result.success();
    }

}
