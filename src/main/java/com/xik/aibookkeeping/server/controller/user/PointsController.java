package com.xik.aibookkeeping.server.controller.user;


import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.server.service.IPointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.beans.beancontext.BeanContext;

/**
 * <p>
 * 积分表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("UserPointsController")
@RequestMapping("/user/points")
@Slf4j
public class PointsController {

    @Resource
    private IPointsService pointsService;

    /**
     * 获取当前用户的积分
     * @return
     */
    @GetMapping
    public Result<Integer> getUserPoints() {
        log.info("获取当前用户的积分");
        Long userId = BaseContext.getCurrentId();
        Integer points = pointsService.getUserPoints(userId);
        return Result.success(points);
    }
    

}
