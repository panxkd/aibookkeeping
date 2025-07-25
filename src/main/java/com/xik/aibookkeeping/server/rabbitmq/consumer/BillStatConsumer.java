package com.xik.aibookkeeping.server.rabbitmq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xik.aibookkeeping.common.constant.BillConstant;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.MqConstant;
import com.xik.aibookkeeping.common.constant.RedisKeyConstant;
import com.xik.aibookkeeping.common.exception.MessageException;
import com.xik.aibookkeeping.common.utils.HasNull;
import com.xik.aibookkeeping.pojo.message.BillStatInitMessage;
import com.xik.aibookkeeping.pojo.message.BillStatUpdateMessage;
import com.xik.aibookkeeping.pojo.entity.BillStatistics;
import com.xik.aibookkeeping.server.config.RabbitMQConfig;
import com.xik.aibookkeeping.server.mapper.BillMapper;
import com.xik.aibookkeeping.server.mapper.BillStatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillStatConsumer {

    private final BillStatisticsMapper statisticsMapper;
    private final RedissonClient redissonClient;
    private final BillMapper billMapper;

    /**
     * 更新账单消息
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = MqConstant.BILL_STAT_UPDATE_QUEUE,
                    durable = "true",
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange", value = MqConstant.BILL_STAT_EXCHANGE_DLX)
                    }
            ),
            exchange = @Exchange(
                    name = MqConstant.BILL_STAT_EXCHANGE,
                    type = ExchangeTypes.TOPIC,
                    durable = "true"
            ),
            key = MqConstant.BILL_STAT_UPDATE_ROUTING_KEY
    ))
    @Transactional(rollbackFor = Exception.class)
    public void processStatUpdate(BillStatUpdateMessage message) {
        log.info("监听的消息：{}, 进行账单更新", message);
        if (HasNull.hasNullField(message)) {
            throw new MessageException(MessageConstant.MESSAGE_NUT_NULL);
        }
        String lockKey = String.format(RedisKeyConstant.BILL_STAT_UPDATE_KEY,
                message.getUserId(), message.getStatType(), message.getStatDate());

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                BillStatistics statistics = new BillStatistics();
                statistics.setUserId(message.getUserId());
                statistics.setStatType(message.getStatType());
                statistics.setStatDate(message.getStatDate());

                if (BillConstant.REVENUE.equals(message.getType())) {
                    statistics.setRevenue(message.getAmount());
                    statistics.setExpenditures(BigDecimal.ZERO);
                } else if (BillConstant.EXPENSE.equals(message.getType())) {
                    statistics.setRevenue(BigDecimal.ZERO);
                    statistics.setExpenditures(message.getAmount());
                }
                if (!message.getIsDelete()) {
                    // 更新账单
                    statisticsMapper.upsertStatistics(statistics);
                } else {
                    // 删除账单
                    statisticsMapper.deleteStatistics(statistics);
                }

                log.debug("账单统计更新成功: {}", message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取统计更新锁中断", e);
        } catch (Exception e) {
            log.error("账单统计更新失败: {}", message, e);
            throw new RuntimeException("统计更新失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 进行账单初始化
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = MqConstant.BILL_STAT_INIT_QUEUE,
                    durable = "true",
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange", value = MqConstant.BILL_STAT_EXCHANGE_DLX)
                    }
            ),
            exchange = @Exchange(
                    name = MqConstant.BILL_STAT_EXCHANGE,
                    type = ExchangeTypes.TOPIC,
                    durable = "true"
            ),
            key = MqConstant.BILL_STAT_INIT_ROUTING_KEY
    ))
    @Transactional(rollbackFor = Exception.class)
    public void processStatInit(BillStatInitMessage message) {
        log.info("监听的消息：{}, 进行账单初始化", message);
        if (HasNull.hasNullField(message)) {
            throw new MessageException(MessageConstant.MESSAGE_NUT_NULL);
        }
        try {
            // 日统计初始化
            checkAndInitialize(message.getUserId(), 1,
                    message.getRecordTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            // 月统计初始化
            checkAndInitialize(message.getUserId(), 2,
                    message.getRecordTime().format(DateTimeFormatter.ofPattern("yyyy-MM")));

            // 年统计初始化
            checkAndInitialize(message.getUserId(), 3,
                    message.getRecordTime().format(DateTimeFormatter.ofPattern("yyyy")));
        } catch (Exception e) {
            log.error("账单统计初始化失败", e);
            throw new RuntimeException("统计初始化失败", e);
        }
    }

    private void checkAndInitialize(Long userId, int statType, String statDate) {
        String lockKey = String.format(RedisKeyConstant.BILL_STAT_INIT_KEY, userId, statType, statDate);

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                Long count = statisticsMapper.selectCount(new QueryWrapper<BillStatistics>()
                        .eq("user_id", userId)
                        .eq("stat_type", statType)
                        .eq("stat_date", statDate));

                if (count == 0) {
                    BillStatistics stats = new BillStatistics();
                    stats.setUserId(userId);
                    stats.setStatType(statType);
                    stats.setStatDate(statDate);
                    stats.setRevenue(BigDecimal.ZERO);
                    stats.setExpenditures(BigDecimal.ZERO);
                    stats.setUpdateTime(LocalDateTime.now());
                    statisticsMapper.insert(stats);
                    log.debug("初始化账单统计记录: userId={}, type={}, date={}",
                            userId, statType, statDate);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取统计初始化锁中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 处理死信队列中的消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = "bill.stat.update.queue.dlx",
                    durable = "true"
            ),
            exchange = @Exchange(
                    name = "bill.stat.exchange.dlx",
                    type = ExchangeTypes.TOPIC,
                    durable = "true"
            ),
            key = "#"
    ))
    public void processDeadLetter(Message deadMsg) {
        log.error("死信消息捕获: {}", new String(deadMsg.getBody()));
    }
}