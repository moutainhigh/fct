package com.fct.message.service;

import com.fct.core.utils.PageUtil;
import com.fct.message.data.entity.MessageQueue;
import com.fct.message.data.repository.MessageQueueRepository;
import com.fct.message.interfaces.MessageService;
import com.fct.message.interfaces.PageResponse;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jon on 2017/4/11.
 */
@Service(value = "messageService")
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageQueueRepository messageQueueRepository;

    @Autowired
    private VerifyCodeManager verifyCodeManager;

    @Autowired
    private SMSRecordManager smsRecordManager;

    @Autowired
    private JdbcTemplate jt;

    public void create(MessageQueue message) {

        try {
            message.setStatus(0);
            message.setRequestCount(0);
            message.setCreateTime(new Date());
            messageQueueRepository.save(message);
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
    }
    public MessageQueue getMessage(Integer messageId)
    {
        if(messageId<=0)
        {
            throw new IllegalArgumentException("消息id不存在");
        }
        try {
            return messageQueueRepository.findOne(messageId);
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
        return  null;
    }

    public void send(String typeId,String targetModule,String sourceAppName,String jsonBody,String remark)
    {
        if(StringUtils.isEmpty(typeId))
        {
            throw new IllegalArgumentException("消息类型为空");
        }
        if(StringUtils.isEmpty(targetModule))
        {
            throw new IllegalArgumentException("消息模型为空.");
        }
        if(StringUtils.isEmpty(jsonBody))
        {
            throw new IllegalArgumentException("消息体为空。");
        }

        try {
            MessageQueue message = new MessageQueue();
            message.setTypeId(typeId);
            message.setTargetModule(targetModule);
            message.setSourceAppName(sourceAppName);
            message.setBody(jsonBody);
            message.setRemark(remark);
            message.setStatus(0);
            message.setRequestCount(0);
            message.setCreateTime(new Date());

            messageQueueRepository.save(message);
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
    }

    public void complete(Integer id) {

        if(id<=0)
        {
            throw new IllegalArgumentException("消息id不存在");
        }
        try {
            MessageQueue messageQueue = messageQueueRepository.findOne(id);
            messageQueue.setStatus(1);
            messageQueue.setRequestCount(1);
            messageQueue.setProcessTime(new Date());
            messageQueueRepository.save(messageQueue);
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
    }

    public void fail(Integer id, String message) {
        if(id<=0)
        {
            throw new IllegalArgumentException("消息id不存在");
        }
        try {
            MessageQueue msg = messageQueueRepository.findOne(id);
            if(msg.getRequestCount()>=3)
            {
                msg.setStatus(-1);  //异常不在处理。
            }
            msg.setRequestCount(msg.getRequestCount()+1);
            msg.setFailMessage(message);
            msg.setProcessTime(new Date());
            messageQueueRepository.saveAndFlush(msg);
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
    }

    public void resume(Integer id) {
        if(id<=0)
        {
            throw new IllegalArgumentException("消息id不存在");
        }
        try {
            messageQueueRepository.resume(id);
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
    }

    public List<MessageQueue> find(String typeId) {

        if(StringUtils.isEmpty(typeId))
        {
            throw new IllegalArgumentException("消息类型不存在");
        }
        try {
            return messageQueueRepository.findByTypeId(typeId);
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
        return null;
    }

    public PageResponse<MessageQueue> findAll(String typeId, Integer status,String body, Integer pageIndex, Integer pageSize) {

        try {
            List<Object> param = new ArrayList<>();
            String condition = "";
            if (!StringUtils.isEmpty(typeId)) {
                condition += " AND typeId=?";
                param.add(typeId);
            }
            if (status > -2) {
                condition += " AND status=" + status;
            }
            if (!StringUtils.isEmpty(body)) {
                condition += " AND body like ?";
                param.add("%" + body + "%");
            }

            String table = "MessageQueue";
            String field = "*";
            String orderBy = "Id Desc";

            String sql = "SELECT Count(0) FROM MessageQueue WHERE 1=1 " + condition;
            Integer count = jt.queryForObject(sql, param.toArray(), Integer.class);

            sql = PageUtil.getPageSQL(table, field, condition, orderBy, pageIndex, pageSize);

            List<MessageQueue> ls = jt.query(sql, param.toArray(), new BeanPropertyRowMapper<MessageQueue>(MessageQueue.class));

            int end = pageIndex + 1;
            Boolean hasmore = true;
            if (pageIndex * pageSize >= count) {
                end = pageIndex;
                hasmore = false;
            }

            PageResponse<MessageQueue> pageResponse = new PageResponse<>();
            pageResponse.setTotalCount(count);
            pageResponse.setCurrent(end);
            pageResponse.setElements(ls);
            pageResponse.setHasMore(hasmore);

            return pageResponse;
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
        return null;
    }

    public void sendSMS(String cellPhone,String content,String ip,String action)
    {
        try {
            smsRecordManager.send(cellPhone,content,ip,action);
        }
        catch (IllegalArgumentException exp)
        {
            throw exp;
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
    }

    public void sendVerifyCode(String sessionId,String cellPhone,String content,String ip,String action)
    {
        try {
            String code = verifyCodeManager.create(sessionId,cellPhone);

            content = content.replace("{code}",code);

            smsRecordManager.send(cellPhone,content,ip,action);
        }
        catch (IllegalArgumentException exp)
        {
            throw exp;
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
    }

    public String getVerifyCode(String sessionId,String cellPhone)
    {
        try {
            return verifyCodeManager.create(sessionId,cellPhone);
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
        return "";
    }

    public int checkVerifyCode(String sessionId,String cellPhone,String code)
    {
        try {
            return verifyCodeManager.check(sessionId,cellPhone,code);
        }
        catch (IllegalArgumentException exp)
        {
            throw exp;
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
        return 0;
    }
}
