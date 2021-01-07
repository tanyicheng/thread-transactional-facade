package com.barrett.transactional.util.service;

import com.barrett.transactional.util.threadTransaction.TransactionInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * //TODO 子线程任务
 * @Author barrett
 * @Date 2020/10/26 09:49
 **/
@Slf4j
public class HandleChildTask implements Runnable {
        private TransactionInfo transactionInfo;
        private Object obj;
        private ChildTask ct;

        /**
         * @Param obj 入参
         * @author created by barrett in 2021/1/6 17:13
         **/
        public HandleChildTask(TransactionInfo transactionInfo, Object obj,ChildTask ct) {
            this.transactionInfo = transactionInfo;
            this.obj=obj;
            this.ct=ct;
        }

        @Override
        public void run() {
            try {
                log.info("HandleChildTask start 子线程: {}", Thread.currentThread().getName());
                //可以切换不同的数据源操作
                ct.execute(transactionInfo, obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }