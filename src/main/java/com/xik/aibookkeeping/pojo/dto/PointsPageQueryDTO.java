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
 * 积分查询实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointsPageQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int page;

    private int pageSize;

    private Long id;

    private String nickname;




}
