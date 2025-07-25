package com.xik.aibookkeeping.server.controller.common;




import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.exception.FileUpLoadException;
import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.common.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口，用于上传图片
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传参数：{}",file);
        //将文件上传至阿里云服务器
        try{
            //将文件后缀提取出来
            //获取原始文件名
            String originalFilename = file.getOriginalFilename();
            //原始文件名的后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新的文件名
            String objectName = UUID.randomUUID().toString() + extension;
            //接收返回结果
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e){
            // 文件上传失败
            throw new FileUpLoadException(MessageConstant.FILE_UPLOAD_ERR);
        }
    }
}
