package com.xik.aibookkeeping.server.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.exception.CategoryException;
import com.xik.aibookkeeping.common.exception.NoticeException;
import com.xik.aibookkeeping.common.exception.base.BaseException;
import com.xik.aibookkeeping.pojo.dto.NoticePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Finance;
import com.xik.aibookkeeping.pojo.entity.Notice;
import com.xik.aibookkeeping.server.mapper.NoticeMapper;
import com.xik.aibookkeeping.server.service.INoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统公告表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

    /**
     * 分页查询
     * @param noticePageQueryDTO
     * @return
     */
    @Override
    public Page<Notice> pageNotice(NoticePageQueryDTO noticePageQueryDTO) {
        try {
            Page<Notice> page =  new Page<>(noticePageQueryDTO.getPage(), noticePageQueryDTO.getPageSize());
            LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(noticePageQueryDTO.getTitle() != null, Notice::getTitle, noticePageQueryDTO.getTitle())
                    .eq(noticePageQueryDTO.getContent() != null, Notice::getContent, noticePageQueryDTO.getContent())
                    .eq(noticePageQueryDTO.getType() != null, Notice::getType, noticePageQueryDTO.getType())
                    .eq(noticePageQueryDTO.getStatus() != null, Notice::getStatus, noticePageQueryDTO.getStatus())
                    .ge(noticePageQueryDTO.getStartTime() != null, Notice::getCreateTime, noticePageQueryDTO.getStartTime())
                    .le(noticePageQueryDTO.getEndTime() != null, Notice::getCreateTime, noticePageQueryDTO.getEndTime())
                    .orderByDesc(Notice::getCreateTime);
            return  baseMapper.selectPage(page, queryWrapper);
        } catch (Exception e) {
            throw new NoticeException(MessageConstant.NOTICE_QUERY_ERR);
        }
    }

    @Override
    @OperationLog(OperationType.ADMIN_INSERT)
    public void saveNotice(Notice notice) {
        try {
            baseMapper.insert(notice);
        } catch (Exception e) {
            throw new NoticeException(MessageConstant.NOTICE_SAVE_ERR);
        }
    }

    @Override
    @OperationLog(OperationType.ADMIN_UPDATE)
    public void updateNotice(Notice notice) {
        try {
            baseMapper.updateById(notice);
        } catch (Exception e) {
            throw new NoticeException(MessageConstant.NOTICE_UPDATE_ERR);
        }
    }

    @Override
    public Notice getByIdNotice(Integer id) {
        try {
            return baseMapper.selectById(id);
        } catch (Exception e) {
            throw new NoticeException(MessageConstant.NOTICE_QUERY_ERR);
        }
    }

    @Override
    @OperationLog(OperationType.ADMIN_UPDATE)
    public void updateStatus(Notice notice) {
        try {
            baseMapper.updateById(notice);
        } catch (Exception e) {
            throw new NoticeException(MessageConstant.NOTICE_UPDATE_ERR);
        }
    }

    @Override
    public void deleteByNotice(Integer id) {
        try {
            LambdaUpdateWrapper<Notice> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Notice::getId, id).set(Notice::getIsDeleted,1);
            baseMapper.update(updateWrapper);
        } catch (Exception e) {
            throw new NoticeException(MessageConstant.NOTICE_DELETE_ERR);
        }
    }

    @Override
    @Transactional(rollbackFor = BaseException.class)
    public void deleteByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new CategoryException(MessageConstant.QUERY_NOT_NULL);
        }
        try {
            // 分批次处理（避免IN条件过长）
            List<Notice> entities = ids.stream()
                    .map(id -> new Notice().setId(id).setIsDeleted(1))
                    .collect(Collectors.toList());
            this.updateBatchById(entities); // 单条SQL批量执行
        } catch (Exception e) {
            throw new BaseException(MessageConstant.NOTICE_DELETE_ERR);
        }
    }

    /**
     * 用户获取系统公告
     * @return
     */
    @Override
    public Notice getNotice() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(Notice::getIsDeleted,0)
                    .eq(Notice::getStatus,1)
                    .le(Notice::getPublishedTime,now)
                    .ge(Notice::getCancelTime,now)
                    .orderByDesc(Notice::getPublishedTime);
            return baseMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new NoticeException(MessageConstant.NOTICE_QUERY_ERR);
        }
    }
}
