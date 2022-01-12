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

/**
 * @Author: huasheng
 * @Date: 2022/1/12 18:29
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @PostMapping("/list")
    public PageResult hotelLiset(@RequestBody RequestParams requestParams) throws IOException {
        PageResult pageResult = hotelService.hotelLiset(requestParams);
        return pageResult;
    }

    /**
     * 暂时没有用到
     */
//    @PostMapping("/filters")
//    public PageResult hotelFilters(@RequestBody RequestParams requestParams) {
//        return new PageResult();
//    }

}
