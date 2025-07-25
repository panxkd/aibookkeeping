package com.xik.aibookkeeping.server.config;

import com.xik.aibookkeeping.server.filter.LogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class FilterConfig {

//    @Bean
    public FilterRegistrationBean<LogFilter> logFilterRegistration(LogFilter logFilter) {
        FilterRegistrationBean<LogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(logFilter);
        registration.setName("logFilter");
        registration.setOrder(1); // 过滤器顺序

        // 拦截所有请求（实际内部已在 LogFilter 中排除 /chat/sse）
        registration.addUrlPatterns("/*");

        return registration;
    }

//    @Bean
    public LogFilter logFilter() {
        return new LogFilter();
    }
}
