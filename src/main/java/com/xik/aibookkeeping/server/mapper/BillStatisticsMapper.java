package com.xik.aibookkeeping.server.mapper;

import com.xik.aibookkeeping.pojo.entity.BillStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author panxikai
 * @since 2025-06-29
 */
public interface BillStatisticsMapper extends BaseMapper<BillStatistics> {


    @Update("INSERT INTO bill_statistics(user_id, stat_type, stat_date, revenue, expenditures) " +
            "VALUES(#{userId}, #{statType}, #{statDate}, #{revenue}, #{expenditures}) " +
            "ON DUPLICATE KEY UPDATE " +
            "revenue = revenue + VALUES(revenue), " +
            "expenditures = expenditures + VALUES(expenditures), " +
            "update_time = NOW()")
    int upsertStatistics(BillStatistics statistics);

    @Update("INSERT INTO bill_statistics(user_id, stat_type, stat_date, revenue, expenditures) " +
            "VALUES(#{userId}, #{statType}, #{statDate}, #{revenue}, #{expenditures}) " +
            "ON DUPLICATE KEY UPDATE " +
            "revenue = revenue - VALUES(revenue), " +
            "expenditures = expenditures - VALUES(expenditures), " +
            "update_time = NOW()")
    int deleteStatistics(BillStatistics statistics);
}
