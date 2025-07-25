package com.xik.aibookkeeping.server.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.exception.PointsLogException;
import com.xik.aibookkeeping.pojo.dto.PointsLogPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.PointsLog;
import com.xik.aibookkeeping.pojo.vo.PointsLogVO;
import com.xik.aibookkeeping.server.mapper.PointsLogMapper;
import com.xik.aibookkeeping.server.service.IPointsLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 积分表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-30
 */
@Service
public class PointsLogServiceImpl extends ServiceImpl<PointsLogMapper, PointsLog> implements IPointsLogService {

    @Resource
    private PointsLogMapper pointsLogMapper;

    @Override
    public Page<PointsLogVO> pagePointsLog(PointsLogPageQueryDTO pointsLogPageQueryDTO) {
        try {

            Page<PointsLogVO> page = new Page<>(pointsLogPageQueryDTO.getPage(), pointsLogPageQueryDTO.getPageSize());
            return pointsLogMapper.pagePointsLog(page, pointsLogPageQueryDTO);
        } catch (Exception e) {
            throw new PointsLogException(MessageConstant.POINTS_LOG_QUERY_ERR);
        }
    }

    /**
     * 根据id获取积分日志表
     * @param id
     * @return
     */
    @Override
    public PointsLogVO getPointsLogById(Long id) {
        try {
            return pointsLogMapper.getPointsLogVO(id);
        } catch (Exception e) {
            throw new PointsLogException(MessageConstant.POINTS_LOG_QUERY_ERR);
        }
    }
}
