package com.xik.aibookkeeping.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 积分表
 * </p>
 *
 * @author panxikai
 * @since 2025-06-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_log")
@Builder
public class PointsLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类型（如 earn=获得, spend=消费）
     */
    private String changeType;

    /**
     * 来源（如 记账、签到、兑换商品）
     */
    private String source;

    /**
     * 本次变动的积分值（正/负）
     */
    private Integer points;

    /**
     * 当前积分余额（用于冗余加快读取）
     */
    private Integer balance;

    /**
     * 备注信息（如"首次记账奖励"）
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


}
