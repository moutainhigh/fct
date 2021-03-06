package com.fct.finance.service.business;

import com.fct.core.json.JsonConverter;
import com.fct.core.utils.DateUtils;
import com.fct.core.utils.PageUtil;
import com.fct.core.utils.StringHelper;
import com.fct.finance.data.entity.MemberAccount;
import com.fct.finance.data.entity.MemberAccountHistory;
import com.fct.finance.data.entity.PayOrder;
import com.fct.finance.data.repository.PayOrderRepository;
import com.fct.finance.interfaces.PageResponse;
import com.fct.message.interfaces.MessageService;
import com.fct.message.interfaces.model.MQPayRefund;
import com.fct.message.interfaces.model.MQPaySuccess;
import com.fct.message.interfaces.model.MQPayTrade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jon on 2017/4/10.
 */
@Service
public class PayOrderManager  {

    @Autowired
    private PayOrderRepository payOrderRepository;

    @Autowired
    private MemberAccountManager memberAccountManager;

    @Autowired
    private MemberAccountHistoryManager memberAccountHistoryManager;

    @Autowired
    private RefundRecordManager refundRecordManager;

    @Autowired
    private MessageService messageService;

    @Autowired
    private JdbcTemplate jt;

    public void save(PayOrder pay)
    {
        payOrderRepository.save(pay);
    }

    @Transactional
    public PayOrder create(PayOrder pay)
    {
        //校验参数
        valid(pay);

        //判读同一业务是否重复生成支付记录
        PayOrder exitOrder =  payOrderRepository.findBytradeTypeAndTradeId(pay.getTradeType(),pay.getTradeId());

        if(exitOrder != null)
        {
            if (exitOrder.getStatus() != Constants.enumPayStatus.waitpay.getValue())
            {
                throw new IllegalArgumentException("支付请求异常。");
            }
            if(DateUtils.compareDate(exitOrder.getExpiredTime(),new Date())<0)
            {
                throw new IllegalArgumentException("支付时间已过。");
            }
            exitOrder.setPayPlatform(pay.getPayPlatform());
            payOrderRepository.save(exitOrder);
            return exitOrder;
        }
        pay.setRefundAmount(new BigDecimal(0));
        pay.setRefundPoints(0);
        pay.setStatus(Constants.enumPayStatus.waitpay.getValue());
        pay.setCreateTime(new Date());
        pay.setOrderId(StringHelper.generatePayOrderId());

        MemberAccount account = memberAccountManager.findById(pay.getMemberId());

        //全部使用积分或账户余额支付
        if (pay.getPayAmount().doubleValue() <= 0 && (pay.getPoints()>0 || pay.getAccountAmount().doubleValue()>0))
        {
            if (account == null)
            {
                throw new IllegalArgumentException("非法操作");
            }
            if (account.getAvailableAmount().doubleValue() < pay.getAccountAmount().doubleValue())
            {
                throw new IllegalArgumentException("余额不足");
            }
            // 积分
            if (account.getPoints() < pay.getPoints())
            {
                throw new IllegalArgumentException("积分不足");
            }
            pay.setPayTime(new Date());
            pay.setStatus(Constants.enumPayStatus.success.getValue());
            payOrderRepository.save(pay);

            BigDecimal useRechargeAmount = new BigDecimal(0);
            BigDecimal useWithdrawAmount = new BigDecimal(0);

            //可用余额减少
            calculateAmount(account,pay.getAccountAmount(),useRechargeAmount,useWithdrawAmount);

            //减去充值金额或可申请提现金额
            account.setPoints(account.getPoints()-pay.getPoints());
            //实时更新虚拟账户手机号码
            account.setCellPhone(pay.getCellPhone());

            memberAccountManager.save(account);

            memberAccountHistoryManager.add(account,pay.getAccountAmount(),useRechargeAmount,useWithdrawAmount,pay.getPoints(),
                    pay.getTradeType(),pay.getTradeId(),0,pay.getDescription());

            //写入消息
            SendMessageQ(pay, Constants.enumPayStatus.success);

        }
        else {
            //校验用户是否存在，如不存在则创建memberAccount
            if(account == null)
            {
                account = new MemberAccount();
                account.setMemberId(pay.getMemberId());
                account.setCellPhone(pay.getCellPhone());
                account.setCreateTime(new Date());
                account.setAccumulateIncome(new BigDecimal(0));
                account.setAccumulatePoints(0);
                account.setAvailableAmount(new BigDecimal(0));
                account.setFrozenAmount(new BigDecimal(0));
                account.setPoints(0);
                account.setRechargeAmount(new BigDecimal(0));
                account.setWithdrawAmount(new BigDecimal(0));

                memberAccountManager.save(account);
            }
            payOrderRepository.save(pay);
        }
        return pay;
    }

    private void calculateAmount(MemberAccount account, BigDecimal payAccountAmount,BigDecimal useRechargeAmount,
                                 BigDecimal useWithdrawAmount)
    {
        //可用余额减少
        account.setAvailableAmount(account.getAvailableAmount().subtract(payAccountAmount));
        //优先扣除充值的余额，如不足才扣除可申请提现金额
        BigDecimal rechargeAmount =  account.getRechargeAmount().subtract(payAccountAmount);
        if(rechargeAmount.doubleValue()>=0)
        {
            account.setRechargeAmount(rechargeAmount);

            useRechargeAmount = rechargeAmount;
        }
        else
        {
            //充值余额不可扣，使用账户余额-当前剩余充值余额
            rechargeAmount = payAccountAmount.subtract(account.getRechargeAmount());

            useRechargeAmount = rechargeAmount;

            account.setRechargeAmount(new BigDecimal(0)); //充值金额为0

            //可提现金额=当前提现金额-扣除的充值金额
            rechargeAmount =  account.getWithdrawAmount().subtract(rechargeAmount);
            if(rechargeAmount.doubleValue()<0)
            {
                //数据异常。
                throw new IllegalArgumentException("余额不匹配。");
            }
            account.setWithdrawAmount(rechargeAmount);

            useWithdrawAmount = rechargeAmount;
        }
    }


    private void valid(PayOrder pay)
    {
        if(pay.getMemberId()<=0)
        {
            throw new IllegalArgumentException("会员不存在");
        }
        if(StringUtils.isEmpty(pay.getTradeId()))
        {
            throw new IllegalArgumentException("业务单号为空。");
        }
        if(StringUtils.isEmpty(pay.getTradeType()))
        {
            throw new IllegalArgumentException("业务类型为空。");
        }
        if(StringUtils.isEmpty(pay.getPayPlatform()))
        {
            throw new IllegalArgumentException("支付平台为空");
        }
        if(pay.getAccountAmount().doubleValue()<0 || pay.getPoints()<0 ||
                pay.getPayAmount().doubleValue()<0 || pay.getTotalAmount().doubleValue()<=0)
        {
            throw new IllegalArgumentException("支付金额不正确");
        }
        if (StringUtils.isEmpty(pay.getCallbackUrl()))
        {
            throw new IllegalArgumentException("callbackUrl is null");
        }
        if(StringUtils.isEmpty(pay.getCellPhone()))
        {
            throw new IllegalArgumentException("cellphone is null");
        }

    }

    public PayOrder findOne(String orderId)
    {
        if(StringUtils.isEmpty(orderId))
        {
            throw new IllegalArgumentException("支付订单号为空");
        }
        return  payOrderRepository.findOne(orderId);
    }

    public PayOrder findByTrade(String tradeType, String tradeId)
    {
        if(StringUtils.isEmpty(tradeId))
        {
            throw new IllegalArgumentException("业务单号为空。");
        }
        if(StringUtils.isEmpty(tradeType))
        {
            throw new IllegalArgumentException("业务类型为空。");
        }
        return  payOrderRepository.findBytradeTypeAndTradeId(tradeType,tradeId);
    }

    /// <summary>
    /// 接收到用户通过第三方平台支付成功，处理我们系统相关的业务,并发送消息供业务方处理。
    /// </summary>
    @Transactional
    public PayOrder paySuccess(String orderId, String platform, String notifyData)
    {
        if(StringUtils.isEmpty(orderId))
        {
            throw new IllegalArgumentException("支付订单号为空");
        }
        if(StringUtils.isEmpty(notifyData))
        {
            throw new IllegalArgumentException("第三方支付通知内容为空");
        }
        PayOrder pay = payOrderRepository.findOne(orderId);

        if(pay ==  null)
        {
            throw new IllegalArgumentException("请求的支付订单不存在");
        }
        if(pay.getPayTime() !=null )
        {
//            LogService.info(pay.getOrderId() + "重复请求该笔已处理过的支付订单。");
            return pay;
        }
        if(!StringUtils.isEmpty(platform))
        {
            pay.setPayPlatform(platform);  //更新支付平台
        }
        pay.setPayTime(new Date());
        pay.setNotifyData(notifyData); //写入第三方支付平台异步通知的数据

        MemberAccount account = memberAccountManager.findById(pay.getMemberId());
        String logContent = "{支付单号:" + pay.getOrderId() + ",交易Id:" + pay.getTradeId() + "，交易类型:" +
                pay.getTradeType() + "}";

        if ((pay.getAccountAmount().doubleValue() > 0 && account.getAvailableAmount().doubleValue()
                < pay.getAccountAmount().doubleValue())||
                (pay.getPoints()>0 && account.getPoints()<pay.getPoints())) // 余额或积分不足
        {
            // 关闭支付订单
            pay.setStatus(Constants.enumPayStatus.exception.getValue());

            payOrderRepository.save(pay); //更新支付记录

            /*
            //如使用线上第三方平台支付金额
            if (pay.getPayAmount().doubleValue() > 0) {
                // 充值，将支付金额追加到虚拟账户余额
                account.setAvailableAmount(account.getAvailableAmount().add(pay.getPayAmount()));
            }*/

            // 写余额异常日志
//            LogService.warning(logContent + "第三方支付平台付款成功，但余额不足无法继续交易。");

            //余额异常，通知业务方关闭订单，并生成退款流程（原路返回）。
            SendMessageQ(pay, Constants.enumPayStatus.exception);
        }
        else // 正常处理
        {
            // 更新支付为支付成功状态
            pay.setStatus(Constants.enumPayStatus.success.getValue());
            payOrderRepository.save(pay);

            // 使用虚拟余额或积分支付
            if (pay.getAccountAmount().doubleValue() > 0 || pay.getPoints().doubleValue() > 0)
            {
                BigDecimal useRechargeAmount = new BigDecimal(0);
                BigDecimal useWithdrawAmount = new BigDecimal(0);

                //可用余额减少
                calculateAmount(account,pay.getAccountAmount(),useRechargeAmount,useRechargeAmount);

                account.setPoints(account.getPoints()-pay.getPoints());
                memberAccountManager.save(account);

                memberAccountHistoryManager.add(account,pay.getAccountAmount(),useRechargeAmount,useWithdrawAmount,
                        pay.getPoints(),pay.getTradeType(),pay.getTradeId(),0,pay.getDescription());

            }

            Constants.logger.info(logContent + "支付成功。");

            SendMessageQ(pay, Constants.enumPayStatus.success);
        }

        return pay;
    }

    private void SendMessageQ(PayOrder pay, Constants.enumPayStatus payStatus)
    {
        MQPaySuccess mq = new MQPaySuccess();

        mq.setPay_time(DateUtils.format(pay.getPayTime()));
        mq.setPay_status(payStatus.getValue()==1 ? 200 :1000);
        mq.setPay_orderid(pay.getOrderId());
        mq.setPay_platform(pay.getPayPlatform());

        mq.setTrade_id(pay.getTradeId());
        mq.setTrade_type(pay.getTradeType());
        mq.setAccount_amount(pay.getAccountAmount());
        mq.setPay_amount(pay.getPayAmount());
        mq.setPoints(pay.getPoints());
        mq.setDiscount_amount(pay.getDiscountAmount());
        mq.setTotal_amount(pay.getTotalAmount());
        mq.setRemark("支付结果通知");
        mq.setNotify_url(pay.getNotifyUrl());

        messageService.send("mq_paysuccess","MQPaySuccess","com.fct.finance",
                JsonConverter.toJson(mq),"发送支付成功通知消息");
    }

    @Transactional
    public void tradeNotify(String json)
    {
        MQPayTrade result = JsonConverter.toObject(json,MQPayTrade.class);

        PayOrder pay = payOrderRepository.findOne(result.getPay_orderid());

        if(pay ==null || pay.getStatus() == Constants.enumPayStatus.fullrefund.getValue())
        {
            throw new IllegalArgumentException("pay_tradehandle: data is illegal");
        }

        /*  交易方如正常处理，则不写入交易通知，否则将处理 异常消息,进行退款处理。
        //交易完成,并却业务类型为消费
        if (result.getTrade_status() == 200)
        {
            MemberAccount account = memberAccountManager.findById(pay.getMemberId());
            //等比赠送积分
            Integer points = pay.getPayAmount().intValue();
            account.setPoints(account.getPoints()+points);
            account.setAccumulatePoints(account.getAccumulatePoints()+points);

            memberAccountManager.save(account);

            MemberAccountHistory history = new MemberAccountHistory();
            history.setTradeId(pay.getTradeId());
            history.setTradeType(pay.getTradeType());
            history.setMemberId(pay.getMemberId());
            history.setCellPhone(pay.getCellPhone());
            history.setAmount(new BigDecimal(0));
            history.setBalanceAmount(account.getAvailableAmount());
            history.setPoints(points);
            history.setBalancePoints(account.getPoints());
            history.setRemark("用户消费使用现金赠送同比积分");
            history.setBehaviorType(1); //收入
            memberAccountHistoryManager.Create(history);

        }*/

        //业务异常，发起退款请求,销售订单会有多个商品存在且多条退款记录
        if (result.getTrade_status() == 1000 && result.getRefund()!=null &&
                result.getRefund().size()>0)
        {
            for (MQPayRefund refund:result.getRefund()
                 ) {
                    //退款
                refundRecordManager.tradeException(refund,pay);
            }
            pay.setStatus(Constants.enumPayStatus.fullrefund.getValue());
            payOrderRepository.save(pay);

        }
    }

    private String getCondition(Integer memberId, String cellPhone, String orderId, String platform,String payOrderId,
                                String tradeId, String tradeType,Integer status,  Integer timeType,String beginTime,
                                String endTime,List<Object> param)
    {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(cellPhone)) {
            sb.append(" AND cellPhone=?");
            param.add(cellPhone);
        }
        if(memberId>0)
        {
            sb.append(" AND memberId="+memberId);
        }
        if (!StringUtils.isEmpty(platform)) {
            sb.append(" AND platform=?");
            param.add(platform);
        }
        if(status>-1)
        {
            sb.append(" AND status="+status);
        }
        if(!StringUtils.isEmpty(orderId))
        {
            sb.append(" AND orderId=?");
            param.add(orderId);
        }
        if(!StringUtils.isEmpty(payOrderId))
        {
            sb.append(" AND notifyData like ?");
            param.add("%"+ payOrderId +"%");
        }

        if (!StringUtils.isEmpty(tradeId)) {
            sb.append(" AND tradeId=?");
            param.add(tradeId);
        }

        if (!StringUtils.isEmpty(tradeType)) {
            sb.append(" AND tradeType=?");
            param.add(tradeType);
        }
        String time = "createTime";
        if(timeType ==1){
            time = "payTime";
        }
        if (!StringUtils.isEmpty(beginTime)) {
            sb.append(" AND "+time+">=?");
            param.add(beginTime);
        }
        if (!StringUtils.isEmpty(endTime)) {
            sb.append(" AND "+time+"<?");
            param.add(endTime);
        }
        return sb.toString();
    }

    public PageResponse<PayOrder> findAll(Integer memberId, String cellPhone, String orderId, String platform,String payOrderId,
                                          String tradeId, String tradeType,Integer status,  Integer timeType,String beginTime,
                                          String endTime, Integer pageIndex, Integer pageSize)
    {
        List<Object> param = new ArrayList<>();

        String table="PayOrder";
        String field ="*";
        String orderBy = "createTime Desc";
        String condition= getCondition(memberId,cellPhone,orderId,platform,payOrderId,tradeId,
                tradeType,status,timeType,beginTime,endTime,param);

        String sql = "SELECT Count(0) FROM PayOrder WHERE 1=1 "+condition;
        Integer count =  jt.queryForObject(sql,param.toArray(),Integer.class);

        sql = PageUtil.getPageSQL(table,field,condition,orderBy,pageIndex,pageSize);

        List<PayOrder> ls = jt.query(sql, param.toArray(), new BeanPropertyRowMapper<PayOrder>(PayOrder.class));

        int end = pageIndex+1;
        Boolean hasmore = true;
        if(pageIndex*pageSize >= count)
        {
            end = pageIndex;
            hasmore = false;
        }

        PageResponse<PayOrder> pageResponse = new PageResponse<>();
        pageResponse.setTotalCount(count);
        pageResponse.setCurrent(end);
        pageResponse.setElements(ls);
        pageResponse.setHasMore(hasmore);

        return pageResponse;
    }
}
