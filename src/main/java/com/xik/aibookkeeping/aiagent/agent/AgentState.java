package com.xik.aibookkeeping.aiagent.agent;


/**
 * 代理执行状态枚举类
 */
public enum AgentState {
    /**
     * 空闲状态
     */
    IDEL,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}
