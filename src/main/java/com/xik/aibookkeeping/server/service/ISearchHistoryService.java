package com.xik.aibookkeeping.server.service;

import com.xik.aibookkeeping.pojo.dto.SearchHistoryDTO;
import com.xik.aibookkeeping.pojo.entity.SearchHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xik.aibookkeeping.pojo.vo.SearchHistoryVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-30
 */
public interface ISearchHistoryService extends IService<SearchHistory> {

    List<SearchHistory> getAll();

    void saveSearchHistory(SearchHistoryDTO searchHistoryDTO);

    void deleteAll();

    void deleteById(Long id);
}
