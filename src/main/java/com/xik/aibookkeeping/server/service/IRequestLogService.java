package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.dto.RequestLogPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.RequestLog;

import java.util.List;


/**
 * <p>
 * 系统日志表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface IRequestLogService extends IService<RequestLog> {

    Page<RequestLog> pageRequestLog(RequestLogPageQueryDTO requestLogPageQueryDTO);

    RequestLog getByRequestLogId(String id);

    void deleteRequestLogById(String id);

    void deleteRequestLogByIds(List<Long> ids);
}
