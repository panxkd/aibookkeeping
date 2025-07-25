package com.xik.aibookkeeping.pojo.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class FinanceDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
     * 计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;


}
