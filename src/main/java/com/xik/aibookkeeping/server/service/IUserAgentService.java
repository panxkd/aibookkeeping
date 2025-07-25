package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.entity.UserAgent;

import java.util.List;

/**
 * <p>
 * 用户-智能体关联表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface IUserAgentService extends IService<UserAgent> {

    void addUserAgent(UserAgent userAgent);

    void updateAgent(UserAgent userAgent);

    void deleteById(Long id);

    List<UserAgent> getUserAgentByUserId();
}
