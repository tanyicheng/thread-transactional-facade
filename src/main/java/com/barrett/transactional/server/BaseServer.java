package com.barrett.transactional.server;

import com.barrett.transactional.domain.TransactionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cmd
 * @data 2020/4/10 19:27
 */
public abstract class BaseServer {
    private Logger logger = LoggerFactory.getLogger(BaseServer.class);

    protected void handleException(TransactionInfo transactionInfo, Exception ex) throws Exception {
        if (!transactionInfo.isMultiThreading()) {
            if (ex != null) {
                throw ex;
            }
            return;
        }
        // 执行完成 等待主线程通知回滚
        transactionInfo.getThreadLatch().countDown();
        // 等待主线程通知
        transactionInfo.getMainLatch().await();
        if (transactionInfo.getRollBack().getIsRollBack()) {
            throw ex == null ? new RuntimeException("主线程出现异常，需要连带回滚") : ex;
        }
    }
}
