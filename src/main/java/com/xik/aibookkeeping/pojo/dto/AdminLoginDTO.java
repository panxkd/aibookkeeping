package com.xik.aibookkeeping.pojo.dto;


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
public class AdminLoginDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
