package com.xik.aibookkeeping.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了think和act的方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{

    /**
     * 可用的工具
     */
    private final ToolCallback[] availableTools;

    /**
     * 保存了工具调用信息的响应
     */
    private ChatResponse tooCallChatResponse;

    /**
     *  保存了工具调用消息的相应
     */
    private final ToolCallingManager toolCallingManager;

    /**
     * 禁用内置的工具调用机制 自己维护山下文
     */
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用SpringAI　内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }


    /**
     * 处理当前状态并决定下一步行动
     * @return
     */
    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 获取工具选项的响应
            ChatResponse chatResponse = getChatClient()
                    .prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 记录相应 用于ACT
            this.tooCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示信息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            log.info(getName() + "的思考：" + result);
            log.info(getName() + "选择了：" + toolCalls.size() + "个工具来使用");
            String toolCallInfo = toolCalls.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCalls.isEmpty()) {
                // 只有不调用工具时，才记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到问题", e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到错误：" + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行工具并调用执行结果
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!tooCallChatResponse.hasToolCalls()) {
            // 没有工具调用
            return "没有工具调用";
        }
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, tooCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        // 当前工具的调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String result = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具：" + toolResponse.name() + "完成了他的任务！结果：" + toolResponse.responseData())
                .collect(Collectors.joining("\n"));
        // 判断是否使用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                        .anyMatch(toolResponse -> "doTerminate".equals(toolResponse.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(result);
        return result;
    }
}
