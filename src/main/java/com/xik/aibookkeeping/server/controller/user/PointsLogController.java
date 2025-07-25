package com.xik.aibookkeeping.server.controller.user;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.PointsLogPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.PointsLog;
import com.xik.aibookkeeping.pojo.vo.PointsLogVO;
import com.xik.aibookkeeping.server.service.IPointsLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 积分表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-30
 */
@RestController("UserPointsLogController")
@RequestMapping("/user/points-log")
@Slf4j
public class PointsLogController {

    @Resource
    private IPointsLogService pointsLogService;

    @GetMapping("/page")
    public Result<Page<PointsLogVO>> page(PointsLogPageQueryDTO pointsLogPageQueryDTO){
        log.info("查询的参数：{}", pointsLogPageQueryDTO);
        Long userId = BaseContext.getCurrentId();
        pointsLogPageQueryDTO.setUserId(userId);
        Page<PointsLogVO> page = pointsLogService.pagePointsLog(pointsLogPageQueryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<PointsLogVO> getPointsLog(@PathVariable Long id){
        log.info("获取积分日志详情：{}",id);
        PointsLogVO pointsLog = pointsLogService.getPointsLogById(id);
        return Result.success(pointsLog);
    }

}
