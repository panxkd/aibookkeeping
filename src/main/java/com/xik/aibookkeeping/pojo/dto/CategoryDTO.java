package com.xik.aibookkeeping.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 传递的实体类 分类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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

    /**
     * 图标
     */
    private String icon;
}
