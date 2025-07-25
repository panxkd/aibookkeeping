package com.xik.aibookkeeping.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountLoginVO implements Serializable {

    private Long id;
    private String token;   //生成的token
    private String account;  //用户账号
    private String nickname;  //用户昵称
    private String avatar; //用户头像
}
