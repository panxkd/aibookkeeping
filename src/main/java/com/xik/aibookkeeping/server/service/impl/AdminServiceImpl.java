package com.xik.aibookkeeping.server.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.constant.AdminRole;
import com.xik.aibookkeeping.common.constant.JwtClaimsConstant;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.BaseContext;

import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.exception.AdminException;
import com.xik.aibookkeeping.common.properties.JwtProperties;
import com.xik.aibookkeeping.common.utils.JwtUtil;
import com.xik.aibookkeeping.pojo.dto.AdminDTO;
import com.xik.aibookkeeping.pojo.dto.AdminLoginDTO;
import com.xik.aibookkeeping.pojo.dto.AdminPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Admin;
import com.xik.aibookkeeping.pojo.vo.AdminLoginVO;
import com.xik.aibookkeeping.server.mapper.AdminMapper;
import com.xik.aibookkeeping.server.service.IAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 管理员表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Resource
    private JwtProperties jwtProperties;
    @Autowired
    private AdminMapper adminMapper;


    @Override
    public Admin getByAdminId(Long id) {
        try {
            LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Admin::getIsDeleted, 0).eq(Admin::getId, id);
            Admin admin = this.getOne(queryWrapper);
            if (admin != null) {
                admin.setPassword("*******");
            }
            return admin;
        } catch (Exception e){
            throw new AdminException(MessageConstant.ADMIN_QUERY_ERR + e.getMessage());
        }
    }

    @Override
    public void removeByAdminId(Long id) {
        try {
            LambdaUpdateWrapper<Admin> queryWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.set(Admin::getIsDeleted, 1).eq(Admin::getId, id);
            this.update(null, queryWrapper);
        } catch (Exception e){
            throw new AdminException(MessageConstant.ADMIN_DELETE_ERR + e.getMessage());
        }
    }

    @Override
    @OperationLog(OperationType.ADMIN_UPDATE)
    public void updateByAdminId(Admin admin) {
        try {
            this.updateById(admin);
        } catch (Exception e) {
            throw new AdminException(MessageConstant.ADMIN_UPDATE_ERR + e.getMessage());
        }
    }

    @Override
    @OperationLog(OperationType.ADMIN_INSERT)
    public void saveAdmin(Admin admin) {
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getIsDeleted, 0).eq(Admin::getUsername, admin.getUsername());
        Admin adminEntity = this.getOne(queryWrapper);
        if (adminEntity != null) {
            throw new AdminException(MessageConstant.ADMIN_FOUND + admin.getUsername());
        }
        try {
            admin.setPassword(DigestUtils.md5DigestAsHex(admin.getPassword().getBytes()));
            this.save(admin);
        }  catch (Exception e) {
            throw new AdminException(MessageConstant.ADMIN_SAVE_ERR + e.getMessage());
        }

    }

    @Override
    public Page<Admin> pageQuery(AdminPageQueryDTO adminPageQueryDTO) {
        try {
            Page<Admin> pageQuery = Page.of(adminPageQueryDTO.getPage(), adminPageQueryDTO.getPageSize());
            // 构建查询条件
            LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
            // 添加模糊查询
            if (StringUtils.isNoneBlank(adminPageQueryDTO.getUsername())) {
                queryWrapper.like(Admin::getUsername, adminPageQueryDTO.getUsername());
            }
            if (adminPageQueryDTO.getId() != null) {
                queryWrapper.like(Admin::getId, adminPageQueryDTO.getId());
            }
            // 查询未被删除的 并且按照时间排序
            queryWrapper.eq(Admin::getIsDeleted, 0).orderByDesc(Admin::getCreateTime);

            return this.page(pageQuery, queryWrapper);
        } catch (Exception e) {
            throw new AdminException(MessageConstant.ADMIN_PAGE_QUERY_ERR + e.getMessage());
        }
    }

    @Override
    public AdminLoginVO login(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getIsDeleted, 0).eq(Admin::getUsername, username);
        Admin admin = this.getOne(queryWrapper);
        if (admin == null) {
            // 账号不存在
            throw new AdminException(MessageConstant.ADMIN_NOT_FOUND);
        }
        // 进行MD5加密后再比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(admin.getPassword())) {
            // 密码错误
            throw new AdminException(MessageConstant.ADMIN_PASSWORD_ERR);
        }
        if (admin.getStatus() == 0) {
            // 账号被锁定
            throw new AdminException(MessageConstant.ACCOUNT_LOCKED);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );
        return AdminLoginVO.builder()
                .id(admin.getId())
                .username(username)
                .role(admin.getRole())
                .token(token)
                .build();
    }

    @Override
    public void logout() {

    }

    @Override
    @OperationLog(OperationType.ADMIN_UPDATE)
    public void updateStatus(Admin admin) {
        try {
            Long currentId = BaseContext.getCurrentId();
            Admin currentAdmin = this.getById(currentId);
            Admin adminData = this.getById(admin.getId());
            if (adminData == null) {
                throw new AdminException(MessageConstant.ADMIN_NOT_FOUND);
            }
            if  (!AdminRole.SUPER_ADMIN.equals(currentAdmin.getRole())) {
                // 非超级管理员不能修改
                throw new AdminException(MessageConstant.ADMIN_NOT_UPDATE);
            }
            this.updateById(admin);
        } catch (Exception e){
            throw new AdminException(MessageConstant.ADMIN_UPDATE_ERR + e.getMessage());
        }

    }

    @Override
    public long getNumber() {
        try {
            LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Admin::getIsDeleted, 0);
            return adminMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            throw new AdminException(MessageConstant.ADMIN_QUERY_ERR + e.getMessage());
        }
    }
}
