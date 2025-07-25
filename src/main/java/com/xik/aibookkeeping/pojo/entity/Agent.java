package com.xik.aibookkeeping.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 智能体表
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("agent")
public class Agent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 状态（0：停用，1：启用）
     */
    private Integer status;

    /**
     * 逻辑删除（0未删除/1删除）
     */
    @TableField(select = false)
    private Integer isDeleted;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;

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
    private Integer isDefault;

}
