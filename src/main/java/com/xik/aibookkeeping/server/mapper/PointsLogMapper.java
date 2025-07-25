package com.xik.aibookkeeping.server.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.pojo.dto.PointsLogPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.PointsLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xik.aibookkeeping.pojo.vo.PointsLogVO;

/**
 * <p>
 * 积分表 Mapper 接口
 * </p>
 *
 * @author panxikai
 * @since 2025-06-30
 */
public interface PointsLogMapper extends BaseMapper<PointsLog> {

    Page<PointsLogVO> pagePointsLog(Page<PointsLogVO> page, PointsLogPageQueryDTO pointsLogPageQueryDTO);

    PointsLogVO getPointsLogVO(Long id);
}
