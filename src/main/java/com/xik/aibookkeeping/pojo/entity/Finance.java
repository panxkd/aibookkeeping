package com.xik.aibookkeeping.pojo.entity;

import java.io.Serial;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 * 理财计划表
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("finance")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Finance implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID，关联 user 表
     */
    private Long userId;

    /**
     * 理财计划名称（如"定投基金计划"）
     */
    private String title;

    /**
     * 理财目标/策略说明
     */
    private String description;

    /**
     * 目标金额（或每期投入金额）
     */
    private BigDecimal amount;

    /**
     * 计划开始时间
     */
    private LocalDateTime startTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态（1启用，2取消）
     */
    private Integer status;

    /**
     * 创建时间(默认现在)
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（(默认现在)可用于任务进度追踪）
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 逻辑删除（0正常，1删除）
     */
    @TableField(select = false)
    private Integer isDeleted;

    /**
     * 现有金额
     */
    private BigDecimal nowAmount;


}
