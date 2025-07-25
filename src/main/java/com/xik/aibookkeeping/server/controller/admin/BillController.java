package com.xik.aibookkeeping.server.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.BillAmountQueryDTO;
import com.xik.aibookkeeping.pojo.dto.BillPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.vo.BillAmountVO;
import com.xik.aibookkeeping.pojo.vo.BillVO;
import com.xik.aibookkeeping.server.service.IBillService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 账单表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminBillController")
@RequestMapping("/admin/bill")
@Slf4j
public class BillController {

    @Resource
    private IBillService billService;

    @GetMapping("/page")
    public Result<Page<BillVO>> page(BillPageQueryDTO billPageQueryDTO) {
        log.info("分页查询账单：{}", billPageQueryDTO);
        Page<BillVO> page = billService.pageBill(billPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 根据id查询详细账单
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<BillVO> getBillById(@PathVariable Long id) {
        log.info("查询账单的id:{}", id);
        BillVO billVO = billService.getBillById(id);
        return Result.success(billVO);
    }

    /**
     * 删除单条账单
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除账单：{}", id);
        billService.removeByBillId(id);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量删除账单：{}", ids);
        billService.removeByBillIds(ids);
        return Result.success();
    }
}
