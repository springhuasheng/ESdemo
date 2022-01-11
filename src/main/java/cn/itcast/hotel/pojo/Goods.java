package cn.itcast.hotel.pojo;

import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;
import java.util.HashMap;

public class Goods {

    private Integer id;
    private String title;
    private Double price;
    private Integer stock;
    private Integer saleNum;
    private Date createTime;
    private String categoryName;
    private String brandName;
    @TableField(exist = false)
    private HashMap spec;

    private String specStr;
}


/*

需求:
1. 使用MybatisPlus查询goods表
2. 在ES中创建索引并按照goods表添加映射 (title字段需要分词)
3. 将goods表中的数据导入到ES中
4. 查询品牌是三星的
5. 查询title中包含华为手机的
*/