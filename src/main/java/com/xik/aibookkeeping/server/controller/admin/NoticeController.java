package com.xik.aibookkeeping.server.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.NoticeDTO;
import com.xik.aibookkeeping.pojo.dto.NoticePageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Notice;
import com.xik.aibookkeeping.server.service.INoticeService;
import com.xik.aibookkeeping.server.service.IRequestLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 系统公告表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminNoticeController")
@RequestMapping("/admin/notice")
@Slf4j
public class NoticeController {

    @Resource
    private INoticeService noticeService;

    @GetMapping("/page")
    public Result<Page<Notice>> page(NoticePageQueryDTO  noticePageQueryDTO){
        log.info("分页查询公告：{}", noticePageQueryDTO);
        Page<Notice> page = noticeService.pageNotice(noticePageQueryDTO);
        log.info("公告查询结果：{}", page);
        return Result.success(page);
    }

    @PostMapping
    public Result save(@RequestBody NoticeDTO noticeDTO){
        log.info("保存的系统公告：{}", noticeDTO);
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeDTO,notice);
        noticeService.saveNotice(notice);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody NoticeDTO noticeDTO){
        log.info("修改的公告{}", noticeDTO);
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeDTO,notice);
        noticeService.updateNotice(notice);
        log.info("修改的系统公告{}", noticeDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Notice> get(@PathVariable("id") Integer id){
        log.info("根据id获取公告：{}",id);
        Notice notice = noticeService.getByIdNotice(id);
        return Result.success(notice);
    }

    @PostMapping("/status/{status}")
    public Result status(@PathVariable("status") Integer status, Long id){
        log.info("修改公告状态id:{},状态：{}",id,status);
        Notice notice = Notice.builder().id(id).status(status).build();
        noticeService.updateStatus(notice);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable("id") Integer id){
        log.info("需要删除的公告：{}", id);
        noticeService.deleteByNotice(id);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteByIds(@RequestBody List<Long> ids){
        log.info("批量删除的公告id:{}",ids);
        noticeService.deleteByIds(ids);
        return Result.success();
    }


}
