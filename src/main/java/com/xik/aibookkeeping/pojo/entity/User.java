package com.xik.aibookkeeping.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.Accessors;
import org.aspectj.lang.annotation.Before;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Data
@TableName("user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 微信OpenID
     */
    private String openid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * man男/woman女/unknown未知
     */
    private String sex;

    /**
     * admin管理员/user普通用户
     */
    private String role;

    /**
     * 状态（1正常/0禁用）
     */
    private Integer status;

    /**
     * 逻辑删除（1删除/0未删除）
     */
    @TableField(select = false)
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;


}
