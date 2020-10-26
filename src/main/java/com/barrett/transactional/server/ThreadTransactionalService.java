package com.barrett.transactional.server;

import com.barrett.transactional.dao.SaveDataMapper;
import com.google.common.collect.Lists;
import com.barrett.transactional.domain.RollBack;
import com.barrett.transactional.domain.TransactionInfo;
import com.barrett.transactional.domain.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程事务管理器[通过线程通信解决多线程事务提交问题]
 * 注：
 * 1.多线程入库仅支持同一种操作:比如单insert/单update,由于数据库的事务隔离级别,不能对同一条数据同时insert和update,会导致死锁
 * 2.如果非要使用多线程同时insert和update,则需要把事务隔离级别降为READ_COMMITTED,但是会出现先update导致失败问题(注解不存在)
 * 3.MySQL默认事务隔离级别:REPEATABLE_READ
 *
 * @author cmd
 * @data 2020/4/10 19:10
 */
@Slf4j
@Service
public class ThreadTransactionalService {

    //根据操作系统获取可用的线程数（不完全可信）
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2 + 1;
    //拆分下限
    private final int batchSize = 2;
    public final static String thread_result_err = "err";
    public final static String thread_result_succ = "succ";

    //创建定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    @Autowired
    private InsertDataService insertDataService;
    @Autowired
    private SaveDataMapper saveDataMapper;

    /**
     * //TODO 事务控制
     * @Author barrett
     * @Date 2020/10/23 09:18
     **/
    @Transactional
    public void handleTransaction(Integer size) {

        System.out.println("可用线程数："+THREAD_COUNT);
        // 主线程监控
        CountDownLatch mainLatch = new CountDownLatch(1);
        // 子线程监控
        CountDownLatch threadLatch = null;
        // 事务共享管理器
        RollBack rollBack = new RollBack(false);
        BlockingDeque<String> threadResult = new LinkedBlockingDeque<>(THREAD_COUNT);
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setThreadResult(threadResult);
        transactionInfo.setRollBack(rollBack);
        transactionInfo.setMainLatch(mainLatch);

        try {
            // 拆分大小为batchSize一个存储
            transactionInfo.setMultiThreading(true);
            threadLatch = new CountDownLatch(1);
            transactionInfo.setThreadLatch(threadLatch);

            //todo 启动子线程
            for (int i = 0; i < size; i++) {
                // 这里会出现线程池不够的情况
                executor.submit(new HandleTask(transactionInfo));
            }

            //TODO 主线程任务 在子线程上面是否可行未尝试！
            UserInfo userInfo = new UserInfo();
            userInfo.setName("主线程");
            saveDataMapper.saveDate(userInfo);

            // 判断子线程是否成功
            if (threadLatch != null) {
                if (wait(threadLatch, threadResult, rollBack)) {
                    //回滚主线程
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
            }
//            System.out.println(1 / 0);

        } catch (Exception e) {
            log.error("handleMessage error ", e);
            // 主线程发生异常 也要回滚
            rollBack.setIsRollBack(true);
            //回滚主线程
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } finally {
            mainLatch.countDown();
        }
    }

    /**
     * //TODO 设置只等待10秒钟，等待子线程执行完毕
     *
     * @Author barrett
     * @Date 2020/10/23 11:28
     **/
    public boolean wait(CountDownLatch threadLatch, BlockingDeque<String> threadResult, RollBack rollBack) throws InterruptedException {
        // 等待子线程执行完毕
        boolean await = threadLatch.await(10, TimeUnit.SECONDS);
        //返回给主线程是否需要回滚
        boolean flag = false;
        if (await) {
            while (threadResult.size() > 0) {
                String re = threadResult.take();
                if (!re.equals(thread_result_succ)) {
                    flag = true;
                    rollBack.setIsRollBack(true);
                    System.out.println("子线程抛出异常：" + re);
                    break;
                }
            }
        } else {
            // 超时 直接回滚
            rollBack.setIsRollBack(true);
        }
        return flag;
    }

    private AtomicInteger type = new AtomicInteger(0);

    private class HandleTask implements Runnable {
        private TransactionInfo transactionInfo;

        private HandleTask(TransactionInfo transactionInfo) {
            this.transactionInfo = transactionInfo;
        }

        @Override
        public void run() {
            try {
                log.info("HandleTask start 子线程: {}" ,Thread.currentThread().getName());
                insertDataService.saveData(transactionInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
