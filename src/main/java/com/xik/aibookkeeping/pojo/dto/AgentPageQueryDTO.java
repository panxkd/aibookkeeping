package com.xik.aibookkeeping.pojo.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 智能体表
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
public class AgentPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int page;

    private int pageSize;

    /**
     * 主键，自增
     */
    private Long id;

    /**
     * 智能体名称（如：记账助手、理财助手）
     */
    private String name;

    /**
     * 智能体简介
     */
    private String description;

    /**
     * 使用的AI模型（如：gpt-4）
     */
    private String model;


    /**
     * 智能体头像
     */
    private String avatar;

    /**
     * 智能体提示词
     */
    private String prompt;

    /**
     * 解锁智能体所需积分
     */
    private Integer needPoints;

    /**
     * 是否为系统默认
     */
    private Integer idDefault;


}
