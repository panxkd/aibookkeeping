package com.xik.aibookkeeping.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.pojo.dto.ChatMessageDTO;
import com.xik.aibookkeeping.pojo.dto.ChatMessagePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.vo.ChatMessageVO;
import com.xik.aibookkeeping.pojo.vo.ResponseMessageVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * <p>
 * AI消息记录表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-07-02
 */
public interface IChatMessageService extends IService<ChatMessage> {

    SseEmitter getResponse(ChatMessageDTO chatMessageDTO);

    List<Bill> getBill(ChatMessageDTO chatMessageDTO);

    ResponseMessageVO doChat(ChatMessageDTO chatMessageDTO);

    Page<ChatMessageVO> pageChatMessage(ChatMessagePageQueryDTO chatMessagePageQueryDTO);

    void deleteById(String id);

    void deleteByIds(List<Long> ids);

    void deleteAll();

    SseEmitter doBillWithSync(ChatMessageDTO chatMessageDTO);
}
