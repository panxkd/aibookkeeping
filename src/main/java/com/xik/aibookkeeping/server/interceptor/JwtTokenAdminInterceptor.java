package com.xik.aibookkeeping.server.interceptor;


import com.xik.aibookkeeping.common.constant.JwtClaimsConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.context.UserContextHolder;
import com.xik.aibookkeeping.common.properties.JwtProperties;
import com.xik.aibookkeeping.common.utils.JwtUtil;
import com.xik.aibookkeeping.pojo.context.UserContext;
import com.xik.aibookkeeping.pojo.entity.Admin;
import com.xik.aibookkeeping.server.mapper.AdminMapper;
import com.xik.aibookkeeping.server.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;



/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Resource
    private JwtProperties jwtProperties;



    @Resource
    private AdminMapper adminMapper;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            log.info("当前管理员id：{}", adminId);
            //将当前id存储
            BaseContext.setCurrentId(adminId);
            // 存储上下文信息
            Admin admin = adminMapper.selectById(adminId);
            UserContext user = UserContext.builder()
                    .userId(admin.getId())
                    .username(admin.getUsername())
                    .userType("ADMIN")
                    .build();
            UserContextHolder.set(user);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清理 ThreadLocal，防止内存泄漏
        BaseContext.removeCurrentId();
        log.info("清理 ThreadLocal 数据");
    }
}
