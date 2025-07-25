package com.xik.aibookkeeping.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 传递的实体类 分类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 类型（revenue收入/expenditures支出/notRecorded不记入收支）
     */
    private String type;

    /**
     * 分类id（餐饮、交通等）
     */
    private Long categoryId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 备注信息
     */
    private String remark;

}
