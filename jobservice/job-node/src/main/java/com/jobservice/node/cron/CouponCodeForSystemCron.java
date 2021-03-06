package com.jobservice.node.cron;

import com.jobservice.node.ShardConstant;
import com.jobservice.node.interfaces.JobHandler;
import com.jobservice.task.JobTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CouponCodeForSystemCron implements InitializingBean {

    @Autowired
    private JobHandler jobHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        JobTask jobTask = new JobTask();
        Map<String, Object> params = new HashMap<>();
        params.putIfAbsent(ShardConstant.SHARD_VALUE, "couponCodeForSystem");
        jobTask.setParams(params);
        jobTask.setTaskId("couponcode-forsystem_generate");
        jobTask.setCronExpression("0 */5 * * * ?");    //每隔5分钟执行一次
        jobHandler.raiseJob(jobTask);
    }
}
