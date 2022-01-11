package cn.itcast.hotel.pojo;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@TableName("goods")
public class Goods {

    private Integer id;
    private String title;
    private Double price;
    private Integer stock;
    @TableField(value = "saleNum")
    private Integer saleNum;

    @TableField(value = "createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private String createTime;
    @TableField(value = "categoryName")
    private String categoryName;
    @TableField(value = "brandName")
    private String brandName;

    @TableField(exist = false)
    private HashMap spec;

    @TableField(value = "spec")
    @JsonIgnore
    private String specStr;

    //{"机身内存":"16G","网络":"联通3G"}
    public HashMap getspec(){
        return JSON.parseObject(this.specStr, HashMap.class);
    }



}


/*

需求:
1. 使用MybatisPlus查询goods表
2. 在ES中创建索引并按照goods表添加映射 (title字段需要分词)
3. 将goods表中的数据导入到ES中
4. 查询品牌是三星的
5. 查询title中包含华为手机的
*/