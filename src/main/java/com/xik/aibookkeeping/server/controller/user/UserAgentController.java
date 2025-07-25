package com.xik.aibookkeeping.server.controller.user;


import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.UserAgentDTO;
import com.xik.aibookkeeping.pojo.entity.UserAgent;
import com.xik.aibookkeeping.server.service.IUserAgentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户-智能体关联表 自定义备注/头像 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("UserUserAgentController")
@RequestMapping("/user/user-agent")
@Slf4j
public class UserAgentController {

    @Resource
    private IUserAgentService userAgentService;

    /**
     * 获取用户所有的智能体
     * @return
     */
    @GetMapping
    public Result<List<UserAgent>> getUserAgent() {
        log.info("获取用户所拥有的智能体");
        List<UserAgent> userAgent = userAgentService.getUserAgentByUserId();
        return Result.success(userAgent);

    }

    /**
     * 解锁智能体
     * @param id
     * @return
     */
    @PostMapping("/{id}")
    public Result addUserAgent(@PathVariable Long id) {
        log.info("解锁智能体：{}", id);
        Long userId = BaseContext.getCurrentId();
        UserAgent userAgent = new UserAgent();
        userAgent.setUserId(userId);
        userAgent.setAgentId(id);
        userAgentService.addUserAgent(userAgent);
        return Result.success();
    }

    @PutMapping
    public Result updateUserAgent(@RequestBody UserAgentDTO userAgentDTO) {
        log.info("用户修改智能体 备注/头像：{}", userAgentDTO);
        Long userId = BaseContext.getCurrentId();
        UserAgent userAgent = new UserAgent();
        BeanUtils.copyProperties(userAgentDTO, userAgent);
        userAgent.setUserId(userId);
        userAgentService.updateAgent(userAgent);
        return Result.success();
    }



}
