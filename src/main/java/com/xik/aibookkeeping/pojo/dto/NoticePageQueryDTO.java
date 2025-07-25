package com.xik.aibookkeeping.pojo.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 系统公告
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticePageQueryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int page;

    private int pageSize;

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
     * 状态（1:启用，2：禁用）
     */
    private Integer status;

    /**
     * 查询创建时间范围
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 查询时间范围
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;






}
