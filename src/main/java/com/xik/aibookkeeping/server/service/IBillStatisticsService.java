package com.xik.aibookkeeping.server.service;

import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.entity.BillStatistics;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-29
 */
public interface IBillStatisticsService extends IService<BillStatistics> {


    List<BillStatistics> getDailyStatistics(LocalDate startDate, LocalDate endDate);

    List<BillStatistics> getMonthlyStatistics(YearMonth startMonth, YearMonth endMonth);

    List<BillStatistics> getYearlyStatistics(Year startYear, Year endYear);

    BillStatistics getDayStatistics(LocalDate day);
}
