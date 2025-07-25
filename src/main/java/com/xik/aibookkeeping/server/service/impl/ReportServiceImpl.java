package com.xik.aibookkeeping.server.service.impl;


import com.xik.aibookkeeping.pojo.entity.Report;
import com.xik.aibookkeeping.server.mapper.ReportMapper;
import com.xik.aibookkeeping.server.service.IReportService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账单报告表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

}
