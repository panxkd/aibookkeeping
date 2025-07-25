package com.xik.aibookkeeping.server.filter;

import com.alibaba.fastjson2.JSON;
import com.xik.aibookkeeping.common.context.UserContextHolder;
import com.xik.aibookkeeping.pojo.context.UserContext;
import com.xik.aibookkeeping.pojo.entity.RequestLog;
import com.xik.aibookkeeping.pojo.message.RequestLogMessage;
import com.xik.aibookkeeping.server.rabbitmq.producer.RequestProducer;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
public class LogFilter implements Filter {

    @Resource
    private RequestProducer requestProducer;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        String upgradeHeader = request.getHeader("Upgrade");
        String connectionHeader = request.getHeader("Connection");

        String method = request.getMethod();

        // 排除 WebSocket 握手请求
        if ("websocket".equalsIgnoreCase(upgradeHeader) &&
                connectionHeader != null &&
                connectionHeader.toLowerCase().contains("upgrade")) {
            chain.doFilter(request, response);
            return;
        }

        // 你的原有排除逻辑
        if (
                uri.contains("/chat/sse") ||
                        uri.contains("/bill/sse") ||
                        uri.contains("/request-log/page") ||
                        (uri.matches("^/request-log/[^/]+$") && "GET".equalsIgnoreCase(method)) ||
                        uri.contains("/common/upload") ||
                        (acceptHeader != null && acceptHeader.contains("text/event-stream"))
        ) {
            chain.doFilter(request, response);
            return;
        }


        // 包装请求/响应用于缓存
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long cost = System.currentTimeMillis() - start;

            // 获取请求体、响应体、请求头
            String requestBody = getRequestBody(wrappedRequest);
            String responseBody = getResponseBody(wrappedResponse);
            String headers = getHeadersJson(wrappedRequest);

            // 获取当前用户上下文
            UserContext userContext = UserContextHolder.getCurrentUser();
            String userType = null;
            Long userId = null;
            String username = null;
            if (userContext != null) {
                userType = userContext.getUserType();
                userId = userContext.getUserId();
                username = userContext.getUsername();
            }

            // 设置日志类型
            int status = response.getStatus();
            String logType = getLogTypeByStatus(status);

            // 构建日志对象
            RequestLogMessage logMessage = RequestLogMessage.builder()
                    .requestId(UUID.randomUUID().toString())
                    .logType(logType) // info / warn / error
                    .requestMethod(request.getMethod())
                    .requestUrl(getFullURL(request))
                    .parameters(safeJson(requestBody))
                    .requestHeaders(safeJson(headers))
                    .ipAddress(getRealIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .status(String.valueOf(status))
                    .userType(userType)
                    .userId(userId)
                    .username(username)
                    .dataAfter(safeJson(responseBody))
                    .executionTime((int) cost)
                    .createTime(LocalDateTime.now())
                    .token(request.getHeader("Authorization"))
                    .isDeleted(0)
                    .build();

            // 发送日志
            requestProducer.saveLog(logMessage);

            // 写回响应体
            wrappedResponse.copyBodyToResponse();

            // 清理用户上下文
            if (userContext != null) {
                log.info("清除用户上下文");
                UserContextHolder.clear();
            }
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length == 0) return "";
        try {
            String charset = Optional.ofNullable(request.getCharacterEncoding()).orElse(StandardCharsets.UTF_8.name());
            return new String(buf, charset);
        } catch (UnsupportedEncodingException e) {
            return "[请求体解析失败]";
        }
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length == 0) return "";
        try {
            String charset = Optional.ofNullable(response.getCharacterEncoding()).orElse(StandardCharsets.UTF_8.name());
            return new String(buf, charset);
        } catch (UnsupportedEncodingException e) {
            return "[响应体解析失败]";
        }
    }

    private String getHeadersJson(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return JSON.toJSONString(headers);
    }

    private String safeJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "{}"; // 返回一个合法空 JSON
        }
        return json;
    }

    private String getFullURL(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String query = request.getQueryString();
        return query != null ? url + "?" + query : url;
    }

    private String getRealIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getLogTypeByStatus(int status) {
        if (status >= 500) return "error";
        if (status >= 400) return "warn";
        return "info";
    }
}