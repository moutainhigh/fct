package com.fct.member.service.business;

import com.fct.core.exceptions.BaseException;
import com.fct.core.utils.DateUtils;
import com.fct.core.utils.UUIDUtil;
import com.fct.member.data.entity.*;
import com.fct.member.data.repository.MemberInfoRepository;
import com.fct.member.interfaces.MemberDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by jon on 2017/5/7.
 */
@Service
public class MemberInfoManager {

    @Autowired
    private MemberInfoRepository memberInfoRepository;

    @Autowired
    private MemberManager memberManager;

    @Autowired
    private MemberBankInfoManager memberBankInfoManager;

    @Autowired
    private MemberStoreManager memberStoreManager;

    @Autowired
    private MemberLoginManager memberLoginManager;

    public void save(MemberInfo info)
    {
        if(info.getMemberId()<=0)
        {
            throw new IllegalArgumentException("会员不存在");
        }
        if(info.getSex() ==null)
        {
            info.setSex(0);
        }
        memberInfoRepository.save(info);
    }

    @Transactional
    public void updateInfo(String token,Integer memberId,String headPortrait,String userName,Integer sex,String birthday,String weixin)
    {
        if(memberId<=0)
        {
            throw new IllegalArgumentException("会员id为空");
        }

        Member member = memberManager.findById(memberId);

        if(member == null)
        {
            throw new IllegalArgumentException("会员不存在");
        }

        if(!StringUtils.isEmpty(userName) && !member.getUserName().equals(userName))
        {
            if(memberManager.countByUserName(userName)>0) {
                throw new IllegalArgumentException("用户昵称已存在");
            }
            member.setUserName(userName);
            memberManager.save(member);
        }

        MemberInfo info = memberInfoRepository.findOne(memberId);
        if(!StringUtils.isEmpty(headPortrait)) {
            info.setHeadPortrait(headPortrait);
        }
        info.setSex(sex>0 ? 1 :0);
        if(!StringUtils.isEmpty(birthday)) {
            info.setBirthday(DateUtils.parseString(birthday, "yyyy-MM-dd"));
        }
        info.setWeixin(weixin);
        memberInfoRepository.save(info);

        if(!StringUtils.isEmpty(token)) {
            memberLoginManager.updateLogin(token, member, info);
        }

    }

    public MemberInfo findById(Integer memberId)
    {
        if(memberId<=0)
        {
            throw new IllegalArgumentException("会员不存在");
        }
        return memberInfoRepository.findOne(memberId);
    }

    @Transactional
    public void authentication(String token,Integer memberId,String name,String identityCardNo,String identityCardImg,
                                     String bankName,String bankAccount)
    {
        if(memberId<=0)
        {
            throw new IllegalArgumentException("会员不存在");
        }
        if(StringUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("姓名为空");
        }
        if(StringUtils.isEmpty(identityCardNo))
        {
            throw new IllegalArgumentException("身份证号码");
        }
        if(StringUtils.isEmpty(identityCardImg))
        {
            throw new IllegalArgumentException("身份证照片");
        }
        if(StringUtils.isEmpty(bankName))
        {
            throw new IllegalArgumentException("银行名称为空");
        }
        if(StringUtils.isEmpty(bankAccount))
        {
            throw new IllegalArgumentException("银行卡号");
        }

        Member member = memberManager.findById(memberId);

        if(member.getAuthStatus() !=0)
        {
            throw new BaseException("已认证或审核中");
        }
        member.setAuthStatus(1);//待审核认证
        memberManager.save(member);

        MemberInfo info = memberInfoRepository.findOne(memberId);
        info.setRealName(name);
        info.setIdentityCardImg(identityCardImg);
        info.setIdentityCardNo(identityCardNo);
        info.setBirthday(DateUtils.parseString(info.getIdentityCardNo().substring(6,14),"yyyyMMdd"));
        memberInfoRepository.save(info);

        MemberBankInfo bank = memberBankInfoManager.findOne(memberId);
        if(bank == null)
        {
            bank = new MemberBankInfo();
        }
        bank.setMemberId(memberId);
        bank.setCellPhone(member.getCellPhone());
        bank.setName(name);
        bank.setStatus(0);
        bank.setBankName(bankName);
        bank.setBankAccount(bankAccount);
        memberBankInfoManager.save(bank);

        if(!StringUtils.isEmpty(token)) {
            memberLoginManager.updateLogin(token, member, info);
        }

    }

    public MemberDTO findByMemberId(Integer id)
    {
        if(id <=0)
        {
            throw new IllegalArgumentException("用户不存在。");
        }
        Member member = memberManager.findById(id);

        MemberInfo info = findById(member.getId());

        MemberStore store = memberStoreManager.findByMemberId(member.getId());

        String cellphone = member.getCellPhone().substring(0,3);
        cellphone +=  "****" + member.getCellPhone().substring(7);
        MemberDTO login = new MemberDTO();
        login.setCellPhone(cellphone);
        login.setMemberId(member.getId());
        login.setHeadPortrait(info.getHeadPortrait());
        login.setAuthStatus(member.getAuthStatus());
        login.setUserName(StringUtils.isEmpty(member.getUserName()) ?cellphone : member.getUserName() );
        login.setShopId(store!=null ?store.getId() :0);
        login.setGradeId(member.getGradeId());

        return login;
    }
}
