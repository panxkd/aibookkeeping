package com.xik.aibookkeeping.pojo.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用于保存用户上下文下信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
    private Long userId;
    private String username;
    private String userType;
    private String token;

}

