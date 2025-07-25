package com.xik.aibookkeeping.common.context;

public class AgentContextHolder {
    private static final ThreadLocal<Long> agentIdHolder = new ThreadLocal<>();

    public static void setAgentId(Long agentId) {
        agentIdHolder.set(agentId);
    }

    public static Long getAgentId() {
        return agentIdHolder.get();
    }

    public static void clear() {
        agentIdHolder.remove();
    }
}
