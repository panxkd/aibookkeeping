package com.xik.aibookkeeping.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 搜索历史返回的数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchHistoryVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 搜索历史返回的数据
     */
    private String content;
}
