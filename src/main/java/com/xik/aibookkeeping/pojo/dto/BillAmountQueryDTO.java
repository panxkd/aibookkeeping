package com.xik.aibookkeeping.pojo.dto;


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
 * 传递的实体类 收支金额
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillAmountQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    /**
     * 类型（revenue收入/expenditures支出/notRecorded不记入收支）
     */
    private String type;

    /**
     * 分类id（餐饮、交通等）
     */
    private Long categoryId;


    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
