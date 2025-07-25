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
 * 交易规则表
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auto_category_rule")
public class AutoCategoryRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID(为空表示系统默认规则)
     */
    private Long userId;

    /**
     * 匹配关键词
     */
    private String keyword;

    /**
     * 关联的分类ID
     */
    private Long categoryId;

    /**
     * 交易类型(收入/支出/不记收支)
     */
    private String type;

    /**
     * 规则优先级(数值越大优先级越高)
     */
    private Integer priority;

    /**
     * 是否激活(1激活/0禁用)
     */
    private Integer isActive;

    /**
     * 规则创建时间
     */
    private LocalDateTime createTime;

    /**
     * 匹配模式(exact:精确/fuzzy:模糊/regex:正则)
     */
    private String matchMode;

    /**
     * 复杂匹配条件(JSON格式)
     */
    private String conditions;

    /**
     * 命中次数统计
     */
    private Integer hitCount;

    /**
     * 最后一次命中时间
     */
    private LocalDateTime lastHitTime;

    /**
     * 逻辑删除（1删除/0未删除）
     */
    @TableField(select = false)
    private Integer isDeleted;


}
