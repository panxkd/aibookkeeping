package com.xik.aibookkeeping.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/***
 * 用户登录
 */
@Data
public class UserCodeLoginDTO implements Serializable {
    private String phone;

}
