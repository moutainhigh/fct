package com.fct.promotion.service.business;

import com.fct.common.utils.ListUtils;
import com.fct.common.utils.QueryResult;
import com.fct.promotion.interfaces.dto.CouponCodeDTO;
import com.fct.promotion.interfaces.dto.OrderProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by jon on 2017/5/12.
 */
@Service
public class CouponCodeDTOManager {

    @Autowired
    JdbcTemplate jt;

    public List<CouponCodeDTO> findMemberCouponCode(Integer policyId,Integer memberId,String code,Integer status,
                                                           Boolean isValid,Integer startIndex, Integer count)
    {
        String condition = getCondition(policyId,memberId,code,status,isValid);

        String orderby = " c.Id desc";
        String fields = "c.*,p.Name as CouponName,p.StartTime,p.EndTime,p.Amount as Amount,p.FullAmount as FullAmount,p.ProductIds as ProductIds";
        String sql = String.format("select * from (select %s,ROW_NUMBER() over(order by  %s) as r  from CouponPolicy p inner join CouponCode c on p.Id = c.policyId where %s)t where t.r between %s and %s",
                fields, orderby, condition, startIndex, startIndex + count - 1);

        return jt.queryForList(sql,CouponCodeDTO.class);

    }

    public Integer getMemberCouponCodeCount(Integer policyId,Integer memberId,String code,Integer status,
                                            Boolean isValid)
    {
        String condition = getCondition(policyId,memberId,code,status,isValid);
        String sql = "Select Count(0) from CouponPolicy p inner join CouponCode c on p.Id = c.policyId  where "+condition;
        return jt.queryForObject(sql,Integer.class);
    }

    private String getCondition(Integer policyId,Integer memberId,String code,Integer status,Boolean isValid)
    {
        StringBuilder sb = new StringBuilder(" 1=1");

        if (policyId > 0)
        {
            sb.append(" and p.Id="+policyId);
        }

        if (memberId > 0)
        {
            sb.append(" and c.MemberId="+memberId);
        }

        if (!StringUtils.isEmpty(code))
        {
            sb.append(" and c.Code='"+ code +"'");
        }

        if (status>-1)
        {
            sb.append(" and c.Status="+status);
        }

        if (isValid)
        {
            sb.append(" and p.StartTime<=getdate() and p.EndTime>=getdate()");
        }

        return sb.toString();
    }

    public CouponCodeDTO findByMemberId(Integer memberId, List<OrderProductDTO> products, String couponCode)
    {
        if (memberId < 1)
        {
            throw new IllegalArgumentException("会员编号不正确");
        }

        if (products == null || products.size() < 1)
        {
            throw new IllegalArgumentException("产品列表不能为空");
        }

        CouponCodeDTO obj = null;
        List<CouponCodeDTO> result = findMemberCouponCode(0,memberId,couponCode,0,
                true,1,50);

        if (!StringUtils.isEmpty(couponCode))
        {
            if (result == null || result == null || result.size() < 1)
            {
                throw new IllegalArgumentException("该优惠券不存在或已过期");
            }
            obj = result.get(0);
            if (obj.getMemberId() > 0)
            {
                if (memberId != obj.getMemberId())
                {
                    throw new IllegalArgumentException("您没有使用该优惠券的权限");
                }
            }
        }



        if (result == null || result == null)
        {
            return obj;
        }

        //按优惠券面额倒序
        List<CouponCodeDTO> userCouponList = result;
        ListUtils.sort(result,false,"amount");

        BigDecimal orderTotalPrice = new BigDecimal(0);
        for (OrderProductDTO product:products
             ) {
            orderTotalPrice.add(product.getRealPrice().multiply(new BigDecimal(product.getCount())));
        }
        for (CouponCodeDTO code:userCouponList
             ) {
            if (!StringUtils.isEmpty(code.getProductIds())) //只针对某些产品
            {
                BigDecimal price = new BigDecimal(0);
                String temp = String.format(",%s,", code.getProductIds());
                for (OrderProductDTO product:products
                        ) {
                    if (!temp.contains("," + product.getProductId() + ","))
                    {
                        continue;
                    }
                    price.add(product.getRealPrice().multiply(new BigDecimal(product.getCount())));
                    if (code.getFullAmount().doubleValue() > 0)
                    {
                        if (price.doubleValue() >= code.getFullAmount().doubleValue()) //大于满额
                        {
                            return code;
                        }
                    }
                    else
                    {
                        if (price.doubleValue() >= code.getAmount().doubleValue()) //大于面额
                        {
                            return code;
                        }
                    }
                }
            }
            else if (code.getFullAmount().doubleValue() > 0) //针对全场且有条件
            {
                if (orderTotalPrice.doubleValue() >= code.getFullAmount().doubleValue()) //大于满额
                {
                    return code;
                }
            }
            else//针对全场
            {
                if (orderTotalPrice.doubleValue() >= code.getAmount().doubleValue()) //大于面额
                {
                    return code;
                }
            }
        }

        return obj;
    }

    public CouponCodeDTO findByCode(String code)
    {
        List<CouponCodeDTO> result = findMemberCouponCode(0,0,code,-1,
                false,1,10);
        if (result == null || result == null || result.size() < 1)
        {
            return new CouponCodeDTO();
        }
        return result.get(0);
    }
}