package com.xik.aibookkeeping.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 理财计划 传递实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancePageQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int page;

    private int pageSize;


    private Long id;

    /**
     * 所属用户ID，关联 user 表
     */
    private Long userId;

    /**
     * 理财计划名称（如"定投基金计划"）
     */
    private String title;

    /**
     * 理财目标/策略说明
     */
    private String description;

    /**
     * 目标金额（或每期投入金额）
     */
    private BigDecimal amount;

    /**
     *  查询时间状态（0未开始 1进行中 2已完成 ）
     */
    private Integer statusTime;

    /**
     * 状态（0取消，1启用）
     */
    private Integer status;


}
