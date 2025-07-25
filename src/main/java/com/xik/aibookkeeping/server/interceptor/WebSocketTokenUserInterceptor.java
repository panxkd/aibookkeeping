package com.xik.aibookkeeping.server.interceptor;


import com.xik.aibookkeeping.common.constant.JwtClaimsConstant;
import com.xik.aibookkeeping.common.properties.JwtProperties;
import com.xik.aibookkeeping.common.utils.JwtUtil;
import com.xik.aibookkeeping.server.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class WebSocketTokenUserInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtProperties jwtProperties;


    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        try {
            // 1. 获取 token（优先 header，其次 URL 参数）
            String token = null;

            if (request instanceof ServletServerHttpRequest servletRequest) {
                HttpServletRequest httpRequest = servletRequest.getServletRequest();
                token = httpRequest.getParameter(jwtProperties.getUserTokenName());
            }

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket握手失败：缺少Token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // 2. 校验 Token
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("WebSocket握手通过，userId={}", userId);

            // 3. 放入 attributes 供后续 WebSocketSession 使用
            attributes.put("userId", userId);

            return true;

        } catch (Exception e) {
            log.error("WebSocket Token 校验失败", e);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 握手后无处理
    }
}
