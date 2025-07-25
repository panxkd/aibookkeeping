package com.xik.aibookkeeping.pojo.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class PointsUpdateMessage implements Serializable {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 获取/消费
     */
    private String changeType;
    /**
     * 积分
     */
    private Integer points;
    /**
     * 来源
     */
    private String source;
    /**
     * 备注
     */
    private String remark;
}