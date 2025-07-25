package com.xik.aibookkeeping.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xik.aibookkeeping.aiagent.client.QianWenAiClient;
import com.xik.aibookkeeping.common.context.AgentContextHolder;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.pojo.dto.ChatMessageDTO;
import com.xik.aibookkeeping.pojo.entity.Agent;
import com.xik.aibookkeeping.server.mapper.AgentMapper;
import com.xik.aibookkeeping.server.mapper.UserAgentMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {
    @Resource
    private QianWenAiClient qianWenAiClient;

    @Resource
    private AgentMapper agentMapper;

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            Long userId = (Long) session.getAttributes().get("userId");
            if (userId != null) {
                userSessions.put(userId, session);
                log.info("WebSocket 连接建立成功：userId={}", userId);
            } else {
                log.warn("WebSocket连接缺少 userId，关闭连接");
                session.close();
            }
        } catch (Exception e) {
            log.error("WebSocket连接建立失败", e);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = null;
        try {
            userId = (Long) session.getAttributes().get("userId");
            if (userId == null) {
                sendTextMessage(session, "[错误] 用户未登录或身份失效");
                session.close();
                return;
            }

            // 解析消息
            ChatMessageDTO dto = mapper.readValue(message.getPayload(), ChatMessageDTO.class);
            Long agentId = dto.getAgentId();
            String chatId = userId + "-" + agentId;

            // 设置上下文
            BaseContext.setCurrentId(userId);
            AgentContextHolder.setAgentId(agentId);

            dto.setChatId(chatId);
            dto.setUserId(userId);

            Agent agent = agentMapper.selectById(agentId);
            if (agent == null) {
                sendTextMessage(session, "[错误] 智能体不存在");
                return;
            }

            dto.setPrompt(agent.getPrompt());

            // 启动流式对话
            qianWenAiClient.doChatSSE(dto)
                    .doOnNext(chunk -> sendTextMessage(session, chunk))
                    .doOnComplete(() -> sendTextMessage(session, "[DONE]"))
                    .doOnError(e -> sendTextMessage(session, "[ERROR] " + e.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("处理消息时发生异常，userId={}, message={}", userId, message.getPayload(), e);
            sendTextMessage(session, "[系统错误] 无法处理请求");
        } finally {
            BaseContext.removeCurrentId();
            AgentContextHolder.clear();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket连接关闭：userId={}, status={}", userId, status);
        }
    }

    /**
     * 工具方法：发送消息到客户端
     */
    private void sendTextMessage(WebSocketSession session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败：{}", message, e);
            }
        }
    }

    /**
     * 可用于服务端主动向用户推送消息
     */
    public void sendToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(String.valueOf(userId));
        sendTextMessage(session, message);
    }
}
