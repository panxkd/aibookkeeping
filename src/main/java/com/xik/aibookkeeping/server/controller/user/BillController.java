package com.xik.aibookkeeping.server.controller.user;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.*;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.vo.*;
import com.xik.aibookkeeping.server.service.IBillService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 账单表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("UserBillController")
@RequestMapping("/user/bill")
@Slf4j
public class BillController {

    @Resource
    private IBillService billService;

    /**
     * 分页查询账单 可根据时间 收支类型 账单分类 是否由ai生成查询
     * @param billUserPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<Page<BillUserVO>> page(BillUserPageQueryDTO billUserPageQueryDTO) {
        log.info("分页查询账单：{}", billUserPageQueryDTO);
        Page<BillUserVO> page = billService.pageUserBill(billUserPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 根据账单id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<BillUserVO> queryById(@PathVariable Long id) {
        log.info("查询的账单id:{}", id);
        BillUserVO billUserVO = billService.getUserBillById(id);
        return Result.success(billUserVO);
    }

    @PostMapping
    public Result addBill(@RequestBody BillDTO BillDTO) {
        log.info("用户添加账单{}", BillDTO);
        Bill bill = new Bill();
        BeanUtils.copyProperties(BillDTO, bill);
        billService.saveBill(bill);
        return Result.success();
    }

    @PutMapping
    public Result updateBill(@RequestBody BillDTO BillDTO) {
        log.info("修改账单：{}", BillDTO);
        Bill bill = new Bill();
        BeanUtils.copyProperties(BillDTO, bill);
        billService.updateBill(bill);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteBill(@PathVariable Long id) {
        log.info("删除账单id:{}", id);
        billService.removeByBillId(id);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteBills(@RequestBody List<Long> ids) {
        log.info("批量删除订单 ids:{}", ids);
        billService.removeByBillIds(ids);
        return Result.success();
    }

    /**
     * 根据用户id查询账单总数量
     */
    @GetMapping("/number")
    public Result<Long> getNumber() {
        log.info("查询当前用户账单总数量");
        long number = billService.getNumberByUserId();
        return Result.success(number);
    }

    @GetMapping("/days/number")
    public Result<Long> getBllDays() {
        log.info("查询当前用户记账的总天数");
        long number = billService.getDaysByUserId();
        return Result.success(number);
    }

    @GetMapping("/amount")
    public Result<BillAmountVO>  getBillAmount(BillAmountQueryDTO billAmountQueryDTO) {
        log.info("查询收支的情况{}", billAmountQueryDTO);
        BillAmountVO billAmountVO = billService.getBillAmount(billAmountQueryDTO);
        return Result.success(billAmountVO);
    }

    /**
     * 查询收支金额结构组成
     * @param billStructureQueryDTO
     * @return
     */
    @GetMapping("/structure")
    public Result<List<BillStructureVO>> getBillStructure(BillStructureQueryDTO billStructureQueryDTO) {
        log.info("查询结构组成：{}", billStructureQueryDTO);
        List<BillStructureVO> billStructureVOList = billService.getBillStructure(billStructureQueryDTO);
        return Result.success(billStructureVOList);
    }
}
