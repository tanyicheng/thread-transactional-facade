package com.barrett.transactional.util.service;

import com.barrett.transactional.util.threadTransaction.RollBack;
import com.barrett.transactional.util.threadTransaction.ThreadTransactionalFacade;
import com.barrett.transactional.util.threadTransaction.TransactionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 公共的抽象类
 *
 * @author created by barrett in 2021/1/4 14:28
 **/
@Slf4j
public abstract class AbsTaskService extends ThreadTransactionalFacade {

    public abstract <T> void mainTask(T t) throws Exception;

    /**
     * 传递参数
     *
     * @param threadNum 开启子线程数，必须一一对应
     * @author created by barrett in 2021/1/4 15:53
     **/
    @Transactional
    public <T> String handleTask(T obj, int threadNum) {
        //初始化
        init(threadNum);
        log.info("主线程: {}", Thread.currentThread().getName());

        String message = "操作成功";
        try {
            mainTask(obj);

            //判断子线程是否成功
            if (threadLatch != null) {
                if (wait(threadLatch, threadResult, rollBack)) {
                    //回滚主线程
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    message="失败，事务回滚";
                }
            }

        } catch (Exception e) {
            //主线程发生异常回滚子线程
            rollBack.setIsRollBack(true);
            //回滚主线程
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            message="失败，事务回滚";
        } finally {
            mainLatch.countDown();
        }
        return message;
    }

    /**
     * 设置等待100秒钟，等待子线程执行完毕
     *
     * @waitTime 为0则不设等待时间
     * @Author barrett
     * @Date 2020/10/23 11:28
     **/
    public boolean wait(CountDownLatch threadLatch, BlockingDeque<Map<String, Object>> threadResult, RollBack rollBack) throws Exception {
        // 等待子线程执行完毕,默认100秒，超时直接回滚
        boolean await = threadLatch.await(100, TimeUnit.SECONDS);
        //返回给主线程是否需要回滚
        boolean flag = false;
        if (await) {
            while (threadResult.size() > 0) {
                Map<String, Object> result = threadResult.take();
                if (!ThreadTransactionalFacade.thread_result_succ.equals(result.get(TransactionInfo.result).toString())) {
                    flag = true;
                    rollBack.setIsRollBack(true);
                    log.error("子线程抛出异常信息：" + result.get(TransactionInfo.obj).toString());
                    break;
                }
            }
        } else {
            // 超时 直接回滚
            rollBack.setIsRollBack(true);
        }
        return flag;
    }
}
