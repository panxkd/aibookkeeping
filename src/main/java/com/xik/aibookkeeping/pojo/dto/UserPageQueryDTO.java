package com.xik.aibookkeeping.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户分页查询传递的数据
 */
@Data
public class UserPageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    //昵称
    private String nickname;
    //页码
    private int page;
    //每页显示记录数
    private int pageSize;
}
