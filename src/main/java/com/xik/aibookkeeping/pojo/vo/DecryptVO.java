package com.xik.aibookkeeping.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecryptVO implements Serializable {
    private String openid;
    private String sessionKey;
}
