package com.barrett.transactional.util.threadTransaction;


import java.util.Map;
import java.util.concurrent.*;

/**
 * 多线程事务管理器[通过线程通信解决多线程事务提交问题]
 * 注：
 * 1.多线程入库仅支持同一种操作:比如单insert/单update,由于数据库的事务隔离级别,不能对同一条数据同时insert和update,会导致死锁
 * 2.如果非要使用多线程同时insert和update,则需要把事务隔离级别降为READ_COMMITTED,但是会出现先update导致失败问题(注解不存在)
 * 3.MySQL默认事务隔离级别:REPEATABLE_READ
 *
 * @Author barrett
 * @Date 2020/10/26 08:12
 **/
public class ThreadTransactionalFacade {

    //根据操作系统获取可用的线程数（不完全准确）
    public static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2 + 1;
    public final static String thread_result_fail = "fail";
    public final static String thread_result_succ = "succ";

    //创建定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
    public ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    public RollBack rollBack;
    public CountDownLatch mainLatch;
    public CountDownLatch threadLatch;
    public BlockingDeque<Map<String,Object>> threadResult;
    public TransactionInfo transactionInfo;

    public void init(int threadNum){
        // 主线程监控
        mainLatch = new CountDownLatch(1);
        // 子线程监控
        threadLatch = null;
        // 事务共享管理器
        rollBack = new RollBack(false);
        threadResult = new LinkedBlockingDeque<>(THREAD_COUNT);
        transactionInfo = new TransactionInfo();
        transactionInfo.setThreadResult(threadResult);
        transactionInfo.setRollBack(rollBack);
        transactionInfo.setMainLatch(mainLatch);

        transactionInfo.setMultiThreading(true);
        threadLatch = new CountDownLatch(threadNum);//子线程监控数，关系到一个请求中可以创建几条子线程
        transactionInfo.setThreadLatch(threadLatch);
    }

}
