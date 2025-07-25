package com.xik.aibookkeeping.server.controller.user;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.AgentDTO;
import com.xik.aibookkeeping.pojo.dto.AgentPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Agent;
import com.xik.aibookkeeping.server.service.IAgentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 智能体表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("UserAgentController")
@RequestMapping("/user/agent")
@Slf4j
public class AgentController {

    @Resource
    private IAgentService agentService;


    @GetMapping("/{id}")
    public Result<Agent> getAgentById(@PathVariable Long id) {
        log.info("获取智能体：{}", id);
        Agent agent = agentService.getUserAgent(id);
        return Result.success(agent);
    }

    @GetMapping("/page")
    public Result<Page<Agent>> pageAgent(AgentPageQueryDTO agentPageQueryDTO) {
        log.info("分页查询智能体：{}", agentPageQueryDTO);
        Page<Agent> page = agentService.pageUserAgent(agentPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 获取默认智能体id
     * @return
     */
    @GetMapping("/default")
    public Result<Long> getDefaultAgent() {
        log.info("获取默认智能体");
        Long agentId = agentService.getDefaultAgent();
        return Result.success(agentId);
    }
}
