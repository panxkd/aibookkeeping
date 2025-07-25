package com.xik.aibookkeeping.pojo.vo;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收支情况 金额
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RevenueAndExpendituresVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 收入
     */
    private BigDecimal  revenue;

    /**
     * 支出
     */
    private BigDecimal expenditures;
}
