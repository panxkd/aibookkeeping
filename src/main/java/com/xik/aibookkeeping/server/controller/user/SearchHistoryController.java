package com.xik.aibookkeeping.server.controller.user;


import com.xik.aibookkeeping.common.result.Result;
import com.xik.aibookkeeping.pojo.dto.SearchHistoryDTO;
import com.xik.aibookkeeping.pojo.entity.SearchHistory;
import com.xik.aibookkeeping.pojo.vo.SearchHistoryVO;
import com.xik.aibookkeeping.server.mapper.SearchHistoryMapper;
import com.xik.aibookkeeping.server.service.ISearchHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author panxikai
 * @since 2025-06-30
 */
@RestController
@RequestMapping("/user/search-history")
@Slf4j
public class    SearchHistoryController {

    @Resource
    private ISearchHistoryService searchHistoryService;

    /**
     * 获取所有搜索的历史数据 (根据用户id查)
     * @param
     * @return
     */
    @GetMapping("all")
    public Result<List<SearchHistory>> getAll() {
        List<SearchHistory> searchHistoryList = searchHistoryService.getAll();
        return Result.success(searchHistoryList);
    }

    /**
     * 保存搜索历史记录
     * @param searchHistoryDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody SearchHistoryDTO searchHistoryDTO) {
        log.info("搜索记录：{}", searchHistoryDTO.getContent());
        searchHistoryService.saveSearchHistory(searchHistoryDTO);
        return Result.success();
    }

    /**
     * 删除所有搜索记录
     * @return
     */
    @DeleteMapping
    public Result deleteAll() {
        searchHistoryService.deleteAll();
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long  id) {
        log.info("删除搜索历史的id ：{}", id);
        searchHistoryService.deleteById(id);
        return Result.success();
    }
}
