package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.dto.NoticePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Notice;

import java.util.List;

/**
 * <p>
 * 系统公告表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface INoticeService extends IService<Notice> {

    Page<Notice> pageNotice(NoticePageQueryDTO noticePageQueryDTO);

    void saveNotice(Notice notice);

    void updateNotice(Notice notice);

    Notice getByIdNotice(Integer id);

    void updateStatus(Notice notice);

    void deleteByNotice(Integer id);

    void deleteByIds(List<Long> ids);

    Notice getNotice();
}
