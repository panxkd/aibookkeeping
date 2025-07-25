package com.xik.aibookkeeping.server.controller.user;

import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.server.service.IUserSelectorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户选择的智能体
 */
@RestController
@RequestMapping("/user/selector")
@Slf4j
public class UserSelectorAgentController {

    @Resource
    private IUserSelectorService userSelectorService;

    /**
     * 获取当前用户选择的智能体id
     * @return
     */
    @GetMapping
    public Result<Long> getUserSelectorAgentId() {
        log.info("查询当前用户选择的智能体");
        Long userId = BaseContext.getCurrentId();
        Long agentId = userSelectorService.getAgentId(userId);
        return Result.success(agentId);
    }

    @PostMapping("/{agentId}")
    public Result updateUserSelectorAgentId(@PathVariable Long agentId) {
        log.info("用户选择的智能体id:{}", agentId);
        Long  userId = BaseContext.getCurrentId();
        userSelectorService.updateAgentId(userId,agentId);
        return Result.success();
    }
}
