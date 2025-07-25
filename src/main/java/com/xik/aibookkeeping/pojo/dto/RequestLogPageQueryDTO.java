package com.xik.aibookkeeping.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 请求日志
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestLogPageQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int page;

    private int pageSize;

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
     * 执行耗时(ms)
     */
    private Integer executionTime;

    /**
     * 操作后数据
     */
    private String dataAfter;


    /**
     * HTTP请求方法(GET/POST等)
     */
    private String requestMethod;


    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求的token
     */
    private String token;




}
