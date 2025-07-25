package com.xik.aibookkeeping.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类分页查询传递的数据
 */
@Data
public class CategoryPageQueryDTO implements Serializable {

    private Long id;
    /**
     * 分类
     */
    private String category;

    /**
     * 描述
     */
    private String description;

    /**
     * 类型（revenue收入/expenditures支出/notRecorded不记入收支）
     */
    private String type;
    //页码
    private int page;
    //每页显示记录数
    private int pageSize;
}
