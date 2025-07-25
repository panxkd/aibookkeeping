package com.xik.aibookkeeping.server.controller.user;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.aiagent.client.QianWenAiClient;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.ChatMessageDTO;
import com.xik.aibookkeeping.pojo.dto.ChatMessagePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Agent;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.entity.ChatMessage;
import com.xik.aibookkeeping.pojo.vo.ChatMessageVO;
import com.xik.aibookkeeping.pojo.vo.ResponseMessageVO;
import com.xik.aibookkeeping.server.mapper.AgentMapper;
import com.xik.aibookkeeping.server.service.IChatMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * AI消息记录表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-07-02
 */
@RestController
@RequestMapping("/user/chat-message")
@Slf4j
public class ChatMessageController {

    @Resource
    private IChatMessageService chatMessageService;

    /**
     * 流式输出
     * @param chatMessageDTO
     * @return
     */
    @PostMapping("/chat/sse")
    public SseEmitter doChatWithSync(@RequestBody ChatMessageDTO chatMessageDTO) {
        log.info("用户发送的消息：{}", chatMessageDTO);
        return  chatMessageService.getResponse(chatMessageDTO);
    }

    /**
     * 创建订单 流式输出
     * @param chatMessageDTO
     * @return
     */
    @PostMapping("/bill/sse")
    public SseEmitter doBillWithSync(@RequestBody ChatMessageDTO chatMessageDTO) {
        log.info("用户发送的消息：{}", chatMessageDTO);
        return chatMessageService.doBillWithSync(chatMessageDTO);

    }

    /**
     * 创建订单 同步输出
     * @param chatMessageDTO
     * @return
     */
    @PostMapping("/bill")
    public Result<List<Bill>> doBill(@RequestBody ChatMessageDTO chatMessageDTO) {
        log.info("用户发送的消息：{}", chatMessageDTO);
        List<Bill> billList = chatMessageService.getBill(chatMessageDTO);
        return Result.success(billList);
    }

    /**
     * 同步输出
     * @param chatMessageDTO
     * @return
     */
    @PostMapping("/chat")
    public Result<ResponseMessageVO> doChat(@RequestBody ChatMessageDTO chatMessageDTO) {
        log.info("用户发送的消息：{}", chatMessageDTO);
        ResponseMessageVO responseMessageVO = chatMessageService.doChat(chatMessageDTO);
        return  Result.success(responseMessageVO);
    }

    /**
     * 获取当前用户的聊天记录
     * @param chatMessagePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<Page<ChatMessageVO>> page(ChatMessagePageQueryDTO chatMessagePageQueryDTO) {
        log.info("分页查询聊天记录：{}", chatMessagePageQueryDTO);
        Page<ChatMessageVO> page = chatMessageService.pageChatMessage(chatMessagePageQueryDTO);
        return Result.success(page);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable String id) {
        log.info("删除聊天记录");
        chatMessageService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteByIds(@RequestBody List<Long> ids) {
        log.info("批量删除聊天记录");
        chatMessageService.deleteByIds(ids);
        return Result.success();
    }

    @DeleteMapping("/all")
    public Result deleteAll() {
        log.info("删除所有聊天记录");
        chatMessageService.deleteAll();
        return Result.success();
    }
}
