package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.dto.*;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.vo.*;

import java.util.List;


/**
 * <p>
 * 账单表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface IBillService extends IService<Bill> {

    Page<BillVO> pageBill(BillPageQueryDTO billPageQueryDTO);

    void removeByBillId(Long id);

    void removeByBillIds(List<Long> ids);

    BillVO getBillById(Long id);

    BillUserVO getUserBillById(Long id);

    void saveBill(Bill bill);

    Bill saveAutoBill(Bill bill);

    void updateBill(Bill bill);

    Page<BillUserVO> pageUserBill(BillUserPageQueryDTO billUserPageQueryDTO);

    long getNumberByUserId();

    long getDaysByUserId();


    BillAmountVO getBillAmount(BillAmountQueryDTO billAmountQueryDTO);

    List<BillStructureVO> getBillStructure(BillStructureQueryDTO billStructureQueryDTO);
}
