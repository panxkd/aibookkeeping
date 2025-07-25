package com.xik.aibookkeeping.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 传递的实体类 金额构成
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillAmountVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 支出金额 expenditures
     */
    private BigDecimal expendituresAmount;

    /**
     * 收入金额
     */
    private BigDecimal revenueAmount;

    /**
     * 不计入收支
     */
    private BigDecimal taxAmount;

}
