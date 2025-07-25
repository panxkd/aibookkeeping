package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.dto.FinancePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Finance;

import java.util.List;

/**
 * <p>
 * 理财计划表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface IFinanceService extends IService<Finance> {

    void addFinance(Finance finance);

    Finance getFinanceById(Long id);

    Page<Finance> pageFinance(FinancePageQueryDTO financePageQueryDTO);

    void deleteFinanceById(Long id);

    void deleteFianceByIds(List<Long> ids);

    void updateFinance(Finance finance);
}
