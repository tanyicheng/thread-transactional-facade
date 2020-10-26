package com.barrett.transactional.server;

import com.barrett.transactional.domain.RollBack;
import com.barrett.transactional.domain.TransactionInfo;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程事务管理器[通过线程通信解决多线程事务提交问题]
 * 注：
 * 1.多线程入库仅支持同一种操作:比如单insert/单update,由于数据库的事务隔离级别,不能对同一条数据同时insert和update,会导致死锁
 * 2.如果非要使用多线程同时insert和update,则需要把事务隔离级别降为READ_COMMITTED,但是会出现先update导致失败问题(注解不存在)
 * 3.MySQL默认事务隔离级别:REPEATABLE_READ
 *
 * 思想就是使用两个CountDownWatch实现子线程的二段提交
 * 步骤：
 * 主线程将任务分发给子线程，然后 使用 boolean await = threadLatch.await(20,TimeUnit.SECONDS); 阻塞主线程，等待所有子线程处理向数据库中插入的业务
 * 使用 threadLatch.countDown(); 释放子线程锁定，同时使用 mainLatch.await(); 阻塞子线程，将程序的控制权交还给主线程
 * 主线程检查子线程执行插入数据库的结果，若有非预期结果出现，主线程标记状态告知子线程回滚，然后使用 mainLatch.countDown(); 将程序控制权再次交给子线程，子线程检测回滚标志，判断是否回滚
 * 子线程执行结束，主线程拼接处理结果，响应给请求方
 * 整个过程类似于GC的标记-清除过程（串行的垃圾收集器）
 * @Author barrett
 * @Date 2020/10/26 08:12
 **/
public class ThreadTransactionalFacade {

    //根据操作系统获取可用的线程数（不完全准确），用来设置线程池的最大线程数
    public static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2 + 1;
    public final static String thread_result_err = "err";
    public final static String thread_result_succ = "succ";

    //创建定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    //事务共享管理器：必须要使用对象，如果使用变量会造成线程之间不可共享变量值
    public RollBack rollBack;
    //线程监控
    public CountDownLatch mainLatch;
    public CountDownLatch threadLatch;
    //根据子线程执行结果判断是否需要回滚
    public BlockingDeque<String> threadResult;
    public TransactionInfo transactionInfo;

    //原子数
    private AtomicInteger type = new AtomicInteger(0);

    public void init(Integer size){
        System.out.println("可用线程数："+THREAD_COUNT);
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
        //TODO 这里的子线程监控数和开辟的子线程数要一致
        threadLatch = new CountDownLatch(size);
        transactionInfo.setThreadLatch(threadLatch);
    }

}
