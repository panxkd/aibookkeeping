package com.xik.aibookkeeping.aiagent.client;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xik.aibookkeeping.aiagent.advisor.AiLoggerAdvisor;
import com.xik.aibookkeeping.aiagent.chatmemory.DataChatMemory;
import com.xik.aibookkeeping.aiagent.rag.QueryRewriter;
import com.xik.aibookkeeping.pojo.dto.ChatMessageDTO;
import com.xik.aibookkeeping.pojo.entity.*;
import com.xik.aibookkeeping.server.mapper.AgentMapper;
import com.xik.aibookkeeping.server.mapper.ChatMessageMapper;
import com.xik.aibookkeeping.server.mapper.UserAgentMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class QianWenAiClient {

    private final ChatClient chatClient;


    @Resource
    private Advisor kowledgeRagCloudAdvisor;

    @Resource
    private Advisor categoryRagCloudAdvisor;

    private final DataChatMemory dataChatMemory;

    @Resource
    private QueryRewriter queryRewriter;

    // ai调用工具
    @Resource
    private ToolCallback[] allTools;


    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Resource
    private AgentMapper agentMapper;

    @Resource
    private UserAgentMapper userAgentMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;


    private static final String SYSTEM_PROMPT = """
                                你叫{name}，是一个智能记账的工具，并且有丰富的理财知识
                                用户开心时说话风格幽默有趣，用户不开心时会安慰人。
                                根据用户提供的记账消息进行有情绪价值的回复，像女朋友一样。
                                """;
    private static final String BILL_PROMPT = """
                                你是一个智能记账助手，请根据用户的输入判断是否需要记账：
                                - 如果需要记账，请严格返回以下 JSON 格式（不能省略字段），不能修改id的值：
                                
                                {jsonExample}
                                
                                - 用户没有输入金额，请输出 null。
                                """;


    /**
     * AI客户端初始化
     * @param dashscopeChatModel
     */
    public QianWenAiClient(ChatModel dashscopeChatModel, DataChatMemory dataChatMemory) {
        this.dataChatMemory = dataChatMemory;
        // 基于数据库的记忆初始化
        chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(dataChatMemory),
                        // 自定义日志 Advisor 按需开启
                        new AiLoggerAdvisor()
                )
                .build();
    }


    /**
     * 多轮对话
     * @param message
     * @return
     */
    public Flux<String> doChatSSE(ChatMessageDTO message) {
        // 重写用户的查询信息
//        String rewrittenMessage = queryRewriter.doQueryRewriter(message.getMessage());
        // 根据传递过来的AgentId查询使用哪个Agent
        LambdaQueryWrapper<Agent> queryAgent =  new LambdaQueryWrapper<>();
        queryAgent.eq(Agent::getIsDeleted,0).eq(Agent::getId,message.getAgentId()).eq(Agent::getStatus,1);
        Agent agent = agentMapper.selectOne(queryAgent);
        String name = agent.getName();
        // 查询用户是否给了备注
        LambdaQueryWrapper<UserAgent> queryUserAgent = new LambdaQueryWrapper<>();
        queryUserAgent
                .eq(UserAgent::getAgentId,message.getAgentId())
                .eq(UserAgent::getIsDeleted,0)
                .eq(UserAgent::getUserId,message.getUserId());
        UserAgent userAgent = userAgentMapper.selectOne(queryUserAgent);
        if (userAgent != null && userAgent.getRemark() != null) {
            name = userAgent.getRemark();
        }
        // 构造模板对象
        String prompt = SYSTEM_PROMPT;
        if (agent.getPrompt() != null) {
            prompt = agent.getPrompt();
        }
        SystemPromptTemplate template = new SystemPromptTemplate(prompt);
        // 替换变量
        String finalPrompt = template.createMessage(Map.of("name", name)).getText();
        return chatClient
                .prompt(finalPrompt)
                .user(message.getMessage())
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, message.getChatId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                // 基于阿里云知识库
                .advisors(kowledgeRagCloudAdvisor)
                .stream()
                .content();
    }

    /**
     * 多轮对话
     * @param message
     * @return
     */
    public String doChat(ChatMessageDTO message) {
        // 重写用户的查询信息
//        String rewrittenMessage = queryRewriter.doQueryRewriter(message.getMessage());
        // 根据传递过来的AgentId查询使用哪个Agent
        LambdaQueryWrapper<Agent> queryAgent =  new LambdaQueryWrapper<>();
        queryAgent.eq(Agent::getIsDeleted,0).eq(Agent::getId,message.getAgentId()).eq(Agent::getStatus,1);
        Agent agent = agentMapper.selectOne(queryAgent);
        String name = agent.getName();
        // 查询用户是否给了备注
        LambdaQueryWrapper<UserAgent> queryUserAgent = new LambdaQueryWrapper<>();
        queryUserAgent
                .eq(UserAgent::getAgentId,message.getAgentId())
                .eq(UserAgent::getIsDeleted,0)
                .eq(UserAgent::getUserId,message.getUserId());
        UserAgent userAgent = userAgentMapper.selectOne(queryUserAgent);
        if (userAgent != null && userAgent.getRemark() != null) {
            name = userAgent.getRemark();
        }
        // 构造模板对象
        String prompt = SYSTEM_PROMPT;
        if (agent.getPrompt() != null) {
            prompt = agent.getPrompt();
        }
        SystemPromptTemplate template = new SystemPromptTemplate(prompt);
        // 替换变量
        String finalPrompt = template.createMessage(Map.of("name", name)).getText();
        ChatResponse chatResponse = chatClient
                .prompt(finalPrompt)
                .user(message.getMessage())
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, message.getChatId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                // 基于阿里云知识库
                .advisors(kowledgeRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }



    /**
     * 结构化输出
     * @param message
     * @return
     */
    public AiBillRecord doChatWithBill(ChatMessageDTO message) throws JsonProcessingException {
        String id = UUID.randomUUID().toString();
        // 构建动态 JSON 示例
        String jsonExample = """
                    {
                      "id": "%s",
                      "billRecordList": [
                        {
                          "amount": 金额（数字）,
                          "type": "revenue" 或 "expenditures" 或 "notRecorded",
                          "category": 分类（如“餐饮”），
                          "remark": 备注说明
                        },
                        {
                          "amount": 金额（数字）,
                          "type": "revenue" 或 "expenditures" 或 "notRecorded",
                          "category": 分类（如“餐饮”），
                          "remark": 备注说明
                        },...
                      ]
                    }
                    """.formatted(id);
        SystemPromptTemplate template = new SystemPromptTemplate(BILL_PROMPT);
        String finalPrompt = template.createMessage(Map.of("jsonExample", jsonExample)).getText();

        AiBillRecord aiBillRecord = chatClient
                .prompt(finalPrompt)
                .user(message.getMessage())
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, message.getChatId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 0))
                .advisors(categoryRagCloudAdvisor)
                .call()
                .entity(new ParameterizedTypeReference<AiBillRecord>() {});
        // 根据uuid获取存储的记忆
        LambdaQueryWrapper<ChatMessage> queryChatMessage = new LambdaQueryWrapper<>();
        queryChatMessage
                .eq(ChatMessage::getIsDeleted,0)
                .eq(ChatMessage::getRoleType,"assistant")
                .eq(ChatMessage::getMessageType,"json")
                .apply("JSON_UNQUOTE(JSON_EXTRACT(content, '$.id')) = {0}", id);
        ChatMessage chatMessage = chatMessageMapper.selectOne(queryChatMessage);
        if (chatMessage == null) {
            return null;
        }

        String content = chatMessage.getContent();
        ObjectMapper objectMapper = new ObjectMapper();
        AiBillRecord result = objectMapper.readValue(content, AiBillRecord.class);

        // 替换 billList 中的 uuid（按顺序）
        List<BillRecord> originList = null;
        if (aiBillRecord != null) {
            originList = aiBillRecord.getBillRecordList();
        }
        List<BillRecord> storedList = result.getBillRecordList();

        if (originList != null) {
            for (int i = 0; i < Math.min(originList.size(), storedList.size()); i++) {
                BillRecord fromAI = originList.get(i);
                BillRecord fromMemory = storedList.get(i);
                fromAI.setUuid(fromMemory.getUuid());
            }
        }

        log.info("parsed bill: {}", aiBillRecord);
        return aiBillRecord;
    }


    public String doChatWithTool(ChatMessageDTO message) {
        // 根据传递过来的AgentId查询使用哪个Agent
        LambdaQueryWrapper<Agent> queryAgent =  new LambdaQueryWrapper<>();
        queryAgent.eq(Agent::getIsDeleted,0).eq(Agent::getId,message.getAgentId()).eq(Agent::getStatus,1);
        Agent agent = agentMapper.selectOne(queryAgent);
        String name = agent.getName();
        // 查询用户是否给了备注
        LambdaQueryWrapper<UserAgent> queryUserAgent = new LambdaQueryWrapper<>();
        queryUserAgent
                .eq(UserAgent::getAgentId,message.getAgentId())
                .eq(UserAgent::getIsDeleted,0)
                .eq(UserAgent::getUserId,message.getUserId());
        UserAgent userAgent = userAgentMapper.selectOne(queryUserAgent);
        if (userAgent != null && userAgent.getRemark() != null) {
            name = userAgent.getRemark();
        }
        // 构造模板对象
        String prompt = SYSTEM_PROMPT;
        if (agent.getPrompt() != null) {
            prompt = agent.getPrompt();
        }
        SystemPromptTemplate template = new SystemPromptTemplate(prompt);
        // 替换变量
        String finalPrompt = template.createMessage(Map.of("name", name)).getText();
        ChatResponse chatResponse =  chatClient
                .prompt(finalPrompt)
                .user(message.getMessage())
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, message.getChatId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                // 基于阿里云知识库
                .advisors(kowledgeRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }





}
