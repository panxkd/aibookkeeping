package com.xik.aibookkeeping.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillRecord {

    private String uuid;
    private BigDecimal amount;
    private String type;        // 收入 / 支出 / 不计
    private String category;        // 分类：餐饮、交通...
    private String remark;

}
