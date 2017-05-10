package com.fct.promotion.interfaces.dto;

import java.math.BigDecimal;

/**
 * Created by jon on 2017/5/9.
 */
public class OrderProductDTO {

    private Integer productId;

    private Integer count;

    /// <summary>
    /// 折后价
    /// </summary>
    private BigDecimal realPrice;

    private BigDecimal discountPrice;

    private Integer discountId;

    /// <summary>
    /// 规格ID
    /// </summary>
    private Integer sizeId;

    private Integer singleCount;
}
