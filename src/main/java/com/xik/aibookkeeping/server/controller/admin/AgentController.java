package com.xik.aibookkeeping.server.controller.admin;


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

import java.util.List;

/**
 * <p>
 * 智能体表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminAgentController")
@RequestMapping("/admin/agent")
@Slf4j
public class AgentController {

    @Resource
    private IAgentService agentService;

    @PostMapping
    public Result addAgent(@RequestBody AgentDTO agentDTO) {
        log.info("添加的智能体：{}", agentDTO);
        Agent agent = new Agent();
        BeanUtils.copyProperties(agentDTO, agent);
        agentService.addAgent(agent);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteAgent(@PathVariable Long id) {
        log.info("删除智能体：{}", id);
        agentService.deleteAgentById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Agent> getAgentById(@PathVariable Long id) {
        log.info("获取智能体：{}", id);
        Agent agent = agentService.getAgent(id);
        return Result.success(agent);
    }

    @GetMapping("/page")
    public Result<Page<Agent>> pageAgent(AgentPageQueryDTO agentPageQueryDTO) {
        log.info("分页查询智能体：{}", agentPageQueryDTO);
        Page<Agent> page = agentService.pageAgent(agentPageQueryDTO);
        return Result.success(page);
    }

    @PutMapping
    public Result updateAgent(@RequestBody AgentDTO agentDTO) {
        log.info("修改智能体：{}", agentDTO);
        Agent agent = new Agent();
        BeanUtils.copyProperties(agentDTO, agent);
        agentService.updateAgent(agent);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result updateAgentStatus(@PathVariable Integer status, Long id) {
        log.info("修改智能体状态：{}, id:{}", status,id);
        Agent agent = new Agent();
        agent.setStatus(status);
        agent.setId(id);
        agentService.updateAgent(agent);
        return Result.success();
    }

}
