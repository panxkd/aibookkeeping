package com.xik.aibookkeeping.server.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.constant.BillConstant;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.constant.PointsLogConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.exception.BillException;
import com.xik.aibookkeeping.common.exception.CategoryException;
import com.xik.aibookkeeping.pojo.dto.BillAmountQueryDTO;
import com.xik.aibookkeeping.pojo.dto.BillPageQueryDTO;
import com.xik.aibookkeeping.pojo.dto.BillStructureQueryDTO;
import com.xik.aibookkeeping.pojo.dto.BillUserPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.vo.BillAmountVO;
import com.xik.aibookkeeping.pojo.vo.BillStructureVO;
import com.xik.aibookkeeping.pojo.vo.BillUserVO;
import com.xik.aibookkeeping.pojo.vo.BillVO;
import com.xik.aibookkeeping.server.rabbitmq.producer.BillStatProducer;
import com.xik.aibookkeeping.server.mapper.BillMapper;
import com.xik.aibookkeeping.server.rabbitmq.producer.PointsProducer;
import com.xik.aibookkeeping.server.service.IBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 账单表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
@Slf4j
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements IBillService {

    @Resource
    private BillMapper billMapper;

    @Resource
    private BillStatProducer billStatProducer;

    @Resource
    private PointsProducer pointsProducer;

    /**
     * 分页查询订单
     * @param billPageQueryDTO
     * @return
     */
    @Override
    public Page<BillVO> pageBill(BillPageQueryDTO billPageQueryDTO) {
        try {
            Page<BillVO> page = new Page<>(billPageQueryDTO.getPage(), billPageQueryDTO.getPageSize());
            return billMapper.pageQuery(page, billPageQueryDTO);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_QUERY);
        }
    }

    /**
     * 删除单条记录
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByBillId(Long id) {
        try {
            LambdaQueryWrapper<Bill> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Bill::getId, id);
            Bill bill = getOne(queryWrapper);
            LambdaUpdateWrapper<Bill> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(Bill::getId, id).set(Bill::getIsDeleted, 1);
            billMapper.update(null, lambdaUpdateWrapper);
            // 只有收入或支出类型才需要统计
            if (BillConstant.REVENUE.equals(bill.getType()) || BillConstant.EXPENSE.equals(bill.getType())) {
                // 发送统计更新消息  删除
                boolean isDelete = true;
                billStatProducer.sendStatUpdateMessage(bill.getId(),isDelete);
            }
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_DELETE_ERR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByBillIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new CategoryException(MessageConstant.QUERY_NOT_NULL);
        }
        try {
            // 查询这些账单的原始信息（包括 type 字段）
            List<Bill> bills = this.listByIds(ids);
            // 构建要更新为 is_deleted=1 的实体列表
            List<Bill> entities = bills.stream()
                    .map(bill -> new Bill()
                            .setId(bill.getId())
                            .setIsDeleted(1))
                    .collect(Collectors.toList());
            // 批量逻辑删除（使用 updateBatchById 执行单条 update 多条数据）
            this.updateBatchById(entities);
            // 筛选出需要更新统计的账单（收入和支出类型）
            List<Bill> needUpdateStatBills = bills.stream()
                    .filter(bill -> BillConstant.REVENUE.equals(bill.getType()) || BillConstant.EXPENSE.equals(bill.getType()))
                    .toList();
            // 发送统计更新消息
            for (Bill bill : needUpdateStatBills) {
                billStatProducer.sendStatUpdateMessage(bill.getId(), true); // true 表示删除
            }
        } catch (Exception e) {
            throw new CategoryException(MessageConstant.BILL_DELETE_ERR + e.getMessage());
        }
    }

    /**
     * 根据id查询账单详情信息
     * @param id
     * @return
     */
    @Override
    public BillVO getBillById(Long id) {
        try {
            return billMapper.queryById(id);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_QUERY);
        }
    }

    /**
     * 用户根据查询自己的账单
     * @param id
     * @return
     */
    @Override
    public BillUserVO getUserBillById(Long id) {
        try {
            return billMapper.getUserBillById(id);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_QUERY);
        }

    }

    /**
     * 用户添加账单
     * @param bill
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(OperationType.USER_INSERT)
    public void saveBill(Bill bill) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            bill.setUserId(currentUserId);
            this.save(bill);
             // 异步保存到数据统计表
            // 只有收入或支出类型才需要统计
            if (BillConstant.REVENUE.equals(bill.getType()) || BillConstant.EXPENSE.equals(bill.getType())) {
                // 发送统计初始化消息
//                billStatProducer.sendStatInitMessage(bill.getUserId(), bill.getBillTime());

                // 发送统计更新消息  添加
                boolean isDelete = false;
                billStatProducer.sendStatUpdateMessage(bill.getId(),isDelete);
            }
            // 如果当天第一次使用将自动获取积分
            pointsProducer.updatePoints(currentUserId, PointsLogConstant.POINTS_UPDATE,PointsLogConstant.EARN,
                    PointsLogConstant.POINTS_SIGNIN_SOURCE, PointsLogConstant.POINTS_UPDATE_REMARK);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_SAVE_ERR + e.getMessage());
        }
    }

    /**
     * AI自动保存
     * @param bill
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @OperationLog(OperationType.USER_INSERT)
    public Bill saveAutoBill(Bill bill) {
        try {
            this.save(bill);
            // 异步保存到数据统计表
            // 只有收入或支出类型才需要统计
            if (BillConstant.REVENUE.equals(bill.getType()) || BillConstant.EXPENSE.equals(bill.getType())) {
                // 发送统计初始化消息
//                billStatProducer.sendStatInitMessage(bill.getUserId(), LocalDateTime.now());

                // 发送统计更新消息  添加
                boolean isDelete = false;
                billStatProducer.sendStatUpdateMessage(bill.getId(),isDelete);
            }
            // 如果当天第一次使用将自动获取积分
            pointsProducer.updatePoints(bill.getUserId(), PointsLogConstant.POINTS_UPDATE,PointsLogConstant.EARN,
                    PointsLogConstant.POINTS_SIGNIN_SOURCE, PointsLogConstant.POINTS_UPDATE_REMARK);
            return bill;
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_SAVE_ERR + e.getMessage());
        }
    }

    /**
     * 用户更新账单
     * @param bill
     */
    @Override
    public void updateBill(Bill bill) {
        try {
            this.updateById(bill);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_UPDATE_ERR + e.getMessage());
        }
    }

    /**
     * 用户分页查询账单
     * @param billUserPageQueryDTO
     * @return
     */
    @Override
    public Page<BillUserVO> pageUserBill(BillUserPageQueryDTO billUserPageQueryDTO) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            billUserPageQueryDTO.setUserId(currentUserId);
            Page<BillUserVO> page = new Page<>(billUserPageQueryDTO.getPage(), billUserPageQueryDTO.getPageSize());
            return billMapper.pageUserQuery(page, billUserPageQueryDTO);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_QUERY + e.getMessage());
        }
    }

    /**
     * 根据用户id查询账单总数量
     * @param
     * @return
     */
    @Override
    public long getNumberByUserId() {
        try {
            Long userId = BaseContext.getCurrentId();
            LambdaQueryWrapper<Bill> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Bill::getUserId, userId).eq(Bill::getIsDeleted, 0);
            return this.count(lambdaQueryWrapper);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_QUERY + e.getMessage());
        }
    }

    /**
     * 查询当前用户记账的总天数
     * @return
     */
    @Override
    public long getDaysByUserId() {
        try {
            Long userId = BaseContext.getCurrentId();
            return billMapper.countDistinctBillDays(userId);
        }  catch (Exception e) {
            throw new BillException(MessageConstant.BILL_QUERY + e.getMessage());
        }
    }

    /**
     * 根据 时间/日期范围查询收支情况
     * @param billAmountQueryDTO
     * @return
     */
    @Override
    public BillAmountVO getBillAmount(BillAmountQueryDTO billAmountQueryDTO) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            billAmountQueryDTO.setUserId(currentUserId);
            return billMapper.getBillAmount(billAmountQueryDTO);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_QUERY + e.getMessage());
        }
    }

    /**
     * 查询结构组成
     * @param billStructureQueryDTO
     * @return
     */
    @Override
    public List<BillStructureVO> getBillStructure(BillStructureQueryDTO billStructureQueryDTO) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            billStructureQueryDTO.setUserId(currentUserId);
            return billMapper.getBillStructure(billStructureQueryDTO);
        } catch (Exception e) {
            throw new BillException(MessageConstant.BILL_QUERY + e.getMessage());
        }
    }



}
