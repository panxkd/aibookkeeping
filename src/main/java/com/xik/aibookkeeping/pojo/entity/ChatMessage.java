package com.xik.aibookkeeping.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AI消息记录表
 * </p>
 *
 * @author panxikai
 * @since 2025-07-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的会话ID
     */
    private String sessionId;

    /**
     * AI会话主表ID
     */
    private String chatSessionId;

    /**
     * 消息顺序（从0递增）
     */
    private Integer messageOrder;

    /**
     * 角色类型（USER/ASSISTANT/SYSTEM）
     */
    private String roleType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息消耗的token数量
     */
    private Integer tokens;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private Integer isDeleted;

    /**
     * 元数据
     */
    private String meta;

    private String messageType;



}
