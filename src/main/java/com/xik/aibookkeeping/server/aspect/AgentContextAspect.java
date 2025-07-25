package com.xik.aibookkeeping.server.aspect;

import com.xik.aibookkeeping.common.context.AgentContextHolder;
import com.xik.aibookkeeping.pojo.dto.ChatMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 *   拦截用户与ai聊天的请求上，拦截并设置agentId存储，用于对话记忆中
 */
@Aspect
@Component
@Slf4j
public class AgentContextAspect {

    /**
     * 拦截所有含有 ChatMessageDTO 参数的方法（你可以更精确地指定包或类）
     */
    @Pointcut("execution(* com.xik.aibookkeeping.server.controller.user.ChatMessageController.doChatWithSync(..)) || " +
            "execution(* com.xik.aibookkeeping.server.controller.user.ChatMessageController.doBill(..))")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void extractAgentId(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ChatMessageDTO dto) {
                Long agentId = dto.getAgentId();
                if (agentId != null) {
                    AgentContextHolder.setAgentId(agentId);
                    log.info("已设置当前 agentId: {}", agentId);
                }
            }
        }
    }

    @After("controllerMethods()")
    public void clear() {
        AgentContextHolder.clear();
        log.info("清除 agentId");
    }
}
