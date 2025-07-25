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
 * 用户-智能体关联表
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_agent")
public class UserAgent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 所选智能体ID
     */
    private Long agentId;

    /**
     * 用户自定义备注
     */
    private String remark;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 逻辑删除（0未删除/1删除）
     */
    @TableField(select = false)
    private Integer isDeleted;

    /**
     * 用户自定义头像
     */
    private String avatar;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


}
