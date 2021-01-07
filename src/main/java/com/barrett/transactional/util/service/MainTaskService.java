package com.barrett.transactional.util.service;

import com.barrett.transactional.dao.SaveDataMapper;
import com.barrett.transactional.domain.UserInfo;
import com.barrett.transactional.util.threadTransaction.RollBack;
import com.barrett.transactional.util.threadTransaction.ThreadTransactionalFacade;
import com.barrett.transactional.util.threadTransaction.TransactionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * //TODO 某功能的主业务
 *
 * @Author barrett
 * @Date 2020/10/26 08:43
 **/
@Slf4j
@Service
public class MainTaskService extends AbsTaskService {
    @Autowired
    private SaveDataMapper dataMapper;

    /**
     * 子业务接口
     *
     * @author created by barrett in 2021/1/4 14:35
     **/
    @Autowired
    @Qualifier("childTaskService")
    private ChildTask childTask;

    @Autowired
    @Qualifier("childTaskService2")
    private ChildTask childTask2;

    /**
     * 主业务逻辑，必须实现
     * @author created by barrett in 2021/1/6 22:16
     **/
    public <T> void mainTask(T t) throws Exception {
        int num = (Integer) t;
        //主线程
        UserInfo userInfo = new UserInfo();
        userInfo.setName("主");
        userInfo.setSex(num);
        dataMapper.saveUser(userInfo);

        if (num == 10)
            System.out.println(1 / 0);

        //子线程
        executor.submit(new HandleChildTask(transactionInfo, userInfo, childTask));

//        executor.submit(new HandleChildTask(transactionInfo, "任意对象",childTask2));

    }

    /**
     * 主线程无需用到子线程的返回值，不需要重写此方法
     * TODO 主线程需要等待子线程的结果集，需要重写此方法 ：目前仅适用于A调用B的情况，A调用B/C/D暂不支持，还有就是禁止套娃！
     * @author created by barrett in 2021/1/6 22:05
     **/
    @Override
    public boolean wait(CountDownLatch threadLatch, BlockingDeque<Map<String, Object>> threadResult, RollBack rollBack) throws Exception {
        // 等待子线程执行完毕,默认100秒，超时直接回滚
        boolean await = threadLatch.await(100, TimeUnit.SECONDS);
        //返回给主线程是否需要回滚
        boolean flag = false;
        if (await) {
            while (threadResult.size() > 0) {
                Map<String, Object> result = threadResult.take();
                if (ThreadTransactionalFacade.thread_result_succ.equals(result.get(TransactionInfo.result).toString())) {
                    //TODO 业务逻辑 start
                    String str = (String) result.get(TransactionInfo.obj);//子线程返回的结果
                    UserInfo userInfo = new UserInfo();
                    userInfo.setName(str);
                    userInfo.setSex(1);
                    dataMapper.saveUser(userInfo);
                    log.info("子线程结果 =================== {}", str);

                    //TODO end
                } else {
                    flag = true;
                    rollBack.setIsRollBack(true);
                    log.error("子线程抛出异常：" + result.get(TransactionInfo.obj).toString());
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
