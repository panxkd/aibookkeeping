package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.dto.PointsPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Points;
import com.xik.aibookkeeping.pojo.vo.PointsVO;

/**
 * <p>
 * 积分表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface IPointsService extends IService<Points> {


    Integer getUserPoints(Long userId);

    Page<PointsVO> pagePoints(PointsPageQueryDTO pointsPageQueryDTO);
}
