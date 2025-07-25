package com.xik.aibookkeeping.common.constant;


/**
 * mq 相关配置名
 */
public class MqConstant {


    // 统计更新队列
    public static final String BILL_STAT_UPDATE_QUEUE = "bill.stat.update.queue";
    // 积分初始化队列
    public static final String POINTS_STAT_QUEUE = "points.stat.init.queue";
    // 积分更新队列
    public static final String POINTS_UPDATE_QUEUE = "points.update.queue";
    // 统计初始化队列
    public static final String BILL_STAT_INIT_QUEUE = "bill.stat.init.queue";
    // 请求日志队列
    public static final String REQUEST_LOG_QUEUE = "request.queue";
    public static final String REQUEST_LOG_EXCHANGE = "request.exchange";
    // 交换机
    public static final String BILL_STAT_EXCHANGE = "bill.stat.exchange";
    // 积分交换机
    public static final String POINTS_STAT_EXCHANGE = "points.stat.init.exchange";
    // 路由键
    public static final String BILL_STAT_UPDATE_ROUTING_KEY = "bill.stat.update";
    public static final String BILL_STAT_INIT_ROUTING_KEY = "bill.stat.init";
    public static final String POINTS_STAT_INIT_ROUTING_KEY = "points.stat.init";
    public static final String POINTS_UPDATE_ROUTING_KEY = "points.stat.update";
    public static final String REQUEST_LOG_ROUTING_KEY = "request.log";

    // 死信队列配置
    public static final String BILL_STAT_EXCHANGE_DLX  = "bill.stat.exchange.dlx";





}
