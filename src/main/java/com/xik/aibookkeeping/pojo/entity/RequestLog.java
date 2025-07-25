package com.xik.aibookkeeping.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统日志表
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("request_log")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 日志类型
     */
    private String logType;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 操作动作
     */
    private String action;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求唯一标识
     */
    private String requestId;

    /**
     * 请求参数
     */
    private String parameters;

    /**
     * 操作状态
     */
    private String status;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误详情
     */
    private String errorMessage;

    /**
     * 执行耗时(ms)
     */
    private Integer executionTime;

    /**
     * 操作前数据
     */
    private String dataBefore;

    /**
     * 操作后数据
     */
    private String dataAfter;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 额外信息
     */
    private String extraInfo;

    /**
     * HTTP请求方法(GET/POST等)
     */
    private String requestMethod;

    /**
     * HTTP请求头(JSON格式)
     */
    private String requestHeaders;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求的token
     */
    private String token;

    /**
     * 逻辑删除（1删除/0未删除）
     */
    @TableField(select = false)
    private Integer isDeleted;


}
