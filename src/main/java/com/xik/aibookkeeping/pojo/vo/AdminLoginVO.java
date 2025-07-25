package com.xik.aibookkeeping.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 传递的实体类 修改 新增
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 管理员id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 管理员角色
     */
    private String role;

    /**
     * 登录的token
     */
    private String token;
}
