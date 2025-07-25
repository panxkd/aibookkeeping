package com.xik.aibookkeeping.pojo.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 系统公告
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容（支持富文本或 Markdown）
     */
    private String content;

    /**
     * 公告类型（如：系统、活动、更新）
     */
    private String type;


    /**
     * 发布时间（便于定时发布）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedTime;

    /**
     * 公告下架时间时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;


}
