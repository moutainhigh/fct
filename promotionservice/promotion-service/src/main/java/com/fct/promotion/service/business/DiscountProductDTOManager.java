package com.fct.promotion.service.business;

import com.fct.promotion.data.entity.Discount;
import com.fct.promotion.data.entity.DiscountProduct;
import com.fct.promotion.interfaces.dto.CouponCodeDTO;
import com.fct.promotion.interfaces.dto.DiscountCouponDTO;
import com.fct.promotion.interfaces.dto.DiscountProductDTO;
import com.fct.promotion.interfaces.dto.OrderProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jon on 2017/5/13.
 */
@Service
public class DiscountProductDTOManager {

    @Autowired
    DiscountProductManager discountProductManager;

    @Autowired
    DiscountManager discountManager;

    @Autowired
    CouponCodeDTOManager couponCodeDTOManager;

    public List<DiscountProductDTO> findByProduct(List<Integer> productIds, Integer filterNoBegin)
    {
        if (productIds == null || productIds.size() < 1)
        {
            throw new IllegalArgumentException("产品编号列表不能为空");
        }

        List<DiscountProduct> discountProductList = discountProductManager.findByValid(productIds,filterNoBegin);
        if (discountProductList == null || discountProductList.size() < 1)
        {
            return null;
        }
        List<Integer> discountIds = new ArrayList<>();
        for (DiscountProduct p: discountProductList
             ) {
            discountIds.add(p.getDiscountId());
        }
        List<Discount> discountList = discountManager.findByDiscountId(discountIds);
        Map<Integer, Discount> map = new HashMap<>();
        for (Discount dicount:discountList
             ) {
            map.put(dicount.getId(),dicount);
        }

        List<DiscountProductDTO> lst = new ArrayList<>();

        for (DiscountProduct p: discountProductList
                ) {

            discountIds.add(p.getDiscountId());
            DiscountProductDTO dto = new DiscountProductDTO();
            dto.setProductId(p.getProductId());
            dto.setDiscountProduct(p);
            dto.setDiscount(map.get(p.getDiscountId()));

            lst.add(dto);
        }
        return lst;
    }

    /// <summary>
    /// 订单提交前，根据订单购物商品，获取有效折扣及优惠券
    /// </summary>
    /// <returns></returns>
    public DiscountCouponDTO getDiscountCoupon(Integer memberId, List<OrderProductDTO> lsProduct, String couponCode)
    {
        if (lsProduct == null || lsProduct.size() < 1)
        {
            throw new IllegalArgumentException("产品编号列表不能为空");
        }

        List<Integer> productIds = new ArrayList<>();
        for (OrderProductDTO p:lsProduct
             ) {
            productIds.add(p.getProductId());
        }

        DiscountCouponDTO dc = new DiscountCouponDTO();
        List<DiscountProduct> discountProductList = discountProductManager.findByValid(productIds,1);
        if (discountProductList !=null)
        {
            for (OrderProductDTO p:lsProduct
                 ) {
                DiscountProduct dp = single(discountProductList,p.getProductId());
                if (dp != null)
                {
                    p.setDiscountId(dp.getDiscountId());
                    p.setDiscountPrice(p.getRealPrice().multiply(dp.getDiscountRate()));
                    p.setSingleCount(dp.getSingleCount());
                }
                else
                {
                    p.setDiscountPrice(p.getRealPrice());
                }
            }
        }
        CouponCodeDTO coupon = couponCodeDTOManager.findByMemberId(memberId,lsProduct,couponCode);
        dc.setDiscount(lsProduct);
        dc.setCoupon(coupon);
        return dc;
    }

    private  DiscountProduct single(List<DiscountProduct> lsProduct,Integer productId)
    {
        for (DiscountProduct p:lsProduct
             ) {
            if(p.getProductId() == productId)
            {
                return p;
            }
        }
        return null;
    }
}