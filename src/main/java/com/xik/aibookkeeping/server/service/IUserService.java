package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.xik.aibookkeeping.pojo.dto.*;
import com.xik.aibookkeeping.pojo.entity.User;
import com.xik.aibookkeeping.pojo.vo.UserAccountLoginVO;
import com.xik.aibookkeeping.pojo.vo.UserLoginVO;
import com.xik.aibookkeeping.pojo.vo.UserPhoneLoginVO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface IUserService extends IService<User> {

    UserLoginVO wxLogin(UserLoginDTO userLoginDTO);

    User getCurrentUser();

    void logout();

    void updateUser(UserDTO userDTO);

    User getByUserId(Long id);

    Page<User> pageUser(UserPageQueryDTO userPageQueryDTO);

    long getNumber();

    void updateRole(String role, Long id);

    UserPhoneLoginVO phoneLogin(UserPhoneLoginDTO userPhoneLoginDTO);

    SendSmsResponse sentCode(UserCodeLoginDTO userCodeLoginDTO);

    void register(UserRegisterDTO userRegisterDTO);

    UserAccountLoginVO accountLogin(UserAccountLoginDTO userAccountLoginDTO);
}
