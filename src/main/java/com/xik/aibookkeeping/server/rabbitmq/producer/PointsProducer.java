package com.xik.aibookkeeping.server.rabbitmq.producer;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.MqConstant;
import com.xik.aibookkeeping.common.constant.PointsLogConstant;
import com.xik.aibookkeeping.common.exception.PointsException;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.message.PointsInitMessage;
import com.xik.aibookkeeping.pojo.message.PointsUpdateMessage;
import com.xik.aibookkeeping.server.mapper.BillMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


/**
 * 积分初始化生产者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointsProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private BillMapper billMapper;

    /**
     * 初始化用户积分表信息
     * @param id
     */
    public void initUser(Long id) {
        if (id == null) {
            throw new PointsException(MessageConstant.POINTS_INIT_ERR);
        }
        PointsInitMessage  message = new  PointsInitMessage();
        message.setUserId(id);
        try {
            rabbitTemplate.convertAndSend(
                    MqConstant.POINTS_STAT_EXCHANGE,
                    MqConstant.POINTS_STAT_INIT_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    }
            );
        } catch (Exception e) {
            throw new PointsException(MessageConstant.POINTS_INIT_ERR + e.getMessage());
        }
    }



    public void updatePoints(Long userId, Integer points, String changeType, String source, String remark) {
        if (userId == null || points == null || changeType == null || source == null || remark == null) {
            throw new PointsException(MessageConstant.POINTS_INIT_ERR);
        }
        // 如果是当天首次使用 签到获得的 需要进行查询当天是否记账
        if (PointsLogConstant.POINTS_SIGNIN_SOURCE.equals(source)) {
            // 查询当天是否签到
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
            LambdaQueryWrapper<Bill> queryWrapper = new LambdaQueryWrapper<Bill>();
            queryWrapper.eq(Bill::getUserId, userId).eq(Bill::getUserId, userId).between(Bill::getCreateTime, startOfDay, endOfDay);
            Long count = billMapper.selectCount(queryWrapper);
            if (count > 0) {
                return;
            }
        }
        PointsUpdateMessage  message = PointsUpdateMessage.builder()
                .userId(userId)
                .points(points)
                .changeType(changeType)
                .source(source)
                .remark(remark)
                .build();
        try {
            rabbitTemplate.convertAndSend(
                    MqConstant.POINTS_STAT_EXCHANGE,
                    MqConstant.POINTS_UPDATE_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    }
            );
        } catch (Exception e) {
            throw new PointsException(MessageConstant.POINTS_UPDATE_ERR + e.getMessage());
        }

    }
}
