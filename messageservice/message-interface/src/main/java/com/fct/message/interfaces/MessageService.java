package com.fct.message.interfaces;

import com.fct.message.data.entity.MessageQueue;

import java.util.List;

/**
 * Created by jon on 2017/4/11.
 */
public interface MessageService {

    /// <summary>
    /// 保存消息
    /// </summary>
   void create(MessageQueue message);

   void send(String typeId, String targetModule, String sourceAppName, String jsonBody, String remark);

    /// <summary>
    /// 更新消息置位
    /// </summary>
    void complete(Integer id);

    /// <summary>
    /// 请求处理失败
    /// </summary>
    /// <returns></returns>
    void fail(Integer id, String message);

    /// <summary>
    /// 恢复消息
    /// </summary>
    /// <returns></returns>
    void resume(Integer id);

    /// <summary>
    /// 根据模块获取消息体
    /// </summary>
    List<MessageQueue> find(String typeId);

    MessageQueue getMessage(Integer messageId);

    /// <summary>
    /// 获取异常消息
    /// </summary>
    /// <returns></returns>
    PageResponse<MessageQueue> findAll(String typeId, Integer status, String body,Integer pageIndex, Integer pageSize);

    void sendSMS(String cellPhone,String content,String ip,String action);

    String getVerifyCode(String sessionId,String cellPhone);

    void sendVerifyCode(String sessionId,String cellPhone,String content,String ip,String action);

    int checkVerifyCode(String sessionId,String cellPhone,String code);
}
