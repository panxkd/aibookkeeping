package com.xik.aibookkeeping.server.controller.user;


import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.entity.Notice;
import com.xik.aibookkeeping.server.service.INoticeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 系统公告表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("UserNoticeController")
@RequestMapping("/user/notice")
@Slf4j
public class NoticeController {

    @Resource
    private INoticeService noticeService;

    @GetMapping
    public Result<Notice> getNotices() {
        log.info("用户获取系统公告");
        Notice notice = noticeService.getNotice();
        return Result.success(notice);
    }

}
