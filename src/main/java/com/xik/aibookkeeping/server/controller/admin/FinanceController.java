package com.xik.aibookkeeping.server.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.FinancePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Finance;
import com.xik.aibookkeeping.server.service.IFinanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 理财计划表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminFinanceController")
@RequestMapping("/admin/finance")
@Slf4j
public class FinanceController {

    @Resource
    private IFinanceService financeService;

    @GetMapping("/{id}")
    public Result<Finance> getFinanceById(@PathVariable Long id){
        log.info("根据id查询理财计划表：{}", id);
        Finance finance = financeService.getFinanceById(id);
        return Result.success(finance);
    }

    @GetMapping("/page")
    public Result<Page<Finance>> pageFinance(FinancePageQueryDTO financePageQueryDTO) {
        log.info("分页查询理财计划：{}", financePageQueryDTO);
        Page<Finance> page = financeService.pageFinance(financePageQueryDTO);
        return Result.success(page);
    }
}
