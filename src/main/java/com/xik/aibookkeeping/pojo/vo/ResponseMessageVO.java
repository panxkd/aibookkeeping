package com.xik.aibookkeeping.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ai的回复内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String responseMessage;

    private LocalDateTime responseTime;

}
