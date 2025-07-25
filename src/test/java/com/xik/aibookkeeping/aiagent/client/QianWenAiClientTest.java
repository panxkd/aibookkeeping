package com.xik.aibookkeeping.aiagent.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xik.aibookkeeping.pojo.dto.ChatMessageDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QianWenAiClientTest {

    @Resource
    private QianWenAiClient qianWenAiClient;

    @Test
    void test() throws JsonProcessingException {
        String message = "早餐5块，午饭10元，晚饭20元，打车50块";
        String chatId = "200";
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setMessage(message);
        chatMessageDTO.setChatId(chatId);
        String reulst = String.valueOf(qianWenAiClient.doChatWithBill(chatMessageDTO));
//        String reulst = String.valueOf(qianWenAiClient.doChat(message, chatId));

        // 第二轮
//        message = "我刚刚吃了啥";
//        reulst = String.valueOf(qianWenAiClient.doChat(message, chatId));
    }

    @Test
    void test1() {
        String message = "早餐5块，午饭10元，晚饭20元，打车50块";
        String chatId = "200";
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setAgentId(1L);
        chatMessageDTO.setMessage(message);
        chatMessageDTO.setChatId(chatId);
//        String stringFlux = qianWenAiClient.doChat(chatMessageDTO);

    }

    @Test
    void test2() {
//        String message = "你是谁啊。我今天消费早餐5块，午饭10元，晚饭20元，打车50块";
        String message = "你叫什么名字，我今天吃了肯德基";
        String chatId = "300";
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setAgentId(1L);
        chatMessageDTO.setMessage(message);
        chatMessageDTO.setChatId(chatId);
        String result = qianWenAiClient.doChatWithTool(chatMessageDTO);
        System.out.println(result);
    }

}