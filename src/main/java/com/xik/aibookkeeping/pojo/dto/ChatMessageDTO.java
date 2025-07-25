package com.xik.aibookkeeping.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 传递的消息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    private String chatId;

    /**
     * 用户输入的消息
     */
    private String message;

    /**
     *  使用智能体的id
     */
    private Long agentId;

    /**
     * 使用的提示词
     */
    private String prompt;

}
