package com.xik.aibookkeeping.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 搜索历史 传递的参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchHistoryDTO implements Serializable {

    /**
     * 搜索的内容
     */
    private String content;
}
