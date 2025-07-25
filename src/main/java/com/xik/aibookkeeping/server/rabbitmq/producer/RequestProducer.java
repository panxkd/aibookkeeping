package com.xik.aibookkeeping.server.rabbitmq.producer;


import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.MqConstant;
import com.xik.aibookkeeping.common.exception.PointsException;
import com.xik.aibookkeeping.pojo.entity.RequestLog;
import com.xik.aibookkeeping.pojo.message.RequestLogMessage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


/**
 * 保存日志生产者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void saveLog(RequestLogMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    MqConstant.REQUEST_LOG_EXCHANGE,
                    MqConstant.REQUEST_LOG_ROUTING_KEY,
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
}
