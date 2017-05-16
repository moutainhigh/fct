package com.fct.mall.data.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * Created by jon on 2017/5/15.
 */
@Entity
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoodsSpecification {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    /// <summary>
    /// 宝贝Id
    /// </summary>
    private Integer goodsId;

    /// <summary>
    /// 名称
    /// </summary>
    private String name;

    /// <summary>
    /// 图片
    /// </summary>
    private String image;

    /// <summary>
    /// 商品编码
    /// </summary>
    private String code;

    /// <summary>
    /// 商品二维条码
    /// </summary>
    private String barCode;

    /// <summary>
    /// 售价
    /// </summary>
    private BigDecimal price;

    /// <summary>
    /// 佣金
    /// </summary>
    private BigDecimal commission;

    /// <summary>
    /// 库存
    /// </summary>
    private Integer stockCount;

    /// <summary>
    /// 排序
    /// </summary>
    private Integer sortIndex;
}
