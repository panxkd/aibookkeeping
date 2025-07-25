package com.xik.aibookkeeping.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.exception.BillStatisticsException;
import com.xik.aibookkeeping.pojo.entity.BillStatistics;
import com.xik.aibookkeeping.server.mapper.BillStatisticsMapper;
import com.xik.aibookkeeping.server.service.IBillStatisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-29
 */
@Service
public class BillStatisticsServiceImpl extends ServiceImpl<BillStatisticsMapper, BillStatistics> implements IBillStatisticsService {


    @Override
    public List<BillStatistics> getDailyStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            Long userId = BaseContext.getCurrentId();
            LambdaQueryWrapper<BillStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(BillStatistics::getUserId, userId)
                    .eq(BillStatistics::getStatType, 1)
                    .between(BillStatistics::getStatDate, startDate.format(DateTimeFormatter.ISO_DATE), endDate.format(DateTimeFormatter.ISO_DATE))
                    .orderByAsc(BillStatistics::getStatDate);
            return baseMapper.selectList(queryWrapper);
        } catch (Exception e) {
            throw new BillStatisticsException(MessageConstant.BILL_STATUS_QUERY_ERR + e.getMessage());
        }


    }

    @Override
    public List<BillStatistics> getMonthlyStatistics(YearMonth startMonth, YearMonth endMonth) {
        try {
            Long userId = BaseContext.getCurrentId();
            LambdaQueryWrapper<BillStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(BillStatistics::getUserId, userId)
                    .eq(BillStatistics::getStatType, 2)
                    .between(BillStatistics::getStatDate, startMonth.toString(), endMonth.toString())
                    .orderByAsc(BillStatistics::getStatDate);
            return baseMapper.selectList(queryWrapper);
        } catch (Exception e) {
            throw new BillStatisticsException(MessageConstant.BILL_STATUS_QUERY_ERR + e.getMessage());
        }

    }

    @Override
    public List<BillStatistics> getYearlyStatistics(Year startYear, Year endYear) {
        try {
            Long userId = BaseContext.getCurrentId();
            LambdaQueryWrapper<BillStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(BillStatistics::getUserId, userId)
                    .eq(BillStatistics::getStatType, 3)
                    .between(BillStatistics::getStatDate, startYear.toString(), endYear.toString())
                    .orderByAsc(BillStatistics::getStatDate);
            return baseMapper.selectList(queryWrapper);
        } catch (Exception e) {
            throw new BillStatisticsException(MessageConstant.BILL_STATUS_QUERY_ERR + e.getMessage());
        }
    }

    /**
     * 统计一天的收支情况
     * @param day
     * @return
     */
    @Override
    public BillStatistics getDayStatistics(LocalDate day) {
        try {
            Long userId = BaseContext.getCurrentId();
            LambdaQueryWrapper<BillStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(BillStatistics::getUserId, userId)
                    .eq(BillStatistics::getStatType, 1)
                    .eq(BillStatistics::getStatDate, day);
            return baseMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new BillStatisticsException(MessageConstant.BILL_STATUS_QUERY_ERR + e.getMessage());
        }
    }
}
