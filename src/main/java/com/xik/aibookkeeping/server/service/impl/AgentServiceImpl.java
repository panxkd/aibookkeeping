package com.xik.aibookkeeping.server.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.exception.AgentException;
import com.xik.aibookkeeping.common.exception.UserException;
import com.xik.aibookkeeping.pojo.dto.AgentPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Agent;
import com.xik.aibookkeeping.pojo.entity.UserAgent;
import com.xik.aibookkeeping.server.mapper.AgentMapper;
import com.xik.aibookkeeping.server.mapper.UserAgentMapper;
import com.xik.aibookkeeping.server.service.IAgentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 智能体表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements IAgentService {

    @Resource
    private UserAgentMapper userAgentMapper;

    /**
     * 添加智能体
     * @param agent
     */
    @Override
    @OperationLog(OperationType.ADMIN_INSERT)
    public void addAgent(Agent agent) {
        try {
            LambdaQueryWrapper<Agent>  queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Agent::getIsDefault,1);
            Long count = baseMapper.selectCount(queryWrapper);
            if (count > 0 && agent.getIsDefault() == 1) {
                throw new AgentException("已有系统默认智能体");
            }
            this.save(agent);
        } catch (Exception e) {
            throw new AgentException(MessageConstant.AGENT_INSERT_ERR + e.getMessage());
        }
    }

    @Override
    public void deleteAgentById(Long id) {
        try {
            Agent agent = this.getById(id);
            if (agent.getStatus() == 1) {
                throw new AgentException(MessageConstant.AGENT_NOT_DELETE);
            }
            LambdaUpdateWrapper<Agent> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(Agent::getId, id).set(Agent::getIsDeleted,1);
            this.update(null, lambdaUpdateWrapper);
        } catch (Exception e) {
            throw new AgentException(MessageConstant.AGENT_DELETE_ERR);
        }
    }

    @Override
    public Agent getAgent(Long id) {
        try {
            return this.getById(id);
        } catch (Exception e) {
            throw new AgentException(MessageConstant.AGENT_QUERY_ERR);
        }
    }

    /**
     * 管理员操作
     * @param agentPageQueryDTO
     * @return
     */
    @Override
    public Page<Agent> pageAgent(AgentPageQueryDTO agentPageQueryDTO) {
        try {
            Page<Agent> page = new Page<>(agentPageQueryDTO.getPage(), agentPageQueryDTO.getPageSize());

            LambdaQueryWrapper<Agent> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .eq(Agent::getIsDeleted,0)
                    .like(agentPageQueryDTO.getName() != null, Agent::getName, agentPageQueryDTO.getName())
                    .like(agentPageQueryDTO.getModel() != null,Agent::getModel,agentPageQueryDTO.getModel())
                    .like(agentPageQueryDTO.getPrompt() != null,Agent::getPrompt, agentPageQueryDTO.getPrompt())
                    .like(agentPageQueryDTO.getDescription() != null,Agent::getDescription,agentPageQueryDTO.getDescription())
                    .orderByDesc(Agent::getCreateTime);
            return this.page(page, lambdaQueryWrapper);
        } catch (Exception e) {
            throw new AgentException(MessageConstant.AGENT_QUERY_ERR);
        }
    }

    @Override
    @OperationLog(OperationType.ADMIN_UPDATE)
    public void updateAgent(Agent agent) {
        try {
            // 判断当前智能体是否是默认智能体
            Agent oldAgent = this.getById(agent.getId());
            // 是默认智能体 可以随意修改
            if (oldAgent.getIsDefault() != 1) {
                LambdaQueryWrapper<Agent>  queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Agent::getIsDefault,1);
                Long count = baseMapper.selectCount(queryWrapper);
                if (count > 0 && agent.getIsDefault() == 1) {
                    throw new AgentException("已有系统默认智能体");
                }
            }

            this.updateById(agent);
        } catch (Exception e) {
            throw new AgentException(MessageConstant.AGENT_UPDATE_ERR +  e.getMessage());
        }
    }

    /**
     * 用户查询智能体
     * @param agentPageQueryDTO
     * @return
     */
    @Override
    public Page<Agent> pageUserAgent(AgentPageQueryDTO agentPageQueryDTO) {
        try {
            // 1. 分页查询 Agent
            Page<Agent> page = new Page<>(agentPageQueryDTO.getPage(), agentPageQueryDTO.getPageSize());
            LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Agent::getIsDeleted, 0)
                    .eq(Agent::getStatus, 1)
                    .like(agentPageQueryDTO.getModel() != null,Agent::getModel,agentPageQueryDTO.getModel())
                    .like(agentPageQueryDTO.getPrompt() != null,Agent::getPrompt, agentPageQueryDTO.getPrompt())
                    .like(agentPageQueryDTO.getDescription() != null,Agent::getDescription,agentPageQueryDTO.getDescription())
                    .like(agentPageQueryDTO.getName() != null, Agent::getName, agentPageQueryDTO.getName())
                    .orderByDesc(Agent::getCreateTime);

            return this.page(page, wrapper);
        } catch (Exception e) {
            throw new AgentException(MessageConstant.AGENT_QUERY_ERR);
        }
    }

    /**
     * 用户查询智能体
     * @param id
     * @return
     */
    @Override
    public Agent getUserAgent(Long id) {
        try {
            return this.getById(id);
        } catch (Exception e) {
            throw new AgentException(MessageConstant.AGENT_QUERY_ERR);
        }
    }

    /**
     * 获取默认智能体id
     * @return
     */
    @Override
    public Long getDefaultAgent() {
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Agent::getIsDefault,1).eq(Agent::getIsDeleted, 0);
        Agent agent = baseMapper.selectOne(queryWrapper);
        if (agent != null) {
            return agent.getId();
        } else {
            return null;
        }
    }
}
