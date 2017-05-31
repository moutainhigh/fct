package com.fct.member.interfaces;


import com.fct.member.data.entity.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Created by jon on 2017/5/2.
 */
public interface MemberService {

    /*注册会员*/
    Member registerMember(String cellPhone, String userName, String password);

    /*普通登录和快捷登录*/
    MemberLogin loginMember(String cellPhone,String password,String ip,Integer expireDay);

    Member getMember(Integer memberId);

    /*修改密码*/
    void updateMemberPassword(Integer memberId,String oldPassword,String newPassword,String reNewPassword);

    void forgetPassword(String cellPhone,String password);

    void lockMember(Integer memberId);

    Page<Member> findMember(String cellPhone,String beginTime,String endTime,Integer pageIndex,Integer pageSize);  //分页按条件查询

    void updateMemberInfo(MemberInfo info);

    MemberInfo getMemberInfo(Integer memberId);

    void saveMemberAddress(MemberAddress address);

    MemberAddress getMemberAddress(Integer id);

    List<MemberAddress> findMemberAddress(Integer memberId);

    void authenticationMember(Integer memberId,String name,String identityCardNo,String identityCardImg,
                              String bankName,String bankAccount);

    void verifyAuthentication(Integer memberId);

    void saveMemberBankInfo(MemberBankInfo bankInfo);

    MemberBankInfo getMemberBankInfo(Integer id);

    Page<MemberBankInfo> findMemberBankInfo(String cellPhone,String name,Integer status,Integer pageIndex,Integer pageSize);

    void createInviteCode(Integer memberId);

    void addInviteCodeCount(Integer memberId,Integer count);

    Page<InviteCode>  findInviteCode(Integer ownerId, String ownerCellPhone, int pageIndex, int pageSize);

    MemberAuth saveMemberAuth(MemberAuth auth);

    MemberAuth getMemberAuth(String platform);

    MemberStore applyStore(Integer memberId,String inviteCode);

    Page<MemberStore> findMemberStore(String cellPhone,Integer status,Integer pageIndex,Integer pageSize);

    void createSystemUser(SystemUser user);

    SystemUser loginSystemUser(String userName,String password);

    void lockSystemUser(Integer userId);

    void updateSystemUserPassword(Integer memberId,String oldPassword,String newPassword,String reNewPassword);

    Page<SystemUser> findSystemUser(String userName, Integer pageIndex, Integer pageSize);

}