package com.fct.promotion.service;

import com.fct.promotion.data.entity.CouponPolicy;
import com.fct.promotion.data.entity.Discount;
import com.fct.promotion.data.entity.DiscountProduct;
import com.fct.promotion.interfaces.PageResponse;
import com.fct.promotion.interfaces.PromotionService;
import com.fct.promotion.interfaces.dto.CouponCodeDTO;
import com.fct.promotion.interfaces.dto.DisCountDTO;
import com.fct.promotion.interfaces.dto.DiscountCouponDTO;
import com.fct.promotion.interfaces.dto.OrderProductDTO;
import com.fct.promotion.service.business.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Created by jon on 2017/5/9.
 */
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    CouponPolicyManager couponPolicyManager;

    @Autowired
    CouponCodeManager couponCodeManager;

    @Autowired
    CouponCodeDTOManager couponCodeDTOManager;

    @Autowired
    DiscountManager discountManager;

    @Autowired
    OrderManager orderManager;

    @Autowired
    DiscountProductManager discountProductManager;

    @Autowired
    DiscountProductDTOManager discountProductDTOManager;

    public CouponPolicy saveCouponPolicy(CouponPolicy policy) {
        return couponPolicyManager.add(policy);
    }

    public void auditCouponPolicy(Integer policyId, Boolean pass, Integer userId)
    {
        couponPolicyManager.audit(policyId,pass,userId);
    }

    //获取优惠券策略对象
    public CouponPolicy getCouponPolicy(Integer policyId)
    {
        return couponPolicyManager.findById(policyId);
    }

    public PageResponse<CouponPolicy> findCouponPolicy(Integer status,Integer fetchType,Integer generateStatus, String startTime,
                                               String endTime,Integer pageIndex, Integer pageSize)
    {
        return couponPolicyManager.findAll(status,fetchType,generateStatus,startTime,endTime,pageIndex,pageSize);
    }

    public List<CouponPolicy> findCanReceiveCouponPolicy()
    {
        return couponPolicyManager.findByCanReceive();
    }

    public List<Integer> findReceivedPolicyId(Integer memberId,List<Integer> policyIds)
    {
        return couponCodeManager.findReceivedPolicyId(memberId,policyIds);
    }

    public List<CouponCodeDTO> findMemberCouponCode(Integer policyId,Integer memberId,String code,Integer status,
                                                    Boolean isValid,Integer pageIndex, Integer pageSize)
    {
        return couponCodeDTOManager.findMemberCouponCode(policyId,memberId,code,status,
                isValid,pageIndex,pageSize);
    }

    public Integer getMemberCouponCodeCount(Integer policyId,Integer memberId,String code,Integer status,
                                            Boolean isValid)
    {
        return couponCodeDTOManager.getMemberCouponCodeCount(policyId,memberId,code,status,
                isValid);
    }

    public String receiveCouponCode(Integer memberId,Integer policyId)
    {
        return couponCodeManager.receive(memberId,policyId);
    }

    public CouponCodeDTO getCouponCodeDTOByOrder(Integer memberId, List<OrderProductDTO> productList)
    {
        return couponCodeDTOManager.findByMemberId(memberId,productList,"");
    }

    public CouponCodeDTO validCouponCode(Integer memberId,List<OrderProductDTO> productList,String couponCode)
    {
        return couponCodeDTOManager.findByMemberId(memberId,productList,couponCode);
    }

    public CouponCodeDTO getCouponCodeDTOByCode(String code)
    {
        return couponCodeDTOManager.findByCode(code);
    }

    public void useCouponCode(String code)
    {
        couponCodeManager.setCodeUsed(code);
    }

    public void cancelUseCouponCode(String code)
    {
        couponCodeManager.cancelCodeUsed(code);
    }

    public void saveDiscount(Discount discount, List<DiscountProduct> lsProduct)
    {
        discountManager.add(discount,lsProduct);
    }

    public Discount getDiscountById(Integer discountId)
    {
        return discountManager.findById(discountId);
    }

    public void auditDiscount(Integer discountId,Boolean pass,Integer userId)
    {
        discountManager.audit(discountId,pass,userId);
    }

    public PageResponse<Discount> findDiscount(Integer status, String startTime, String endTime, Integer pageIndex,
                                               Integer pageSize)
    {
        return discountManager.findAll(status,startTime,endTime,pageIndex,pageSize);
    }

    public List<DiscountProduct> findDiscountProduct(Integer discountId)
    {
        return discountProductManager.findByDiscountId(discountId);
    }

    public DisCountDTO getDisCountDTOById(Integer discountId)
    {
        DisCountDTO dto = new DisCountDTO();
        dto.setDiscount(discountManager.findById(discountId));
        dto.setProductList(discountProductManager.findByDiscountId(discountId));
        return dto;
    }

    public Integer useCouponCodeDiscount(String orderId,Integer memberId,Integer memberGradeId,List<OrderProductDTO> products,
                                  String couponCode)
    {
        return orderManager.use(orderId,memberId,products,couponCode,memberGradeId);
    }

    public DiscountCouponDTO getPromotion(Integer memberId, List<OrderProductDTO> products, String couponCode)
    {
        return discountProductDTOManager.getDiscountCoupon(memberId,products,couponCode);
    }
}
