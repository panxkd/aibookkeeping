package com.xik.aibookkeeping.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xik.aibookkeeping.common.constant.RedisKeyConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.exception.UserSelectorAgentException;
import com.xik.aibookkeeping.pojo.entity.Agent;
import com.xik.aibookkeeping.server.mapper.AgentMapper;
import com.xik.aibookkeeping.server.service.IUserSelectorService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserSelectorServiceImpl implements IUserSelectorService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AgentMapper agentMapper;

    @Override
    public Long getAgentId(Long userId) {
        try {
            String key = RedisKeyConstant.USER_AGENT_KEY + userId;
            String val = stringRedisTemplate.opsForValue().get(key);

            if (StringUtils.hasText(val)) {
                return Long.valueOf(val);
            }
            // Redis 没有值，查询默认智能体
            LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Agent::getIsDefault, 1);
            Agent agent = agentMapper.selectOne(queryWrapper);
            // 可以考虑加非空校验
            if (agent == null) {
                throw new UserSelectorAgentException("未配置默认智能体！");
            }
            // 存入redis
            stringRedisTemplate.opsForValue().set(key, String.valueOf(agent.getId()));
            return agent.getId();
        } catch (Exception e) {
            throw new UserSelectorAgentException("获取id失败");
        }
    }

    @Override
    public void updateAgentId(Long userId, Long agentId) {
        try {
            String key = RedisKeyConstant.USER_AGENT_KEY + userId;
            stringRedisTemplate.opsForValue().set(key, String.valueOf(agentId));
        } catch (Exception e) {
            throw new UserSelectorAgentException("选择失败");
        }
    }
}
