package com.fct.member.data.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by jon on 2017/5/1.
 */
@Entity
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class MemberInfo implements Serializable{
    @Id
    private Integer memberId;

    private String headPortrait;

    private String realName;

    private Integer sex;

    private String weixin;

    private Date birthday;

    private String identityCardNo;

    private String identityCardImg;

}
