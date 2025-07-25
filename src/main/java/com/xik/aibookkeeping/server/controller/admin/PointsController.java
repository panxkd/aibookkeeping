package com.xik.aibookkeeping.server.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.PointsPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Points;
import com.xik.aibookkeeping.pojo.vo.PointsVO;
import com.xik.aibookkeeping.server.service.IPointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 积分表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminPointsController")
@RequestMapping("/admin/points")
@Slf4j
public class PointsController {

    @Resource
    private IPointsService pointsService;

    @GetMapping("/page")
    public Result<Page<PointsVO>> page(PointsPageQueryDTO pointsPageQueryDTO) {
        log.info("分页查询用户积分");
        Page<PointsVO> page = pointsService.pagePoints(pointsPageQueryDTO);
        return Result.success(page);
    }
}
