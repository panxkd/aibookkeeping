package com.xik.aibookkeeping.server.interceptor;


import com.xik.aibookkeeping.common.constant.JwtClaimsConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.context.UserContextHolder;
import com.xik.aibookkeeping.common.properties.JwtProperties;
import com.xik.aibookkeeping.common.utils.JwtUtil;
import com.xik.aibookkeeping.pojo.context.UserContext;
import com.xik.aibookkeeping.pojo.entity.User;
import com.xik.aibookkeeping.server.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;



/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private UserMapper userMapper;

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
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：{}", userId);
            //将当前id存储
            BaseContext.setCurrentId(userId);
            User user = userMapper.selectById(userId);
            UserContext userContext = UserContext.builder()
                    .userId(user.getId())
                    .username(user.getNickname())
                    .userType("USER")
                    .build();
            UserContextHolder.set(userContext);
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
