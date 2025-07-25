package com.xik.aibookkeeping.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xik.aibookkeeping.pojo.entity.Bill;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 关联的会话ID
     */
    private String sessionId;


    /**
     * 角色类型（USER/ASSISTANT/SYSTEM）
     */
    private String roleType;

    /**
     * 消息内容
     */
    private String content;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 消息类型 是账单还是正常回复
     */
    private String messageType;

    /**
     * 账单
     */
    private List<Bill>  billList;


}
