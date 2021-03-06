package com.fct.member.interfaces;


import com.fct.member.data.entity.*;
import org.springframework.data.domain.Page;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

/**
 * Created by jon on 2017/5/2.
 */
public interface MemberService {

    /*注册会员*/
    Member registerMember(String cellPhone, String userName, String password);

    /*普通登录和快捷登录*/
    MemberLogin loginMember(String cellPhone,String password,String platform,String ip,Integer expireDay);

    void logoutMember(String token);

    Member getMember(Integer memberId);

    MemberLogin getMemberLogin(String token);

    MemberDTO getMemberDTO(Integer memberId);

    /*修改密码*/
    void updateMemberPassword(Integer memberId,String oldPassword,String newPassword,String reNewPassword);

    void forgetPassword(String cellPhone,String password);

    void lockMember(Integer memberId);

    PageResponse<Member> findMember(String cellPhone,Integer authStatus,String beginTime,String endTime,Integer pageIndex,Integer pageSize);  //分页按条件查询

    void updateMemberInfo(String token,Integer memberId,String headPortrait,String username,Integer sex,String birthday,String weixin);

    MemberInfo getMemberInfo(Integer memberId);

    void saveMemberAddress(MemberAddress address);

    MemberAddress getMemberAddress(Integer id);

    List<MemberAddress> findMemberAddress(Integer memberId);

    MemberAddress getDefaultAddress(Integer memberId);

    void deleteAddress(Integer id, Integer memberId);

    void setDefaultAddress(Integer id,Integer memberId);

    void authenticationMember(String token,Integer memberId,String name,String identityCardNo,String identityCardImg,
                              String bankName,String bankAccount);

    void verifyAuthentication(Integer memberId,Integer authStatus);

    void saveMemberBankInfo(MemberBankInfo bankInfo);

    MemberBankInfo getMemberBankInfo(Integer id);

    MemberBankInfo getMemberBankInfoByMember(Integer memberId);

    PageResponse<MemberBankInfo> findMemberBankInfo(Integer memberId,String cellPhone,String bankName,Integer status,Integer pageIndex,Integer pageSize);

    void createInviteCode(Integer memberId);

    void addInviteCodeCount(Integer memberId,Integer count);

    PageResponse<InviteCode>  findInviteCode(String code,Integer ownerId, String ownerCellPhone, String toCellphone,
                                             int pageIndex, int pageSize);

    MemberLogin saveMemberAuth(Integer memberId,String openId,String platform,String nickName,String headImgUrl,
                              String unionId,Integer sex,String ip,Integer expireDay);

    MemberLogin bindMemberAuth(String cellPhone,String platform,String openId, String nickName,String headImgUrl,
                              String unionId,Integer sex,String ip,Integer expireDay);

    MemberAuth getMemberAuth(String platform);

    MemberStore applyStore(Integer memberId,String inviteCode);

    void updateStoreStatus(Integer id);

    MemberStore getMemberStore(Integer storeId);

    PageResponse<MemberStore> findMemberStore(String cellPhone,Integer status,Integer pageIndex,Integer pageSize);

    void createSystemUser(SystemUser user);

    SysUserLogin loginSystemUser(String userName,String password,String ip,Integer expireHour);

    SysUserLogin getSysUserLogin(String token);

    SystemUser getSystemUser(String cellPhone);

    void logoutSysUser(String token);

    void lockSystemUser(Integer userId);

    void updateSystemUserPassword(Integer memberId,String oldPassword,String newPassword,String reNewPassword);

    PageResponse<SystemUser> findSystemUser(String userName, Integer pageIndex, Integer pageSize);

    void saveFavourite(Integer memberId,Integer favType,Integer relatedId);

    void deleteFavourite(Integer memberId,Integer favType,Integer relatedId);

    int getFavouriteCount(Integer memberId,Integer favType,Integer relatedId);

    PageResponse<MemberFavourite> findFavourite(Integer memberId,Integer favType,
                                                Integer pageIndex, Integer pageSize);

}
