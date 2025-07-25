package com.xik.aibookkeeping.server.service.impl;

import com.xik.aibookkeeping.pojo.entity.ChatSession;
import com.xik.aibookkeeping.server.mapper.ChatSessionMapper;
import com.xik.aibookkeeping.server.service.IChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AI会话主表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-07-02
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

}
