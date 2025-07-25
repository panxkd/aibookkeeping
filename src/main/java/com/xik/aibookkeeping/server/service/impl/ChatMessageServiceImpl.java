package com.xik.aibookkeeping.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xik.aibookkeeping.aiagent.client.QianWenAiClient;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.AgentContextHolder;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.exception.ChatException;
import com.xik.aibookkeeping.pojo.dto.ChatMessagePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.*;
import com.xik.aibookkeeping.pojo.dto.ChatMessageDTO;
import com.xik.aibookkeeping.pojo.vo.ChatMessageVO;
import com.xik.aibookkeeping.pojo.vo.ResponseMessageVO;
import com.xik.aibookkeeping.server.mapper.AgentMapper;
import com.xik.aibookkeeping.server.mapper.BillMapper;
import com.xik.aibookkeeping.server.mapper.CategoryMapper;
import com.xik.aibookkeeping.server.mapper.ChatMessageMapper;
import com.xik.aibookkeeping.server.service.IBillService;
import com.xik.aibookkeeping.server.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * AI消息记录表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-07-02
 */
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Resource
    private QianWenAiClient qianWenAiClient;

    @Resource
    private AgentMapper agentMapper;

    @Resource
    private CategoryMapper  categoryMapper;

    @Resource
    private IBillService billService;

    @Resource
    private BillMapper billMapper;





    @Override
    public SseEmitter getResponse(ChatMessageDTO chatMessageDTO) {
        try {
            // 创建一个超时时间较长的 SseEmitter
            SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
            Long userId = BaseContext.getCurrentId();
            Long agentId = chatMessageDTO.getAgentId();
            String chatId = String.valueOf(userId) + "-" + String.valueOf(agentId);
            chatMessageDTO.setChatId(chatId);
            chatMessageDTO.setUserId(userId);
            Agent agent = agentMapper.selectById(chatMessageDTO.getAgentId());
            chatMessageDTO.setPrompt(agent.getPrompt());
            // 获取 Flux 数据流并直接订阅
            qianWenAiClient.doChatSSE(chatMessageDTO)
                    .subscribe(
                            // 处理每条消息
                            chunk -> {
                                try {
                                    emitter.send(chunk);
                                } catch (IOException e) {
                                    emitter.completeWithError(e);
                                }
                            },
                            // 处理错误
                            emitter::completeWithError,
                            // 处理完成
                            emitter::complete
                    );
            // 返回emitter
            return emitter;
        } catch (Exception e) {
            throw new ChatException(MessageConstant.AGENT_REPLY_ERR);
        }
    }

    @Override
    public List<Bill> getBill(ChatMessageDTO chatMessageDTO) {
        try {
            Long userId = BaseContext.getCurrentId();
            Long agentId = AgentContextHolder.getAgentId();
            String chatId = String.valueOf(userId) + "-" + String.valueOf(agentId);
            chatMessageDTO.setChatId(chatId);
            chatMessageDTO.setUserId(userId);
            Agent agent = agentMapper.selectById(chatMessageDTO.getAgentId());
            chatMessageDTO.setPrompt(agent.getPrompt());
            AiBillRecord aiBillRecord = qianWenAiClient.doChatWithBill(chatMessageDTO);
            List<BillRecord> billRecordList = aiBillRecord.getBillRecordList();
            if (billRecordList == null || billRecordList.isEmpty()) {
                return Collections.emptyList();
            }

            List<Bill> billList = new ArrayList<>();

            for (BillRecord billRecord : billRecordList) {
                if (billRecord == null) {
                    continue; // 跳过空对象，避免 NPE
                }

                Bill bill = new Bill()
                        .setUserId(userId)
                        .setAmount(billRecord.getAmount())
                        .setRemark(billRecord.getRemark())
                        .setUuid(billRecord.getUuid())
                        .setIsAutoGenerated(1)
                        .setUuid(billRecord.getUuid())
                        .setType(billRecord.getType());

                // 查询分类ID
                LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper
                            .eq(Category::getIsDeleted, 0)
                            .eq(Category::getCategory, billRecord.getCategory())
                            .and(wrapper -> wrapper.eq(Category::getIsSystem, 1)
                                    .or()
                                    .eq(Category::getUserId, userId));
                Category category = categoryMapper.selectOne(queryWrapper);
                if (category != null && category.getId() != null) {
                    bill.setCategoryId(category.getId());
                } else {
                    LambdaQueryWrapper<Category> queryCategory = new LambdaQueryWrapper<>();
                    queryCategory
                            .eq(Category::getIsDeleted, 0)
                            .eq(Category::getType, billRecord.getType())
                            .eq(Category::getStatus,1)
                            .eq(Category::getCategory,"其他");
                    bill.setCategoryId(categoryMapper.selectOne(queryCategory).getId());
                }
                Bill saveBill = billService.saveAutoBill(bill);
                billList.add(saveBill);

            }

            return billList;
        } catch (Exception e) {
            throw new ChatException(MessageConstant.AGENT_REPLY_ERR);
        }
    }

    /**
     * 同步输出
     * @param chatMessageDTO
     * @return
     */
    @Override
    public ResponseMessageVO doChat(ChatMessageDTO chatMessageDTO) {
        try {
            Long userId = BaseContext.getCurrentId();
            Long agentId = chatMessageDTO.getAgentId();
            String chatId = String.valueOf(userId) + "-" + String.valueOf(agentId);
            chatMessageDTO.setChatId(chatId);
            chatMessageDTO.setUserId(userId);
            Agent agent = agentMapper.selectById(chatMessageDTO.getAgentId());
            chatMessageDTO.setPrompt(agent.getPrompt());
            String response = qianWenAiClient.doChat(chatMessageDTO);
            return ResponseMessageVO.builder()
                    .responseMessage(response)
                    .responseTime(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            throw new ChatException(MessageConstant.AGENT_REPLY_ERR + e.getMessage());
        }
    }

    /**
     *
     * @param chatMessagePageQueryDTO
     * @return
     */
    @Override
    public Page<ChatMessageVO> pageChatMessage(ChatMessagePageQueryDTO chatMessagePageQueryDTO) {
        try {
            Long userId = BaseContext.getCurrentId();
            // 先查询chat_message表得到历史数据
            Page<ChatMessage> pageQuery = new Page<>(chatMessagePageQueryDTO.getPage(), chatMessagePageQueryDTO.getPageSize());
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMessage::getIsDeleted, 0)
                    .orderByDesc(ChatMessage::getCreateTime)
                    .likeRight(ChatMessage::getSessionId, userId + "-");
            Page<ChatMessage> chatMessagePage = page(pageQuery, queryWrapper);
            ObjectMapper objectMapper = new ObjectMapper();

            // 用于收集所有 uuid 进行批量查询
            Map<Long, List<String>> messageUuidMap = new HashMap<>();
            List<String> allUuids = new ArrayList<>();

            // 转为VO
            List<ChatMessageVO> chatMessageVOList = chatMessagePage.getRecords().stream()
                    .map(msg -> {
                        ChatMessageVO  chatMessageVO = ChatMessageVO.builder()
                                .id(msg.getId())
                                .createTime(msg.getCreateTime())
                                .messageType(msg.getMessageType())
                                .sessionId(msg.getSessionId())
                                .roleType(msg.getRoleType())
                                .content(msg.getContent())
                                .build();
                        // 如果消息类型是json格式的需要进行转换
                        if ("json".equalsIgnoreCase(msg.getMessageType())) {
                            try {
                                AiBillRecord aiBillRecord = objectMapper.readValue(msg.getContent(),AiBillRecord.class);
                                List<BillRecord> billRecordList = aiBillRecord.getBillRecordList();
                                if (billRecordList != null && !billRecordList.isEmpty()) {
                                    // 获取消息下的所有uuid
                                    List<String> uuids = billRecordList.stream()
                                            .map(BillRecord::getUuid)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());
                                    if (!uuids.isEmpty()) {
                                        messageUuidMap.put(msg.getId(), uuids);
                                        allUuids.addAll(uuids);
                                    }

                                }

                            } catch (JsonProcessingException e) {
                                throw new ChatException(MessageConstant.CHAT_MESSAGE_BILL_QUERY);
                            }
                        }
                        return chatMessageVO;
                    }).toList();
            // 查询所有uuid对应的Bill
            Map<String, Bill> billMap;
            if (!allUuids.isEmpty()) {
                LambdaQueryWrapper<Bill> billQuery = new LambdaQueryWrapper<>();
                billQuery.in(Bill::getUuid, allUuids);
                List<Bill> billList = billMapper.selectList(billQuery);
                billMap = billList.stream()
                        .collect(Collectors.toMap(Bill::getUuid, Function.identity()));
            } else {
                billMap = new HashMap<>();
            }
            // 将 bill 回填到 VO
            chatMessageVOList.forEach(vo -> {
                List<String> uuids = messageUuidMap.get(vo.getId());
                if (uuids != null) {
                    List<Bill> bills = uuids.stream()
                            .map(billMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    vo.setBillList(bills);
                    vo.setContent(null);
                }

            });
            // 进行消息去重
            List<ChatMessageVO> newChatMessageVOList = deduplicateMessages(chatMessageVOList, 10);
            // 构造分页结果
            Page<ChatMessageVO> chatMessageVOPage = new Page<>();
            chatMessageVOPage.setCurrent(chatMessagePage.getCurrent());
            chatMessageVOPage.setSize(newChatMessageVOList.size());
            chatMessageVOPage.setTotal(chatMessagePage.getTotal());
            chatMessageVOPage.setRecords(newChatMessageVOList);
            return chatMessageVOPage;
        } catch (Exception e) {
            throw new ChatException(MessageConstant.CHAT_MESSAGE_QUERY + e.getMessage());
        }
    }

    /**
     * 根据id删除
     * @param id
     */
    @Override
    public void deleteById(String id) {
        try {
            LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ChatMessage::getId, id).set(ChatMessage::getIsDeleted, 1);
            this.update(updateWrapper);
        }catch (Exception e){
            throw new ChatException(MessageConstant.CHAT_DELETE_ERR + e.getMessage());
        }
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        try {
            List<ChatMessage> chatMessages = this.listByIds(ids);
            // 构建要更新为 is_deleted=1 的实体列表
            List<ChatMessage> entities = chatMessages.stream()
                    .map(chatMessage -> new ChatMessage()
                            .setId(chatMessage.getId())
                            .setIsDeleted(1))
                    .collect(Collectors.toList());
            // 批量逻辑删除（使用 updateBatchById 执行单条 update 多条数据）
            this.updateBatchById(entities);
        }catch (Exception e){
            throw new ChatException(MessageConstant.CHAT_DELETE_ERR + e.getMessage());
        }
    }

    /**
     * 删除所有
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll() {
        try {
            Long userId = BaseContext.getCurrentId();
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.likeRight(ChatMessage::getSessionId, userId + "-");
            List<ChatMessage> chatMessages = this.list(queryWrapper);
            // 构建要更新为 is_deleted=1 的实体列表
            List<ChatMessage> entities = chatMessages.stream()
                    .map(chatMessage -> new ChatMessage()
                            .setId(chatMessage.getId())
                            .setIsDeleted(1))
                    .collect(Collectors.toList());
            // 批量逻辑删除（使用 updateBatchById 执行单条 update 多条数据）
            this.updateBatchById(entities);
        }catch (Exception e){
            throw new ChatException(MessageConstant.CHAT_DELETE_ERR + e.getMessage());
        }
    }

    /**
     * 流式输出订单
     * @param chatMessageDTO
     * @return
     */
    @Override
    public SseEmitter doBillWithSync(ChatMessageDTO chatMessageDTO) {
        SseEmitter emitter = new SseEmitter(36000L); // 无限超时
        Long userId = BaseContext.getCurrentId();
        CompletableFuture.runAsync(() -> {
            try {
                Long agentId = chatMessageDTO.getAgentId();
                String chatId = userId + "-" + agentId;
                chatMessageDTO.setChatId(chatId);
                chatMessageDTO.setUserId(userId);
                Agent agent = agentMapper.selectById(chatMessageDTO.getAgentId());
                chatMessageDTO.setPrompt(agent.getPrompt());
                // AI 生成账单
                AiBillRecord aiBillRecord = qianWenAiClient.doChatWithBill(chatMessageDTO);
                List<BillRecord> billRecordList = aiBillRecord.getBillRecordList();
                if (billRecordList == null || billRecordList.isEmpty()) {
                    emitter.send(SseEmitter.event().name("result").data("未识别出账单信息"));
                    emitter.complete();
                    return;
                }
                List<BillJson> billList = new ArrayList<>();
                for (BillRecord billRecord : billRecordList) {
                    if (billRecord == null) continue;
                    Bill bill = new Bill()
                            .setUserId(userId)
                            .setBillTime(LocalDateTime.now())
                            .setCreateTime(LocalDateTime.now())
                            .setUpdateTime(LocalDateTime.now())
                            .setCreateUser("USER"+userId)
                            .setUpdateUser("USER"+userId)
                            .setAmount(billRecord.getAmount())
                            .setRemark(billRecord.getRemark())
                            .setUuid(billRecord.getUuid())
                            .setIsAutoGenerated(1)
                            .setType(billRecord.getType());
                    // 分类逻辑
                    LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper
                            .eq(Category::getIsDeleted, 0)
                            .eq(Category::getCategory, billRecord.getCategory())
                            .and(wrapper -> wrapper.eq(Category::getIsSystem, 1)
                                    .or()
                                    .eq(Category::getUserId, userId));
                    Category category = categoryMapper.selectOne(queryWrapper);
                    if (category != null) {
                        bill.setCategoryId(category.getId());
                    } else {
                        LambdaQueryWrapper<Category> queryCategory = new LambdaQueryWrapper<>();
                        queryCategory.eq(Category::getIsDeleted, 0)
                                .eq(Category::getType, billRecord.getType())
                                .eq(Category::getStatus, 1)
                                .eq(Category::getCategory, "其他");
                        bill.setCategoryId(categoryMapper.selectOne(queryCategory).getId());
                    }
                    Bill saved = billService.saveAutoBill(bill);
                    BillJson billJson = BeanUtil.copyProperties(saved, BillJson.class);
                    billJson.setBillTime(billJson.getBillTime().replace("T", " "));
                    billJson.setCreateTime(billJson.getCreateTime().replace("T", " "));
                    billJson.setUpdateTime(billJson.getUpdateTime().replace("T", " "));
                    billList.add(billJson);
                }
                // 推送 JSON 账单数据
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(billList);
                emitter.send(json);
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE账单处理失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("账单生成失败"));
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 做消息去重
     * @param chatMessageVOList
     * @return
     */
    private List<ChatMessageVO> deduplicateMessages(List<ChatMessageVO> chatMessageVOList, int fuzzySeconds) {
        try {
            List<ChatMessageVO> result = new ArrayList<>();
            Map<String, LocalDateTime> lastUserMessageTimeMap = new HashMap<>();
            List<ChatMessageVO> modifiableList = new ArrayList<>(chatMessageVOList);
            Collections.reverse(modifiableList);
            for (ChatMessageVO msg : modifiableList) {
                if ("user".equalsIgnoreCase(msg.getRoleType())) {
                    String content = msg.getContent();
                    LocalDateTime currentTime = msg.getCreateTime();

                    if (!lastUserMessageTimeMap.containsKey(content)) {
                        // 首次出现，保留并记录时间
                        lastUserMessageTimeMap.put(content, currentTime);
                        result.add(msg);
                    } else {
                        LocalDateTime lastTime = lastUserMessageTimeMap.get(content);
                        Duration diff = Duration.between(lastTime, currentTime);
                        if (Math.abs(diff.getSeconds()) > fuzzySeconds) {
                            // 时间间隔足够大，不重复，保留并更新时间
                            lastUserMessageTimeMap.put(content, currentTime);
                            result.add(msg);
                        }
                        // 时间间隔很近，认为重复，什么都不做，跳过
                    }
                } else if ("assistant".equals(msg.getRoleType())) {
                    String type = msg.getMessageType();
                    // 判断json是否为空
                    if (type == null) {
                        continue;
                    }
                    result.add(msg);
                }
            }
            Collections.reverse(result);
            return result;
        } catch (Exception e) {
            throw new ChatException(e.getMessage());
        }

    }

}
