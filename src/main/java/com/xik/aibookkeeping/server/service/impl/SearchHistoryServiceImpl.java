package com.xik.aibookkeeping.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xik.aibookkeeping.common.constant.MessageConstant;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.exception.SearchHistoryException;
import com.xik.aibookkeeping.pojo.dto.SearchHistoryDTO;
import com.xik.aibookkeeping.pojo.entity.SearchHistory;
import com.xik.aibookkeeping.pojo.vo.SearchHistoryVO;
import com.xik.aibookkeeping.server.mapper.SearchHistoryMapper;
import com.xik.aibookkeeping.server.service.ISearchHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-30
 */
@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements ISearchHistoryService {



    @Override
    public List<SearchHistory> getAll() {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            if (currentUserId == null) {
                throw new SearchHistoryException(MessageConstant.USER_NOT_NULL);
            }
            LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchHistory::getUserId, currentUserId).eq(SearchHistory::getIsDeleted,0);
            return  baseMapper.selectList(queryWrapper);
        } catch (Exception e) {
            throw new SearchHistoryException(MessageConstant.SEARCH_HISTORY_QUERY_ERR);
        }
    }

    @Override
    public void saveSearchHistory(SearchHistoryDTO searchHistoryDTO) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            if (currentUserId == null) {
                throw new SearchHistoryException(MessageConstant.USER_NOT_NULL);
            }
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUserId(currentUserId);
            searchHistory.setIsDeleted(0);
            searchHistory.setSearchTime(LocalDateTime.now());
            searchHistory.setContent(searchHistoryDTO.getContent());
            baseMapper.insert(searchHistory);
        } catch (Exception e) {
            throw new SearchHistoryException(MessageConstant.SEARCH_HISTORY_INSERT_ERR);
        }
    }

    @Override
    public void deleteAll() {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            if (currentUserId == null) {
                throw new SearchHistoryException(MessageConstant.USER_NOT_NULL);
            }
            LambdaUpdateWrapper<SearchHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper
                    .eq(SearchHistory::getUserId, currentUserId)
                    .set(SearchHistory::getIsDeleted, 1);
            baseMapper.update(null, updateWrapper);
        }  catch (Exception e) {
            throw new SearchHistoryException(MessageConstant.SEARCH_HISTORY_DELETE_ERR);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            LambdaUpdateWrapper<SearchHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper
                    .eq(SearchHistory::getId, id)
                    .set(SearchHistory::getIsDeleted, 1);
            baseMapper.update(null, updateWrapper);
        }  catch (Exception e) {
            throw new SearchHistoryException(MessageConstant.SEARCH_HISTORY_DELETE_ERR);
        }
    }
}
