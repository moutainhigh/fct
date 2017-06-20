package com.fct.member.service.business;

import com.fct.common.utils.DateUtils;
import com.fct.common.utils.PageUtil;
import com.fct.member.data.entity.InviteCode;
import com.fct.member.data.entity.Member;
import com.fct.member.data.entity.MemberStore;
import com.fct.member.data.repository.MemberStoreRepository;
import com.fct.member.interfaces.PageResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.jws.Oneway;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jon on 2017/5/7.
 */
@Service
public class MemberStoreManager {

    @Autowired
    private MemberStoreRepository memberStoreRepository;

    @Autowired
    private InviteCodeManager inviteCodeManager;

    @Autowired
    private MemberManager memberManager;

    @Autowired
    private JdbcTemplate jt;

    public MemberStore findByMemberId(Integer memberId) {
        return memberStoreRepository.findByMemberId(memberId);
    }

    @Transient
    public MemberStore apply(Integer memberId, String inviteCode) {
        InviteCode code = inviteCodeManager.findByCode(inviteCode);

        if (DateUtils.compareDate(new Date(), code.getExpireTime()) > 0 || code.getStatus() != 0) {
            throw new IllegalArgumentException("邀请码无效。");
        }
        Member member = memberManager.findById(memberId);

        if (memberStoreRepository.findByMemberId(memberId) != null) {
            throw new IllegalArgumentException("已有店铺存在。");
        }

        code.setStatus(1);//已使用
        code.setToMemberId(memberId);
        code.setToCellPhone(member.getCellPhone());
        code.setUseTime(new Date());
        inviteCodeManager.save(code);

        MemberStore ms = new MemberStore();
        ms.setMemberId(memberId);
        ms.setCreateTime(new Date());
        ms.setCellPhone(member.getCellPhone());
        ms.setStatus(0);
        memberStoreRepository.save(ms);

        return ms;
    }

    public void updateStatus(Integer id)
    {
        memberStoreRepository.updateStatus(id);
    }

    private String getCondition(String cellPhone, Integer status, List<Object> param)
    {
        String condition ="";
        if(!StringUtils.isEmpty(cellPhone))
        {
            condition+=" AND cellPhone=? OR id="+cellPhone;
            param.add(cellPhone);
        }
        if(status>-1)
        {
            condition += " AND status="+status;
        }
        return condition;
    }

    public PageResponse<MemberStore> findAll(String cellPhone, Integer status, Integer pageIndex, Integer pageSize)
    {
        List<Object> param = new ArrayList<>();

        String table="MemberStore";
        String field ="*";
        String orderBy = "Id asc";
        String condition= getCondition(cellPhone,status,param);

        String sql = "SELECT Count(0) FROM MemberStore WHERE 1=1 "+condition;
        Integer count =  jt.queryForObject(sql,param.toArray(),Integer.class);

        sql = PageUtil.getPageSQL(table,field,condition,orderBy,pageIndex,pageSize);

        List<MemberStore> ls = jt.query(sql, param.toArray(), new BeanPropertyRowMapper<MemberStore>(MemberStore.class));

        int end = pageIndex+1;
        Boolean hasmore = true;
        if(pageIndex*pageSize >= count)
        {
            end = pageIndex;
            hasmore = false;
        }
        PageResponse<MemberStore> p = new PageResponse<>();
        p.setTotalCount(count);
        p.setCurrent(end);
        p.setElements(ls);
        p.setHasMore(hasmore);

        return p;
    }
}
