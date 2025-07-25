package com.xik.aibookkeeping.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/***
 * 用户登录
 */
@Data
public class UserPhoneLoginDTO implements Serializable {
    private String phone;
    private String code;
}
