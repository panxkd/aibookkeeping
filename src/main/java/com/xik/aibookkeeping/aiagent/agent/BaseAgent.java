package com.xik.aibookkeeping.aiagent.agent;



import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *  抽象代理类，用于管理代理状态和执行流程
 *
 *  提供状态转换、内存管理和基于步骤的执行循环的基础功能
 *  子类必须实现step方法
 */
@Data
@Slf4j
public abstract class BaseAgent {

    /**
     * 核心属性
     */
    private String name;

    /**
     * 提示词
     */
    private String systemPrompt;
    private String nextStepPrompt;

    /**
     * 状态
     */
    private AgentState state = AgentState.IDEL;

    /**
     * 执行控制
     */
    private int maxSteps = 10;
    private int currentStep = 0;

    /**
     * LLM
     */
    private ChatClient chatClient;

    /**
     * Memory 需要自主维护上下文
     */
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理状态
     * @param userPrompt
     * @return
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(3600000L);
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDEL) {
                    sseEmitter.send("错误，无法从当前状态运行代理" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StringUtil.isEmpty(userPrompt)) {
                    sseEmitter.send("错误，不能用空运行代理代理" + this.state);
                    sseEmitter.complete();
                    return;
                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }

            //更改状态  运行状态
            state = AgentState.RUNNING;
            // 记录上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                for (int i = 0; i < this.maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    int currentStep = stepNumber;
                    log.info("Executing step " + stepNumber + "/" + maxSteps);
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    // 输出当前结果给sse
                    sseEmitter.send(result);
                }
                // 检查是否超出执行步骤限制
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    sseEmitter.send("执行结束：达到最大步骤(" + maxSteps + ")");
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent step: " + currentStep, e);
                try {
                    sseEmitter.send("执行错误" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(e);
                }
            } finally {
                this.cleanup();
            }
        });
        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE 响应超时");
        });
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE 响应完成");
        });
        return sseEmitter;

    }

    /**
     *  执行单个步骤
     * @return 执行步骤结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){
        // 子类重写该方法来清理资源
    }
}
