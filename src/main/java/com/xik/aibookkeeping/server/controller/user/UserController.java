package com.xik.aibookkeeping.server.controller.user;


import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.*;
import com.xik.aibookkeeping.pojo.entity.User;
import com.xik.aibookkeeping.pojo.vo.UserAccountLoginVO;
import com.xik.aibookkeeping.pojo.vo.UserLoginVO;
import com.xik.aibookkeeping.pojo.vo.UserPhoneLoginVO;
import com.xik.aibookkeeping.server.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("UserUserController")
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    @Resource
    private IUserService userService;

    /**
     * 微信登陆
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> wxLogin(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户微信登录：{}，encryptedData：{}，iv:{}"
                ,userLoginDTO.getCode(),userLoginDTO.getEncryptedData(),userLoginDTO.getIv());
        UserLoginVO userLoginVO = userService.wxLogin(userLoginDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 手机号登陆
     * @param userPhoneLoginDTO
     * @return
     */
    @PostMapping("/phone")
    public Result<UserPhoneLoginVO> phoneLogin(@RequestBody UserPhoneLoginDTO userPhoneLoginDTO){
        log.info("用户登陆：{}", userPhoneLoginDTO.getPhone());
        UserPhoneLoginVO userPhoneLoginVO = userService.phoneLogin(userPhoneLoginDTO);
        return Result.success(userPhoneLoginVO);
    }

    /**
     * 手机号发送验证码
     * @param userCodeLoginDTO
     * @return
     */
    @PostMapping("/code")
    public Result<SendSmsResponse> sentCode(@RequestBody UserCodeLoginDTO userCodeLoginDTO){
        log.info("用户发送验证码：{}", userCodeLoginDTO.getPhone());
        SendSmsResponse res = userService.sentCode(userCodeLoginDTO);
        return Result.success(res);
    }

    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}，{}",userRegisterDTO.getAccount(),userRegisterDTO.getPassword());
        userService.register(userRegisterDTO);
        return Result.success();
    }

    /**
     * 账号登陆
     * @param userAccountLoginDTO
     * @return
     */
    @PostMapping("/account")
    public Result<UserAccountLoginVO> accountLogin(@RequestBody UserAccountLoginDTO userAccountLoginDTO){
        log.info("用户登陆：{}", userAccountLoginDTO.getAccount());
        UserAccountLoginVO userAccountLoginVO = userService.accountLogin(userAccountLoginDTO);
        return Result.success(userAccountLoginVO);
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping
    private Result<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result wxLogout() {
        userService.logout();
        return Result.success();
    }

    @PutMapping
    public Result<User> updateUser(@RequestBody UserDTO userDTO) {
        log.info("修改用户信息:{}",userDTO);

        userService.updateUser(userDTO);
        return Result.success();
    }

    @GetMapping("{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        log.info("获取用户信息：{}", id);
        User user = userService.getByUserId(id);
        return Result.success(user);
    }

}
