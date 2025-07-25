package com.xik.aibookkeeping.server.config;


import com.xik.aibookkeeping.server.interceptor.WebSocketTokenUserInterceptor;
import com.xik.aibookkeeping.server.websocket.BillWebSocketHandler;
import com.xik.aibookkeeping.server.websocket.ChatWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final BillWebSocketHandler billWebSocketHandler;

    @Resource
    private WebSocketTokenUserInterceptor webSocketTokenUserInterceptor;

    @Autowired
    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, BillWebSocketHandler billWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.billWebSocketHandler = billWebSocketHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")  //URL地址
                .setAllowedOrigins("*")  //允许跨域
                .addInterceptors(webSocketTokenUserInterceptor);
        registry.addHandler(billWebSocketHandler, "/ws/bill")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketTokenUserInterceptor);
    }
}
