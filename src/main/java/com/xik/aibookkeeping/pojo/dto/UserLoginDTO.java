package com.xik.aibookkeeping.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/***
 * 用户登录
 */
@Data
public class UserLoginDTO implements Serializable {
    private String code;
    private String encryptedData;
    private String iv;
}
