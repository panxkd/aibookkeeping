package com.xik.aibookkeeping.server.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.pojo.dto.BillAmountQueryDTO;
import com.xik.aibookkeeping.pojo.dto.BillPageQueryDTO;
import com.xik.aibookkeeping.pojo.dto.BillStructureQueryDTO;
import com.xik.aibookkeeping.pojo.dto.BillUserPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.vo.*;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 账单表 Mapper 接口
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface BillMapper extends BaseMapper<Bill> {


    Page<BillVO> pageQuery(Page<BillVO> page, BillPageQueryDTO billPageQueryDTO);

    BillVO queryById(Long id);

    BillUserVO getUserBillById(Long id);

    Page<BillUserVO> pageUserQuery(Page<BillUserVO> page,  BillUserPageQueryDTO billUserPageQueryDTO);


    @Select("SELECT SUM(amount) FROM bill " +
            "WHERE user_id = #{userId} AND type = #{type} " +
            "AND is_deleted = 0 " +
            "AND create_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal sumAmountByDateRange(@Param("userId") Long userId,
                                    @Param("type") String type,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    BillAmountVO getBillAmount(BillAmountQueryDTO billAmountQueryDTO);

    List<BillStructureVO> getBillStructure(BillStructureQueryDTO billStructureQueryDTO);

    @Select("SELECT COUNT(DISTINCT DATE(create_time)) FROM bill WHERE user_id = #{userId} AND is_deleted = 0")
    long countDistinctBillDays(@Param("userId") Long userId);
}
