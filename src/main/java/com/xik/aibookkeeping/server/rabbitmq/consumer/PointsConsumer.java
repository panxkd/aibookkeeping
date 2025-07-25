package com.xik.aibookkeeping.server.rabbitmq.consumer;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.MqConstant;
import com.xik.aibookkeeping.common.constant.PointsLogConstant;
import com.xik.aibookkeeping.common.constant.RedisKeyConstant;
import com.xik.aibookkeeping.common.exception.PointsException;
import com.xik.aibookkeeping.pojo.entity.Points;
import com.xik.aibookkeeping.pojo.entity.PointsLog;
import com.xik.aibookkeeping.pojo.entity.User;
import com.xik.aibookkeeping.pojo.message.PointsInitMessage;
import com.xik.aibookkeeping.pojo.message.PointsUpdateMessage;
import com.xik.aibookkeeping.server.mapper.PointsLogMapper;
import com.xik.aibookkeeping.server.mapper.PointsMapper;
import com.xik.aibookkeeping.server.mapper.UserMapper;
import com.xik.aibookkeeping.server.service.IPointsService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


/**
 * 积分初始化消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointsConsumer {

    @Resource
    private PointsMapper pointsMapper;

    @Resource
    private PointsLogMapper pointsLogMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 初始化积分表
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstant.POINTS_STAT_QUEUE),
            exchange = @Exchange(name = MqConstant.POINTS_STAT_EXCHANGE,type = ExchangeTypes.DIRECT),
            key = MqConstant.POINTS_STAT_INIT_ROUTING_KEY
    ))
    public void initUserPoints(PointsInitMessage message) {
        log.info("init user points message:{}", message);
        Long userId = message.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new PointsException(MessageConstant.POINTS_INIT_ERR);
        }
        String lockKey = RedisKeyConstant.POINTS_STAT_INIT_KEY + message.getUserId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                Points points = new Points()
                        .setUserId(userId)
                        .setCreateTime(LocalDateTime.now())
                        .setUpdateTime(LocalDateTime.now());
                // 对于新用户 进行积分奖励
                points.setTotalPoints(PointsLogConstant.POINTS_INIT_LOGIN);
                pointsMapper.insert(points);
                // 记录积分日志
                PointsLog pointsLog = PointsLog.builder()
                        .userId(userId)
                        // 获取
                        .changeType(PointsLogConstant.EARN)
                        // 来源
                        .source(PointsLogConstant.POINTS_SOURCE)
                        // 记录本次的积分变化
                        .points(PointsLogConstant.POINTS_INIT_LOGIN)
                        // 总积分
                        .balance(points.getTotalPoints())
                        // 备注
                        .remark(PointsLogConstant.POINTS_INIT_REMARK)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                pointsLogMapper.insert(pointsLog);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取积分初始化锁中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 更新积分表
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstant.POINTS_UPDATE_QUEUE),
            exchange = @Exchange(name = MqConstant.POINTS_STAT_EXCHANGE,type = ExchangeTypes.DIRECT),
            key = MqConstant.POINTS_UPDATE_ROUTING_KEY
    ))
    public void updatePoints(PointsUpdateMessage message){
        log.info("update points message:{}", message);
        if (message.getUserId() == null || message.getPoints() == null || message.getChangeType() == null) {
            throw new PointsException(MessageConstant.POINTS_INIT_ERR);
        }
        String lockKey = String.format(RedisKeyConstant.POINTS_STAT_UPDATE_KEY, message.getUserId(), message.getChangeType(), message.getPoints());
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                // 查看是获取还是消费
                String sql = null;
                if (PointsLogConstant.EARN.equals(message.getChangeType())) {
                    // 获取积分
                    sql = "total_points = total_points +" + message.getPoints();
                } else if (PointsLogConstant.SPEND.equals(message.getChangeType())) {
                    sql = "total_points = total_points -" + message.getPoints();
                }
                // 更新积分表
                LambdaUpdateWrapper<Points> updateWrapperPoints = new LambdaUpdateWrapper<>();
                updateWrapperPoints
                        .eq(Points::getUserId, message.getUserId())
                        .set(Points::getUpdateTime, LocalDateTime.now())
                        .setSql(sql);
                pointsMapper.update(updateWrapperPoints);
                LambdaQueryWrapper<Points> queryWrapperPoints = new LambdaQueryWrapper<>();
                queryWrapperPoints.eq(Points::getUserId, message.getUserId());
                Points points = pointsMapper.selectOne(queryWrapperPoints);
                // 记录积分日志表
                PointsLog pointsLog = PointsLog.builder()
                        .userId(message.getUserId())
                        // 获取
                        .changeType(message.getChangeType())
                        // 来源
                        .source(message.getSource())
                        // 记录本次的积分变化
                        .points(message.getPoints())
                        // 总积分
                        .balance(points.getTotalPoints())
                        .remark(message.getRemark())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                pointsLogMapper.insert(pointsLog);
            }
        } catch (InterruptedException e) {
            log.error("获取积分更新化锁中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
