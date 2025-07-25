package com.xik.aibookkeeping.server.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.pojo.dto.PointsPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Points;
import com.xik.aibookkeeping.pojo.vo.PointsVO;

/**
 * <p>
 * 积分表 Mapper 接口
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface PointsMapper extends BaseMapper<Points> {

    Page<PointsVO> pagePoints(Page<PointsVO> page, PointsPageQueryDTO pointsPageQueryDTO);
}
