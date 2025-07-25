package com.xik.aibookkeeping.server.service;

public interface IUserSelectorService {
    Long getAgentId(Long userId);

    void updateAgentId(Long userId, Long agentId);
}
