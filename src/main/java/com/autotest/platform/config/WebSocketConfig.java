package com.autotest.platform.config;

import com.autotest.platform.websocket.ReportWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置
 *
 * @author autotest
 * @date 2024-01-01
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ReportWebSocketHandler reportWebSocketHandler;

    public WebSocketConfig(ReportWebSocketHandler reportWebSocketHandler) {
        this.reportWebSocketHandler = reportWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册报告WebSocket处理器
        registry.addHandler(reportWebSocketHandler, "/ws/reports")
                .setAllowedOrigins("*") // 允许跨域
                .withSockJS(); // 启用SockJS支持

        // 注册执行监控WebSocket处理器
        registry.addHandler(reportWebSocketHandler, "/ws/execution")
                .setAllowedOrigins("*")
                .withSockJS();

        // 注册系统监控WebSocket处理器
        registry.addHandler(reportWebSocketHandler, "/ws/monitor")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}