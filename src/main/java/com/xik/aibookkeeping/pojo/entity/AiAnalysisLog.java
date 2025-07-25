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
 * AI分析记录表
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ai_analysis_log")
public class AiAnalysisLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * spending_trend ： 支出趋势
income_trend ： 收入趋势
budget_suggestion ： 预算建议
category_optimization ： 类别优化
financial_health ： 财务健康
anomaly_detection ：异常检测
custom ： 自定义
     */
    private String analysisType;

    /**
     * 输入数据
     */
    private String inputData;

    /**
     * 分析结果
     */
    private String outputResult;

    /**
     * 使用的AI模型
     */
    private String modelUsed;

    /**
     * 消耗的tokens
     */
    private Integer costTokens;

    /**
     * 分析状态：pending 待处理，
processing 处理中，
completed 已完成，
failed 失败
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 逻辑删除（1删除/0未删除）
     */
    @TableField(select = false)
    private Integer isDeleted;


}
