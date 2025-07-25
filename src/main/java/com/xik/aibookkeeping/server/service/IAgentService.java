package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.dto.AgentPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Agent;

/**
 * <p>
 * 智能体表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface IAgentService extends IService<Agent> {

    void addAgent(Agent agent);

    void deleteAgentById(Long id);

    Agent getAgent(Long id);

    Page<Agent> pageAgent(AgentPageQueryDTO agentPageQueryDTO);

    void updateAgent(Agent agent);
    
    Page<Agent> pageUserAgent(AgentPageQueryDTO agentPageQueryDTO);

    Agent getUserAgent(Long id);

    Long getDefaultAgent();
}
