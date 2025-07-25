package com.xik.aibookkeeping.server.rabbitmq.producer;

import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.MqConstant;
import com.xik.aibookkeeping.common.exception.MessageException;
import com.xik.aibookkeeping.common.utils.HasNull;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.message.BillStatInitMessage;
import com.xik.aibookkeeping.pojo.message.BillStatUpdateMessage;
import com.xik.aibookkeeping.server.config.RabbitMQConfig;
import com.xik.aibookkeeping.server.mapper.BillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillStatProducer {

    private final RabbitTemplate rabbitTemplate;
    private final BillMapper billMapper;

    public void sendStatUpdateMessage(Long billId, boolean isDelete) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null || !"revenue".equals(bill.getType()) && !"expenditures".equals(bill.getType())) {
            return;
        }

        // 发送日统计更新
        sendSingleStatUpdate(bill, 1,
                bill.getBillTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),isDelete);

        // 发送月统计更新
        sendSingleStatUpdate(bill, 2,
                bill.getBillTime().format(DateTimeFormatter.ofPattern("yyyy-MM")),isDelete);

        // 发送年统计更新
        sendSingleStatUpdate(bill, 3,
                bill.getBillTime().format(DateTimeFormatter.ofPattern("yyyy")),isDelete);
    }

    private void sendSingleStatUpdate(Bill bill, int statType, String statDate, Boolean isDelete) {
        BillStatUpdateMessage message = new BillStatUpdateMessage(
                bill.getId(),
                bill.getUserId(),
                bill.getType(),
                bill.getAmount(),
                bill.getBillTime(),
                statType,
                statDate,
                isDelete
        );
        if (HasNull.hasNullField(message)) {
            throw new MessageException(MessageConstant.MESSAGE_NUT_NULL);
        }
        try {
            rabbitTemplate.convertAndSend(
                    MqConstant.BILL_STAT_EXCHANGE,
                    MqConstant.BILL_STAT_UPDATE_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    }
            );
        } catch (Exception e) {
            log.error("发送统计更新消息失败: {}", message, e);
            // 可以加入重试逻辑或错误处理
        }
    }

    public void sendStatInitMessage(Long userId, LocalDateTime recordTime) {
        BillStatInitMessage message = new BillStatInitMessage(userId, recordTime);
        if (HasNull.hasNullField(message)) {
            throw new MessageException(MessageConstant.MESSAGE_NUT_NULL);
        }
        try {
            rabbitTemplate.convertAndSend(
                    MqConstant.BILL_STAT_EXCHANGE,
                    MqConstant.BILL_STAT_INIT_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    }
            );
        } catch (Exception e) {
            log.error("发送统计初始化消息失败: {}", message, e);
        }
    }
}