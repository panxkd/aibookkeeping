package com.xik.aibookkeeping.server.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.PointsLogConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.exception.UserAgentException;
import com.xik.aibookkeeping.pojo.entity.Agent;
import com.xik.aibookkeeping.pojo.entity.Points;
import com.xik.aibookkeeping.pojo.entity.UserAgent;
import com.xik.aibookkeeping.server.mapper.AgentMapper;
import com.xik.aibookkeeping.server.mapper.PointsMapper;
import com.xik.aibookkeeping.server.mapper.UserAgentMapper;
import com.xik.aibookkeeping.server.rabbitmq.producer.PointsProducer;
import com.xik.aibookkeeping.server.service.IUserAgentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 用户-智能体关联表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class UserAgentServiceImpl extends ServiceImpl<UserAgentMapper, UserAgent> implements IUserAgentService {

    @Resource
    private AgentMapper agentMapper;

    @Resource
    private PointsMapper pointsMapper;

    @Resource
    private PointsProducer pointsProducer;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserAgent(UserAgent userAgent) {
        try {
            // 查看是否为系统预设
            LambdaQueryWrapper<Agent> queryWrapperAgent = new LambdaQueryWrapper<>();
            queryWrapperAgent.eq(Agent::getId, userAgent.getAgentId()).eq(Agent::getIsDeleted,0).eq(Agent::getIsDefault,1);
            Long count = agentMapper.selectCount(queryWrapperAgent);
            if (count > 0) {
                // 如果是系统预设 直接保存
                this.save(userAgent);
                return;
            }
            // 不是系统预设
            // 查看用户现有积分和智能体所需要的积分
            int userPoints = pointsMapper.selectOne(
                    new LambdaQueryWrapper<Points>()
                            .eq(Points::getUserId,userAgent.getUserId())
            ).getTotalPoints();
            int agentPoints = agentMapper.selectOne(
                    new LambdaQueryWrapper<Agent>()
                            .eq(Agent::getId,userAgent.getAgentId())
            ).getNeedPoints();
            if (userPoints < agentPoints) {
                throw new UserAgentException("积分不足，无法解锁");
            }
            this.save(userAgent);
            // 解锁智能体 扣减用户积分 记录积分明细表
            //                          用户id                 变动积分         类型（获取/消费）
            pointsProducer.updatePoints(userAgent.getUserId(), agentPoints,PointsLogConstant.SPEND,
                               // 来源                                  备注
                    PointsLogConstant.POINTS_UPDATE_REMARK_AGENT, PointsLogConstant.POINTS_UPDATE_REMARK_AGENT);
        } catch (Exception e) {
            throw new UserAgentException("解锁智能体失败");
        }
    }

    @Override
    public void updateAgent(UserAgent userAgent) {
        try {
            LambdaUpdateWrapper<UserAgent> wrapper = new LambdaUpdateWrapper<>();
            wrapper
                    .eq(UserAgent::getId, userAgent.getId())
                    .set(UserAgent::getRemark, userAgent.getRemark())
                    .set(UserAgent::getAvatar, userAgent.getAvatar());
            this.update(wrapper);
        } catch (Exception e) {
            throw new UserAgentException(MessageConstant.USER_AGENT_UPDATE);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            LambdaUpdateWrapper<UserAgent> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(UserAgent::getId, id).eq(UserAgent::getIsDeleted,1);
            this.update(null,lambdaUpdateWrapper);
        } catch (Exception e) {
            throw new UserAgentException(MessageConstant.USER_AGENT_DELETE);
        }
    }

    /**
     * 获取用户所有的智能体
     * @return
     */
    @Override
    public List<UserAgent> getUserAgentByUserId() {
        try {
            Long userId = BaseContext.getCurrentId();
            LambdaQueryWrapper<UserAgent> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserAgent::getUserId, userId).eq(UserAgent::getIsDeleted,0);
            return this.list(lambdaQueryWrapper);
        } catch (Exception e) {
            throw new UserAgentException("查询失败");
        }
    }
}
