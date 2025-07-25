package com.xik.aibookkeeping.server.controller.user;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.FinanceDTO;
import com.xik.aibookkeeping.pojo.dto.FinancePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Finance;
import com.xik.aibookkeeping.server.service.IFinanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 理财计划表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("UserFinanceController")
@RequestMapping("/user/finance")
@Slf4j
public class FinanceController {

    @Resource
    private IFinanceService financeService;

    /**
     * 添加理财计划表
     * @param financeDTO
     * @return
     */
    @PostMapping
    public Result addFinance(@RequestBody FinanceDTO financeDTO){
        log.info("添加理财计划表");
        Finance finance = new Finance();
        BeanUtils.copyProperties(financeDTO,finance);
        financeService.addFinance(finance);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Finance> getFinanceById(@PathVariable Long id){
        log.info("根据id查询理财计划表：{}", id);
        Finance finance = financeService.getFinanceById(id);
        return Result.success(finance);
    }

    @GetMapping("/page")
    public Result<Page<Finance>> pageFinance(FinancePageQueryDTO financePageQueryDTO) {
        log.info("分页查询理财计划：{}", financePageQueryDTO);
        Long userId = BaseContext.getCurrentId();
        financePageQueryDTO.setUserId(userId);
        Page<Finance> page = financeService.pageFinance(financePageQueryDTO);
        return Result.success(page);
    }

    @DeleteMapping("/{id}")
    public Result deleteFinanceById(@PathVariable Long id){
        log.info("删除理财计划表{}", id);
        financeService.deleteFinanceById(id);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    public Result deleteFinanceByIds(@PathVariable List<Long> ids){
        log.info("批量删除理财计划表{}", ids);
        financeService.deleteFianceByIds(ids);
        return Result.success();
    }

    /**
     * 更新计划表
     * @param financeDTO
     * @return
     */
    @PutMapping
    public Result updateFinance(@RequestBody FinanceDTO financeDTO){
        log.info("修改理财计划表{}", financeDTO);
        Finance finance = Finance.builder().build();
        BeanUtils.copyProperties(financeDTO,finance);
        financeService.updateFinance(finance);
        return Result.success();
    }

    /**
     * 修改计划表的状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        log.info("修改的状态:{}，id:{}", status,id);
        Finance finance = Finance.builder()
                .status(status)
                .id(id)
                .build();
        financeService.updateFinance(finance);
        return Result.success();
    }

}
