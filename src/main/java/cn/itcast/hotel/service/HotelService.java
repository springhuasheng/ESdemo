package cn.itcast.hotel.service;

import cn.itcast.hotel.dto.RequestParams;
import cn.itcast.hotel.vo.PageResult;

import java.io.IOException;

/**
 * @Author: huasheng
 * @Date: 2022/1/12 18:30
 */
public interface HotelService {

    PageResult hotelLiset(RequestParams requestParams) throws IOException;

}
