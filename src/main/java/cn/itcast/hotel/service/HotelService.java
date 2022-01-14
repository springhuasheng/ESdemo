package cn.itcast.hotel.service;

import cn.itcast.hotel.dto.RequestParams;
import cn.itcast.hotel.vo.PageResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: huasheng
 * @Date: 2022/1/12 18:30
 */
public interface HotelService {

    /**
     * 多条件 查询
     */
    PageResult hotelLiset(RequestParams requestParams) throws IOException;

    /**
     * 页面搜索的词条 的聚合搜索
     */
    Map<String, List<String>> holetFilters();

    /**
     * 搜索框 联想功能
     */
    List<String> holetSuggestion(String key);

}
