package com.barrett.transactional.util.threadTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //TODO
 * @Author barrett
 * @Date 2020/10/26 09:51
 **/
public abstract class TransactionalException {
    private Logger logger = LoggerFactory.getLogger(TransactionalException.class);

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
