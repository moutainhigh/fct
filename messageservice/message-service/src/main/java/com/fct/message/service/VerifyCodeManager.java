package com.fct.message.service;

import com.fct.core.utils.DateUtils;
import com.fct.core.utils.StringHelper;
import com.fct.message.data.entity.VerifyCode;
import com.fct.message.data.repository.VerifyCodeRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by jon on 2017/5/6.
 * jon love nancy
 */
@Service
public class VerifyCodeManager {

    @Autowired
    private VerifyCodeRepository verifyCodeRepository;

    @Autowired
    private JdbcTemplate jt;

    @Transactional
    public String create(String sessionId,String cellPhone)
    {
        if(StringUtils.isEmpty(sessionId))
        {
            throw new IllegalArgumentException("sessionId为空。");
        }

        List<Object> param = new ArrayList<>();
        //校验是否短信获取太频繁
        String endtime = DateUtils.formatDate(DateUtils.addDay(new Date(),1),"yyyy-MM-dd");

        String sql = "SELECT Count(0) FROM VerifyCode WHERE CellPhone=? AND CreateTime>=? AND CreateTime<?";
        param.add(cellPhone);
        param.add(DateUtils.format(new Date()));
        param.add(endtime);

        if(jt.queryForObject(sql,Integer.class,param.toArray())>30)
        {
            throw new IllegalArgumentException("当天接收验证码次数超过限制!");
        }
        param = new ArrayList<>();
        sql = "SELECT Count(0) FROM VerifyCode WHERE CellPhone=? AND SessionId=?";

        param.add(cellPhone);
        param.add(sessionId);

        if(jt.queryForObject(sql,Integer.class,param.toArray())>5)
        {
            throw new IllegalArgumentException("当天接收验证码次数超过限制!");
        }

        sql = "SELECT * FROM VerifyCode WHERE cellPhone=? AND sessionId=? AND expireTime>=? limit 1";
        //sql = "SELECT * FROM VerifyCode limit 1";
        param.add(DateUtils.format(new Date()));

        VerifyCode vc = null;
        try {
            vc = jt.queryForObject(sql, new BeanPropertyRowMapper<VerifyCode>(VerifyCode.class), param.toArray());
        }
        catch (Exception exp)
        {

        }

        if(vc!=null) {
            //同一个sessionId和手机号码.1分钟类只能提交1次,
            if (DateUtils.minutesBetween(vc.getCreateTime(),new Date()) <= 1) {
                throw new IllegalArgumentException("请稍等1分钟后再试。");
            }
            return vc.getCode();
            /*
            //将sessionId和手机号码相同的数据过期时间设置为过期.
            vc.setExpireTime(DateUtils.addDay(new Date(), -1)); //过期
            verifyCodeRepository.saveAndFlush(vc);*/
        }
        else
        {
            vc = new VerifyCode();
            vc.setSessionId(sessionId);
            vc.setCellPhone(cellPhone);
            vc.setCode(StringHelper.getRandomNumber(6));
            vc.setExpireTime(DateUtils.addMinute(new Date(),5));
            vc.setCreateTime(new Date());

            verifyCodeRepository.save(vc);
        }
        return vc.getCode();
    }

    public int check(String sessionId,String cellPhone,String code)
    {
        List<Object> param = new ArrayList<>();
        String sql = "SELECT count(0) FROM VerifyCode WHERE SessionId=? and cellphone=? AND Code=? AND expireTime>=? limit 1";
        param.add(sessionId);
        param.add(cellPhone);
        param.add(code);
        param.add(DateUtils.format(new Date()));
        return jt.queryForObject(sql,Integer.class,param.toArray());
    }



}
