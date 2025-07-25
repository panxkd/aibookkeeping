package com.xik.aibookkeeping.server.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.entity.BillStatistics;
import com.xik.aibookkeeping.server.mapper.BillStatisticsMapper;
import com.xik.aibookkeeping.server.service.IBillStatisticsService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-29
 */
@RestController
@RequestMapping("/user/bill-statistics")
@RequiredArgsConstructor
@Slf4j
public class BillStatisticsController {

    @Resource
    private IBillStatisticsService billStatisticsService;
    /**
     * 根据天统计收支
     */
    @GetMapping("/day")
    public Result<BillStatistics> getBillStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate day) {
        log.info("{}的收支情况", day.toString());
        BillStatistics billStatistics = billStatisticsService.getDayStatistics(day);
        return Result.success(billStatistics);
    }

    /**
     * 查询当天的收入支出
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/daily")
    public Result<List<BillStatistics>> getDailyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("日收支的范围：{}-{}", startDate, endDate);
        List<BillStatistics> stats = billStatisticsService.getDailyStatistics(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 查询月收支
     * @param startMonth
     * @param endMonth
     * @return
     */
    @GetMapping("/monthly")
    public Result<List<BillStatistics>> getMonthlyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth startMonth,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth endMonth) {
        log.info("月收支的范围：{}-{}", startMonth, endMonth);
        List<BillStatistics> stats = billStatisticsService.getMonthlyStatistics(startMonth, endMonth);


        return Result.success(stats);
    }

    /**
     * 查询年收支
     * @param startYear
     * @param endYear
     * @return
     */
    @GetMapping("/yearly")
    public Result<List<BillStatistics>> getYearlyStatistics(
            @RequestParam Year startYear,
            @RequestParam Year endYear) {
        log.info("年收支的范围：{}-{}", startYear, endYear);
        List<BillStatistics> stats = billStatisticsService.getYearlyStatistics(startYear, endYear);
        return Result.success(stats);
    }
}
