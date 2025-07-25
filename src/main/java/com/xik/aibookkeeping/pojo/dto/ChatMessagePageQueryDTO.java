package com.xik.aibookkeeping.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 传递的消息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessagePageQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int page;

    private int pageSize;


    private Long id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户输入的消息
     */
    private String message;

    /**
     *  使用智能体的id
     */
    private Long agentId;

}
