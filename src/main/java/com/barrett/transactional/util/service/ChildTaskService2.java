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
 * 示例：不需要使用子线程的结果
 *
 * @Author barrett
 * @Date 2020/10/26 09:50
 **/
@Slf4j
@Service
public class ChildTaskService2 extends TransactionalException implements ChildTask {
    @Autowired
    private SaveDataMapper dataMapper;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public <T> void execute(TransactionInfo transactionInfo, T t) throws Exception {
        Exception ex = null;
        try {
            int size = (Integer) t;
            //子业务
            UserInfo userInfo = new UserInfo();
            userInfo.setName("二" + UUID.randomUUID().toString());
            userInfo.setSex(size);
            dataMapper.saveUser(userInfo);
            String result = "123123";

            if (size == 2)
                System.out.println(1 / 0);

            transactionInfo.getThreadResult().put(transactionInfo.getSuccessResult(result));
        } catch (Exception e) {
            transactionInfo.getThreadResult().put(transactionInfo.getFailResult("抛出具体异常信息"));
            ex = e;
            e.printStackTrace();
        } finally {
            // 执行完成 等待主线程通知回滚
            handleException(transactionInfo, ex);
        }
    }
}
