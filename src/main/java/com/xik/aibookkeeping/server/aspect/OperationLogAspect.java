package com.xik.aibookkeeping.server.aspect;


import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.constant.AutoFillConstant;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.exception.AspectException;
import com.xik.aibookkeeping.pojo.entity.Admin;
import com.xik.aibookkeeping.pojo.entity.User;
import com.xik.aibookkeeping.server.mapper.AdminMapper;
import com.xik.aibookkeeping.server.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 切面类更新 插入自动记录操作人和时间
 */
@Component
@Slf4j
@Aspect
public class OperationLogAspect {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    // 切入点
    @Pointcut("@annotation(com.xik.aibookkeeping.common.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    // 前置通知
    @Before("operationLogPointcut()")
    public void operationLog(JoinPoint joinPoint) {
        // 开始对公告字段填充
        log.info("开始公共字段填充");
        // 获取当前被拦截方法上的操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLog operationLog = signature.getMethod().getAnnotation(OperationLog.class);
        OperationType operationType = operationLog.value();
        // 获取当前方法的参数
        Object[] args = joinPoint.getArgs();
        if (args == null && args.length == 0) {
            return;
        }
        // 获取实体对象
        //获取实体对象
        Object entity = args[0];
        Long currentId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        if (currentId == null) {
            log.warn("用户不存在，跳过自动填充");
            return;
        }
        // 处理参数对象
        try {
            // 通过反射获取字段名
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER_NICKNAME, String.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER_NICKNAME, String.class);
            if (operationType == OperationType.ADMIN_INSERT ||  operationType == OperationType.ADMIN_UPDATE) {
                String adminNickname = currentId + ".ADMIN." + getAdminNickname(currentId);
                if (operationType == OperationType.ADMIN_INSERT) {
                    //通过反射为对象赋值
                    setCreateTime.invoke(entity,now);
                    setUpdateTime.invoke(entity,now);
                    setCreateUser.invoke(entity,adminNickname);
                    setUpdateUser.invoke(entity,adminNickname);
                } else {
                    setUpdateTime.invoke(entity,now);
                    setCreateUser.invoke(entity,adminNickname);
                }
            } else {
                String userNickname = currentId + ".USER." + getUserNickname(currentId) ;
                if (operationType == OperationType.USER_INSERT) {
                    //通过反射为对象赋值
                    setCreateTime.invoke(entity,now);
                    setUpdateTime.invoke(entity,now);
                    setCreateUser.invoke(entity,userNickname);
                    setUpdateUser.invoke(entity,userNickname);
                } else {
                    setUpdateTime.invoke(entity,now);
                    setUpdateUser.invoke(entity,userNickname);
                }
            }
        }  catch (Exception e) {
            throw new AspectException(MessageConstant.ASPECT_ERR + e.getMessage());
        }

    }


    /**
     * 获取管理员昵称
     * @param id
     * @return
     */
    private String getAdminNickname(Long id) {
        Admin admin = adminMapper.selectById(id);
        return admin.getUsername();
    }

    /**
     * 获取用户昵称
     * @param id
     * @return
     */
    private String getUserNickname(Long id) {
        User user = userMapper.selectById(id);
        return user.getNickname();
    }


}
