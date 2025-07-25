package com.xik.aibookkeeping.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.pojo.dto.PointsLogPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.PointsLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.vo.PointsLogVO;

/**
 * <p>
 * 积分表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-30
 */
public interface IPointsLogService extends IService<PointsLog> {

    Page<PointsLogVO> pagePointsLog(PointsLogPageQueryDTO pointsLogPageQueryDTO);

    PointsLogVO getPointsLogById(Long id);
}
