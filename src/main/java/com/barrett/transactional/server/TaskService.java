package com.barrett.transactional.server;

import com.barrett.transactional.dao.SaveDataMapper;
import com.google.common.collect.Lists;
import com.barrett.transactional.domain.RollBack;
import com.barrett.transactional.domain.TransactionInfo;
import com.barrett.transactional.domain.UserInfo;
import io.swagger.models.auth.In;
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
 * 业务逻辑
 */
@Slf4j
@Service
public class TaskService extends ThreadTransactionalFacade {

    @Autowired
    private InsertDataService insertDataService;
    @Autowired
    private SaveDataMapper saveDataMapper;

    /**
     * //TODO 事务控制
     *
     * @Author barrett
     * @Date 2020/10/23 09:18
     **/
    @Transactional
    public void HandleTask(Integer size) {

        //初始化
        init(size);

        try {
            // TODO start <<<<<<<<<<<<<<
            //TODO 主线程任务
            UserInfo userInfo = new UserInfo();
            userInfo.setName("主==");
            saveDataMapper.saveUser(userInfo);

            if (size == 3)
                System.out.println(1 / 0);

            //todo 启动子线程
            for (int i = 0; i < size; i++) {
                // 这里会出现线程池不够的情况
                executor.submit(new HandleChildTask(transactionInfo,size));
            }

            // 判断子线程是否成功
            if (threadLatch != null) {
                if (wait(threadLatch, threadResult, rollBack)) {
                    //回滚主线程
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
            }
            // TODO end <<<<<<<<<<<<<<
        } catch (Exception e) {
            log.error("handleMessage error ", e);
            // 主线程发生异常 也要回滚
            rollBack.setIsRollBack(true);
            //回滚主线程
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } finally {
            mainLatch.countDown();
            log.info("关闭线程池，释放资源");
            executor.shutdown();
        }
    }

    /**
     * //TODO 等待子线程执行完毕，有一条线程异常，全部回滚
     *
     * @Author barrett
     * @Date 2020/10/23 11:28
     **/
    public boolean wait(CountDownLatch threadLatch, BlockingDeque<String> threadResult, RollBack rollBack) throws InterruptedException {
        // 等待子线程执行完毕, fixme-1 可以不设等待时间
        boolean await = threadLatch.await(10, TimeUnit.SECONDS);
        //返回给主线程是否需要回滚
        boolean flag = false;
        if (await) {
            while (threadResult.size() > 0) {
                String re = threadResult.take();
                if (!re.equals(thread_result_succ)) {
                    flag = true;
                    rollBack.setIsRollBack(true);
                    log.info("子线程抛出异常：" + re);
                    break;
                }
            }
        } else {
            // 超时 直接回滚
            rollBack.setIsRollBack(true);
        }
        return flag;
    }


    /**
     * //TODO 子线程任务
     * @Author barrett
     * @Date 2020/10/26 08:26
     **/
    private class HandleChildTask implements Runnable {
        private TransactionInfo transactionInfo;
        private Integer size;

        private HandleChildTask(TransactionInfo transactionInfo, Integer size) {
            this.transactionInfo = transactionInfo;
            this.size=size;
        }

        @Override
        public void run() {
            try {
                //如果需要切数据源的，写在进入方法前
                log.info("HandleChildTask start 子线程: {}", Thread.currentThread().getName());
                insertDataService.saveData(transactionInfo,size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
