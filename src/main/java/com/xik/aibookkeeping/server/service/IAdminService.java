package com.xik.aibookkeeping.server.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.dto.AdminDTO;
import com.xik.aibookkeeping.pojo.dto.AdminLoginDTO;
import com.xik.aibookkeeping.pojo.dto.AdminPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Admin;
import com.xik.aibookkeeping.pojo.vo.AdminLoginVO;


/**
 * <p>
 * 管理员表 服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
public interface IAdminService extends IService<Admin> {

    Admin getByAdminId(Long id);

    void removeByAdminId(Long id);

    void updateByAdminId(Admin admin);

    void saveAdmin(Admin admin);

    Page<Admin> pageQuery(AdminPageQueryDTO adminPageQueryDTO);

    AdminLoginVO login(AdminLoginDTO adminLoginDTO);

    void logout();

    void updateStatus(Admin admin);

    long getNumber();
}
