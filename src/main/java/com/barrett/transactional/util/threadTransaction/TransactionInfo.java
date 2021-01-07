package com.barrett.transactional.util.threadTransaction;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * //TODO
 * @Author barrett
 * @Date 2020/10/26 10:03
 **/
@Data
public class TransactionInfo {
    /**
     * 每个线程执行结果
     */
    private BlockingDeque<Map<String,Object>> threadResult = new LinkedBlockingDeque<>(30);
    /**
     * 事务管理器
     */
    private RollBack rollBack;
    /**
     * 主线程监控
     */
    private CountDownLatch mainLatch;
    /**
     * 当前子线程监控
     */
    private CountDownLatch threadLatch;

    /**
     * 是否是多线程
     */
    private boolean isMultiThreading = false;

    public final static String result = "result";
    public final static String obj = "obj";

    public <T> Map<String,Object> getSuccessResult(T t){
        Map<String,Object> map = new HashMap<>();
        map.put("result", ThreadTransactionalFacade.thread_result_succ);
        map.put("obj",t);
        return map;
    }

    public <T> Map<String,Object> getFailResult(T t){
        Map<String,Object> map = new HashMap<>();
        map.put("result", ThreadTransactionalFacade.thread_result_fail);
        map.put("obj",t);
        return map;
    }
}
