package com.xik.aibookkeeping.server.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.PointsLogConstant;
import com.xik.aibookkeeping.common.exception.PointsException;
import com.xik.aibookkeeping.pojo.dto.PointsPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Points;
import com.xik.aibookkeeping.pojo.entity.PointsLog;
import com.xik.aibookkeeping.pojo.entity.User;
import com.xik.aibookkeeping.pojo.vo.PointsVO;
import com.xik.aibookkeeping.server.mapper.PointsLogMapper;
import com.xik.aibookkeeping.server.mapper.PointsMapper;
import com.xik.aibookkeeping.server.mapper.UserMapper;
import com.xik.aibookkeeping.server.service.IPointsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 积分表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class PointsServiceImpl extends ServiceImpl<PointsMapper, Points> implements IPointsService {

    @Resource
    private PointsMapper pointsMapper;

    /**
     * 根据用户id获取用户积分
     * @param userId
     * @return
     */
    @Override
    public Integer getUserPoints(Long userId) {
        try {
            LambdaQueryWrapper<Points> queryWrapper = new LambdaQueryWrapper<Points>();
            queryWrapper.eq(Points::getUserId, userId);
            Points points = baseMapper.selectOne(queryWrapper);
            if (points == null) {
                // 用户积分未被初始化
                throw new PointsException(MessageConstant.USER_POINTS_INIT_ERR);
            }
            return points.getTotalPoints();
        }  catch (Exception e) {
            throw new PointsException(MessageConstant.POINTS_QUERY_ERR + e.getMessage());
        }
    }

    /**
     *
     * @param pointsPageQueryDTO
     * @return
     */
    @Override
    public Page<PointsVO> pagePoints(PointsPageQueryDTO pointsPageQueryDTO) {
        try {
            Page<PointsVO> page = new Page<>(pointsPageQueryDTO.getPage(), pointsPageQueryDTO.getPageSize());
            return pointsMapper.pagePoints(page, pointsPageQueryDTO);
        } catch (Exception e) {
            throw new PointsException(MessageConstant.POINTS_QUERY_ERR + e.getMessage());
        }
    }
}
