package com.xik.aibookkeeping.aiagent.chatmemory;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.AgentContextHolder;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.pojo.entity.AiBillRecord;
import com.xik.aibookkeeping.pojo.entity.BillRecord;
import com.xik.aibookkeeping.pojo.entity.ChatMessage;
import com.xik.aibookkeeping.pojo.entity.ChatSession;
import com.xik.aibookkeeping.server.mapper.ChatMessageMapper;
import com.xik.aibookkeeping.server.mapper.ChatSessionMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataChatMemory implements ChatMemory {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;


    @Override
    public void add(String conversationId, Message message) {
        // 获取当前智能体
        Long agentId = AgentContextHolder.getAgentId();
        Long userId = BaseContext.getCurrentId();
        // 查询当前会话
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(ChatSession::getSessionId,conversationId)
                .eq(agentId != null, ChatSession::getAgentId,agentId)
                .last("limit 1")
                .eq(ChatSession::getIsDeleted,0);
        ChatSession chatSession = chatSessionMapper.selectOne(queryWrapper);
        String sessionId = null;
        if (chatSession == null) {
            sessionId = UUID.randomUUID().toString();
            // 创建新会话
            ChatSession newSession = new ChatSession();
            newSession.setSessionId(conversationId);
            // 自动生成标题
            String firstMatch = findFirstMatch(message.getText());
            newSession.setTitle(firstMatch == null ? message.getText() : message.getText().split(firstMatch)[0]);

//            Long userId = 6L;
            if (userId != null) {
                newSession.setUserId(userId);
            }
            newSession.setId(sessionId);
            if (agentId != null) {
                newSession.setAgentId(agentId);
            }
            chatSessionMapper.insert(newSession);
        }  else {
            sessionId = chatSession.getId();
        }
        //  为新的消息创建存储对象
        ChatMessage chatMessage = new ChatMessage();
        String str = message.getText();
        log.info("发送的消息：{}", str);
        if (str != null) {
            chatMessage.setMessageType("text");
            if (isJson(str)) {
                chatMessage.setMessageType("json");
            }
        }
        chatMessage.setSessionId(conversationId);
        chatMessage.setRoleType(message.getMessageType().getValue());
        chatMessage.setContent(message.getText());
        chatMessage.setMeta(message.getMetadata().toString());
        chatMessage.setChatSessionId(sessionId);
        // 确定消息排序序号 获取当前最大的序号+1
        ChatMessage lastMessage = chatMessageMapper.selectOne(
                new LambdaQueryWrapper<>(chatMessage)
                        .select(ChatMessage::getMessageOrder)
                        .eq(ChatMessage::getChatSessionId, conversationId)
                        .eq(ChatMessage::getIsDeleted,0)
                        .orderByDesc(ChatMessage::getMessageOrder)
                        .last("limit 1")
        );
        chatMessage.setMessageOrder(lastMessage != null ? lastMessage.getMessageOrder() + 1 : 0);
        // 保存消息明细
        log.info("存储发送的消息：{}", chatMessage);
        chatMessageMapper.insert(chatMessage);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        Long agentId = AgentContextHolder.getAgentId();
        // 回答的消息加入消息列表
        // 检查消息列表是否为空
        if (messages == null || messages.isEmpty()) {
            return;
        }
        // 取最后一条消息
        Message lastMessage = messages.getLast();
        // 获取父消息id
        ChatSession chatSession = chatSessionMapper.selectOne(
                new LambdaQueryWrapper<>(new ChatSession())
                        .eq(ChatSession::getSessionId, conversationId)
                        .eq(agentId != null,ChatSession::getAgentId, agentId)
                        .last("limit 1")
                        .eq(ChatSession::getIsDeleted,0)
        );
        ChatMessage chatMessage = new ChatMessage();
        String str = lastMessage.getText();
        log.info("回答的消息：{}", str);
        if (StringUtils.isNotBlank(str) && !"null".equals(str.trim())) {
            chatMessage.setMessageType("text");
            if (isJson(str)) {
                chatMessage.setMessageType("json");
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    // 为生成的订单设置一个uuid 先将json转为实体类 设置uuid后再转为json
                    AiBillRecord result = objectMapper.readValue(str, AiBillRecord.class);
                    List<BillRecord> billRecordList = result.getBillRecordList();
                    for (BillRecord billRecord : billRecordList) {
                        String uuid = UUID.randomUUID().toString();
                        billRecord.setUuid(uuid);
                    }
                    str = objectMapper.writeValueAsString(result);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (!StringUtils.isNotBlank(str) && "null".equals(str.trim())) {
            chatMessage.setMessageType("null");
        }
        chatMessage.setSessionId(conversationId);
        chatMessage.setRoleType(messages.get(0).getMessageType().getValue());
        chatMessage.setContent(str);
        chatMessage.setMeta(lastMessage.getMetadata().toString());
        chatMessage.setChatSessionId(chatSession.getId());
        // 查询最高的排序
        // 确定消息的排序号 获取当前最大序号+1
        ChatMessage getSort = chatMessageMapper.selectOne(
                new LambdaQueryWrapper<>(chatMessage)
                        .select(ChatMessage::getMessageOrder)
                        .eq(ChatMessage::getIsDeleted,0)
                        .eq(ChatMessage::getSessionId, conversationId)
                        .orderByDesc(ChatMessage::getMessageOrder)
                        .last("LIMIT 1")
        );
        chatMessage.setMessageOrder(getSort != null ? getSort.getMessageOrder() + 1 : 0);
        log.info("存储回答的消息：{}", chatMessage);
        chatMessageMapper.insert(chatMessage);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        if (conversationId == null || conversationId.isEmpty() || lastN <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(ChatMessage::getSessionId, conversationId)
                .eq(ChatMessage::getIsDeleted,0)
                .orderByDesc(ChatMessage::getMessageOrder);
        // 查询符合条件的
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(queryWrapper);
        // 取最后N条
        if (chatMessages.size() >  lastN) {
            chatMessages =  chatMessages.subList(chatMessages.size() - lastN, chatMessages.size());
        }
        // 转为Message实体对象
        return chatMessages.stream()
                .map(msg -> switch (msg.getRoleType()) {
                    case "user" -> new UserMessage(msg.getContent());
                    case "assistant" -> new AssistantMessage(msg.getContent());
                    default -> new SystemMessage(msg.getContent());
                }).collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        chatMessageMapper.update(
                new LambdaUpdateWrapper<>(new ChatMessage())
                        .eq(ChatMessage::getSessionId, conversationId)
                        .set(ChatMessage::getIsDeleted,1)
        );
        chatSessionMapper.update(
                new LambdaUpdateWrapper<>(new ChatSession())
                        .eq(ChatSession::getSessionId, conversationId)
                        .set(ChatSession::getIsDeleted,1)
        );
    }

    /**
     * 用于分割标题
     * @param input
     * @return
     */
    private String findFirstMatch(String input) {
        if (input == null) {
            return null;
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ',') {
                // 检查英文逗号
                return ",";
            } else if (c == '，') {
                // 检查中文逗号
                return "，";
            }
        }
        return "";
    }

    /**
     * 判断是否json
     */
    public boolean isJson(String str) {
        try {
            new ObjectMapper().readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
