package com.xik.aibookkeeping.pojo.vo;


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
 * 传递的实体类 收支构成
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillStructureVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 收入/支出/不计入收支
     */
    private String type;

    /**
     * 分类
     */
    private String category;

    /**
     * 收入金额
     */
    private BigDecimal revenueAmount;

    /**
     * 支出金额
     */
    private BigDecimal expendituresAmount;

    /**
     * 不计入收支金额
     */
    private BigDecimal taxAmount;

}
