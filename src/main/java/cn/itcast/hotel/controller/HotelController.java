package cn.itcast.hotel.controller;

import cn.itcast.hotel.dto.RequestParams;
import cn.itcast.hotel.service.HotelService;
import cn.itcast.hotel.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: huasheng
 * @Date: 2022/1/12 18:29
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    /**
     * 多条件 查询
     */
    @PostMapping("/list")
    public PageResult hotelLiset(@RequestBody RequestParams requestParams) throws IOException {
        return hotelService.hotelLiset(requestParams);
    }

    /**
     * 页面搜索的词条 的聚合搜索
     */
    @RequestMapping("filters")
    public Map<String, List<String>> holetFilters() {
        // 调用Service 进行查询
        return hotelService.holetFilters();
    }

    /**
     * 搜索框 联想功能
     */
    @RequestMapping("suggestion")
    public List<String> holetSuggestion(String key) {
        // 调用Service 进行查询
        return hotelService.holetSuggestion(key);
    }

}
