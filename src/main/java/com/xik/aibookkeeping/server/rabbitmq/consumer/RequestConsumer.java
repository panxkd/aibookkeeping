package com.xik.aibookkeeping.server.rabbitmq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.MqConstant;
import com.xik.aibookkeeping.common.constant.RedisKeyConstant;
import com.xik.aibookkeeping.common.exception.LogException;
import com.xik.aibookkeeping.common.exception.MessageException;
import com.xik.aibookkeeping.pojo.entity.RequestLog;
import com.xik.aibookkeeping.pojo.message.PointsInitMessage;
import com.xik.aibookkeeping.pojo.message.RequestLogMessage;
import com.xik.aibookkeeping.server.mapper.RequestLogMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * 保存日志消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestConsumer {

    @Resource
    private RequestLogMapper requestLogMapper;

    @Resource
    private RedissonClient redissonClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstant.REQUEST_LOG_QUEUE),
            exchange = @Exchange(name = MqConstant.REQUEST_LOG_EXCHANGE,type = ExchangeTypes.DIRECT),
            key = MqConstant.REQUEST_LOG_ROUTING_KEY
    ))
    public void saveRequestLog(RequestLogMessage message) {
        log.info("日志数据：{}", message);
        String lockKey = RedisKeyConstant.LOG_KEY + message.getRequestId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                LambdaQueryWrapper<RequestLog> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(RequestLog::getRequestId, message.getRequestId());
                RequestLog requestLog = requestLogMapper.selectOne(queryWrapper);
                if (requestLog != null) {
                    throw new LogException(MessageConstant.LOG_CONSUMER);
                }
                RequestLog newRequestLog = new RequestLog();
                BeanUtils.copyProperties(message, newRequestLog);
                requestLogMapper.insert(newRequestLog);

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
}
