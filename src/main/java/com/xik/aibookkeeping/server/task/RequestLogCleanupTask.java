package com.xik.aibookkeeping.server.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xik.aibookkeeping.pojo.entity.RequestLog;
import com.xik.aibookkeeping.server.mapper.RequestLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class RequestLogCleanupTask {

    @Resource
    private RequestLogMapper requestLogMapper;

    /**
     * 定期清理日志，删除 isDeleted=1 的数据和创建时间超过10天的数据
     * cron表达式：每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldAndDeletedLogs() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(10);

        int deletedCount = requestLogMapper.delete(new LambdaQueryWrapper<RequestLog>()
            .eq(RequestLog::getIsDeleted, 1)
            .or()
            .lt(RequestLog::getCreateTime, thresholdDate)
        );

        log.info("清理请求日志，删除记录数：{}", deletedCount);
    }
}
