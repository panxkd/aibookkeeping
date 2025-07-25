package com.xik.aibookkeeping.server.controller.admin;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.AdminDTO;
import com.xik.aibookkeeping.pojo.dto.AdminLoginDTO;
import com.xik.aibookkeeping.pojo.dto.AdminPageQueryDTO;
import com.xik.aibookkeeping.pojo.entity.Admin;
import com.xik.aibookkeeping.pojo.vo.AdminLoginVO;
import com.xik.aibookkeeping.server.service.IAdminService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 管理员表 前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@RestController("AdminAdminController")
@RequestMapping("/admin/admin")
@Slf4j
public class AdminController {

    @Resource
    private IAdminService adminService;

    /**
     * 根据id获取管理员信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Admin> getAdminById(@PathVariable Long id){
        log.info("管理员id：{}", id);
        Admin admin = adminService.getByAdminId(id);
        return Result.success(admin);
    }

    @DeleteMapping("/{id}")
    public Result deleteAdminById(@PathVariable Long id){
        log.info("删除的管理员id:{}", id);
        adminService.removeByAdminId(id);
        return Result.success();
    }

    @PutMapping
    public Result updateAdmin(@RequestBody AdminDTO adminDTO){
        log.info("修改管理员信息：{}", adminDTO);
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminDTO,admin);
        admin.setPassword(DigestUtils.md5DigestAsHex(admin.getPassword().getBytes()));
        adminService.updateByAdminId(admin);
        return Result.success();
    }

    @PostMapping
    public Result addAdmin(@RequestBody AdminDTO adminDTO){
        log.info("添加管理员{}", adminDTO);
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminDTO,admin);
        adminService.saveAdmin(admin);
        return Result.success();
    }

    @GetMapping("/page")
    public  Result<Page<Admin>> getAllAdmin(AdminPageQueryDTO adminPageQueryDTO) {
        log.info("分页查询：{}", adminPageQueryDTO);
        Page<Admin> pageQuery = adminService.pageQuery(adminPageQueryDTO);
        return Result.success(pageQuery);
    }

    @PostMapping("/login")
    public Result<AdminLoginVO>  login(@RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("管理员登录：{}", adminLoginDTO.getUsername());
        AdminLoginVO adminLoginVO = adminService.login(adminLoginDTO);
        return Result.success(adminLoginVO);
    }

    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        log.info("管理员：{} 修改状态", id);
        Admin admin = Admin.builder()
                .status(status)
                .id(id)
                .build();

        adminService.updateStatus(admin);
        return Result.success();
    }

    @GetMapping("/number")
    public Result<Long> getNumber(){
        log.info("获取管理员总条数");
        Long number = adminService.getNumber();
        return Result.success(number);
    }


}
