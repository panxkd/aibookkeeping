package com.xik.aibookkeeping.server.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.exception.CategoryException;
import com.xik.aibookkeeping.common.exception.FinanceException;
import com.xik.aibookkeeping.common.exception.base.BaseException;
import com.xik.aibookkeeping.pojo.dto.FinancePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Bill;
import com.xik.aibookkeeping.pojo.entity.Finance;
import com.xik.aibookkeeping.server.mapper.FinanceMapper;
import com.xik.aibookkeeping.server.service.IFinanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 理财计划表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class FinanceServiceImpl extends ServiceImpl<FinanceMapper, Finance> implements IFinanceService {

    private final FinanceMapper financeMapper;

    public FinanceServiceImpl(FinanceMapper financeMapper) {
        this.financeMapper = financeMapper;
    }

    /**
     * 添加理财计划表
     * @param finance
     */
    @Override
    @OperationLog(OperationType.USER_INSERT)
    public void addFinance(Finance finance) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            if (currentUserId == null) {
                throw new BaseException(MessageConstant.BASE_USER_NOT_NULL);
            }
            finance.setUserId(currentUserId);
            this.save(finance);
        } catch (Exception e){
            throw new FinanceException(MessageConstant.FINANCE_INSERT_ERR);
        }
    }

    @Override
    public Finance getFinanceById(Long id) {
        try {
            LambdaQueryWrapper<Finance> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Finance::getId, id).eq(Finance::getIsDeleted,0);
            return this.getOne(queryWrapper);
        } catch (Exception e){
            throw new BaseException(MessageConstant.FINANCE_QUERY_ERR);
        }
    }

    /**
     * 查询理财计划
     * @param dto
     * @return
     */
    @Override
    public Page<Finance> pageFinance(FinancePageQueryDTO dto) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LambdaQueryWrapper<Finance> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(dto.getUserId() != null,Finance::getUserId, dto.getUserId())
                    .like(StringUtils.hasText(dto.getTitle()), Finance::getTitle, dto.getTitle())
                    .like(StringUtils.hasText(dto.getDescription()), Finance::getDescription, dto.getDescription())
                    .eq(dto.getAmount() != null, Finance::getAmount, dto.getAmount())
                    .eq(dto.getStatus() != null, Finance::getStatus, dto.getStatus())
                    .eq(Finance::getIsDeleted, 0);

            // 根据 statusTime 动态追加时间判断条件
            if (dto.getStatusTime() != null) {
                switch (dto.getStatusTime()) {
                    case 0 -> wrapper.gt(Finance::getStartTime, now); // 未开始
                    case 1 -> wrapper.le(Finance::getStartTime, now)
                            .ge(Finance::getEndTime, now);  // 进行中
                    case 2 -> wrapper.lt(Finance::getEndTime, now);   // 已完成
                }
            }

            Page<Finance> page = new Page<>(dto.getPage(), dto.getPageSize());

            return financeMapper.selectPage(page, wrapper);

        } catch (Exception e) {
            throw new BaseException(MessageConstant.FINANCE_QUERY_ERR);
        }
    }

    @Override
    public void deleteFinanceById(Long id) {
        try {
            LambdaUpdateWrapper<Finance> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Finance::getId, id).set(Finance::getIsDeleted,1);
            this.update(updateWrapper);
        } catch (Exception e) {
            throw new BaseException(MessageConstant.FINANCE_DELETE_ERR);
        }
    }

    @Override
    @Transactional(rollbackFor = BaseException.class)
    public void deleteFianceByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new CategoryException(MessageConstant.QUERY_NOT_NULL);
        }
        try {
            // 分批次处理（避免IN条件过长）
            List<Finance> entities = ids.stream()
                    .map(id -> new Finance().setId(id).setIsDeleted(1))
                    .collect(Collectors.toList());
            this.updateBatchById(entities); // 单条SQL批量执行
        } catch (Exception e) {
            throw new BaseException(MessageConstant.FINANCE_DELETE_ERR);
        }
    }

    @Override
    @OperationLog(OperationType.USER_UPDATE)
    public void updateFinance(Finance finance) {
        try {
            this.updateById(finance);
        } catch (Exception e) {
            throw new BaseException(MessageConstant.FINANCE_UPDATE_ERR);
        }
    }
}
