package com.xik.aibookkeeping.server.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.exception.CategoryException;
import com.xik.aibookkeeping.common.exception.LogException;
import com.xik.aibookkeeping.pojo.dto.RequestLogPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Finance;
import com.xik.aibookkeeping.pojo.entity.RequestLog;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xik.aibookkeeping.server.mapper.RequestLogMapper;
import com.xik.aibookkeeping.server.service.IRequestLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统日志表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class RequestLogServiceImpl extends ServiceImpl<RequestLogMapper, RequestLog> implements IRequestLogService {


    @Override
    public Page<RequestLog> pageRequestLog(RequestLogPageQueryDTO requestLogPageQueryDTO) {
        try {
            Page<RequestLog> page = new Page<>(requestLogPageQueryDTO.getPage(), requestLogPageQueryDTO.getPageSize());
            LambdaQueryWrapper<RequestLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(requestLogPageQueryDTO.getRequestId() != null,RequestLog::getRequestId,requestLogPageQueryDTO.getRequestId())
                    .eq(requestLogPageQueryDTO.getLogType() != null,RequestLog::getLogType,requestLogPageQueryDTO.getLogType())
                    .eq(requestLogPageQueryDTO.getModule() != null,RequestLog::getModule,requestLogPageQueryDTO.getModule())
                    .eq(requestLogPageQueryDTO.getUserId() != null,RequestLog::getUserId,requestLogPageQueryDTO.getUserId())
                    .eq(requestLogPageQueryDTO.getUsername() != null,RequestLog::getUsername,requestLogPageQueryDTO.getUsername())
                    .eq(requestLogPageQueryDTO.getStatus()!= null,RequestLog::getStatus,requestLogPageQueryDTO.getStatus())
                    .eq(requestLogPageQueryDTO.getRequestMethod() != null,RequestLog::getRequestMethod,requestLogPageQueryDTO.getRequestMethod())
                    .eq(requestLogPageQueryDTO.getRequestUrl() != null,RequestLog::getRequestUrl,requestLogPageQueryDTO.getRequestUrl())
                    .orderByDesc(RequestLog::getCreateTime);
            return this.page(page, queryWrapper);
        } catch (Exception e) {
            throw new LogException(MessageConstant.LOG_QUERY + e.getMessage());
        }
    }

    @Override
    public RequestLog getByRequestLogId(String id) {
        try {
            return this.getById(id);
        }catch (Exception e){
            throw new LogException(MessageConstant.LOG_QUERY + e.getMessage());
        }
    }

    @Override
    public void deleteRequestLogById(String id) {
        try {
            LambdaUpdateWrapper<RequestLog> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(RequestLog::getRequestId, id).set(RequestLog::getIsDeleted,1);
            this.update(updateWrapper);
        } catch (Exception e) {
            throw new LogException(MessageConstant.LOG_DELETE + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRequestLogByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new CategoryException(MessageConstant.QUERY_NOT_NULL);
        }
        try {
            // 分批次处理（避免IN条件过长）
            List<RequestLog> entities = ids.stream()
                    .map(id -> new RequestLog().setId(id).setIsDeleted(1))
                    .collect(Collectors.toList());
            this.updateBatchById(entities); // 单条SQL批量执行
        } catch (Exception e) {
            throw new LogException(MessageConstant.LOG_DELETE + e.getMessage());
        }
    }
}
