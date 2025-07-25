package com.xik.aibookkeeping.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDTO implements Serializable {
    //用户id
    private Long id;

    //昵称
    private String nickname;

    //头像
    private String avatar;

    //性别
    private String sex;

    //密码
    private String password;

    // 账号
    private String account;
}
