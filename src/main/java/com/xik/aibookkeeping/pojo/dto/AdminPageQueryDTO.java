package com.xik.aibookkeeping.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 传递的实体类 分页查询
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminPageQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

   private int page;

   private int pageSize;
}
