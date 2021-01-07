package com.barrett.transactional.util.service;

import com.barrett.transactional.dao.SaveDataMapper;
import com.barrett.transactional.domain.UserInfo;
import com.barrett.transactional.util.threadTransaction.TransactionInfo;
import com.barrett.transactional.util.threadTransaction.TransactionalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 示例：主线程依赖子线程的结果
 *
 * @Author barrett
 * @Date 2020/10/26 09:50
 **/
@Slf4j
@Service
public class ChildTaskService extends TransactionalException implements ChildTask {
    
    @Autowired
    private SaveDataMapper dataMapper;

    /**
     *
     * @Param t 入参
     * @author created by barrett in 2021/1/6 17:12
     **/
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public <T> void execute(TransactionInfo transactionInfo, T t) throws Exception {
        Exception ex = null;
        try {
            //TODO 子业务 === start
            UserInfo mainUser = (UserInfo)t;
            UserInfo userInfo = new UserInfo();
            userInfo.setName(userInfo.getName()+";一"+UUID.randomUUID().toString());
            userInfo.setSex(mainUser.getSex());
            dataMapper.saveUser(userInfo);

            Thread.sleep(3000);
            if (mainUser.getSex() == 1)//测试异常
                System.out.println(1 / 0);
            String result = "子线程结果集";

            //TODO 子业务 === end
            transactionInfo.getThreadResult().put(transactionInfo.getSuccessResult(result));
        } catch (Exception e) {
            e.printStackTrace();
            transactionInfo.getThreadResult().put(transactionInfo.getFailResult("抛出具体异常信息"));
            ex = e;
        } finally {
            // 执行完成 等待主线程通知回滚
            handleException(transactionInfo, ex);
        }
    }
}
