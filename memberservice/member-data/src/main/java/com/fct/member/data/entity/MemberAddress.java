package com.fct.member.data.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by jon on 2017/5/1.
 */
@Entity
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberAddress {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer Id;

    /// <summary>
    /// 会员Id
    /// </summary>
    private Integer memberId;

    /// <summary>
    /// 收货人手机号码
    /// </summary>
    private String cellPhone;

    /// <summary>
    /// 省份
    /// </summary>
    private String province;

    /// <summary>
    /// 城市
    /// </summary>
    private String cityId;

    /// <summary>
    /// 城镇
    /// </summary>
    private String townId;

    /// <summary>
    /// 地址
    /// </summary>
    private String address;

    /// <summary>
    /// 邮编
    /// </summary>
    private String postCode;

    /// <summary>
    /// 是否默认
    /// </summary>
    private Integer isDefault;

    /// <summary>
    /// 创建时间
    /// </summary>
    private Date createTime;
}
