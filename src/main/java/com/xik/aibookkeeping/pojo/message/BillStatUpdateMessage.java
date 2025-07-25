package com.xik.aibookkeeping.pojo.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillStatUpdateMessage implements Serializable {
    private Long billId;
    private Long userId;
    private String type; // revenue/expenditures
    private BigDecimal amount;
    private LocalDateTime billTime;
    private Integer statType; // 1-日 2-月 3-年
    private String statDate; // yyyy-MM-dd/yyyy-MM/yyyy
    private Boolean isDelete;
}