package com.xik.aibookkeeping.server.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.RequestLogPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.RequestLog;
import com.xik.aibookkeeping.server.service.IRequestLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 系统日志表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminRequestController")
@RequestMapping("/admin/request-log")
@Slf4j
public class RequestLogController {

    @Resource
    private IRequestLogService requestLogService;

    @GetMapping("/page")
    public Result<Page<RequestLog>> page(RequestLogPageQueryDTO requestLogPageQueryDTO) {
        log.info("分页查询系统日志：{}", requestLogPageQueryDTO);
        Page<RequestLog> page = requestLogService.pageRequestLog(requestLogPageQueryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<RequestLog> getRequestLogById(@PathVariable String id) {
        log.info("日志查询id:{}", id);
        RequestLog requestLog = requestLogService.getByRequestLogId(id);
        return Result.success(requestLog);
    }

    @DeleteMapping("/{id}")
    public Result deleteRequestLogById(@PathVariable String id) {
        log.info("删除的日志id:{}", id);
        requestLogService.deleteRequestLogById(id);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteRequestLogByIds(@RequestBody List<Long> ids) {
        log.info("批量删除日志 ids:{}", ids);
        requestLogService.deleteRequestLogByIds(ids);
        return Result.success();
    }

}
