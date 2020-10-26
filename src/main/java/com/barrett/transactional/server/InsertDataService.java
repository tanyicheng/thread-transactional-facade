package com.barrett.transactional.server;

import com.barrett.transactional.config.ContextUtils;
import com.barrett.transactional.dao.SaveDataMapper;
import com.barrett.transactional.domain.TransactionInfo;
import com.barrett.transactional.domain.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * //TODO 插入数据
 * @Author barrett
 * @Date 2020/10/26 11:17
 **/
@Slf4j
@Service
public class InsertDataService extends TransactionalException {
    @Autowired
    private SaveDataMapper saveDataMapper;

    //不加也可以 transactionManager = "transactionalTransactionManager"
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveData(TransactionInfo transactionInfo, Integer size) throws Exception {
        Exception ex = null;
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setName("子--");
            //如果 HandleChildTask 是单独的一个类，则无法使用 @Autowired 注入，只能通过下面的方式获取
//            SaveDataMapper mapper = (SaveDataMapper) ContextUtils.getBean("saveDataMapper");
            saveDataMapper.saveDate(userInfo);

            if (size == 4)
                System.out.println(1 / 0);
            transactionInfo.getThreadResult().put(TaskService.thread_result_succ);
        } catch (Exception e) {
            log.error("saveData error ", e);
            transactionInfo.getThreadResult().put("具体的异常信息");
            ex = e;
        } finally {
            // 执行完成 等待主线程通知回滚
            handleException(transactionInfo, ex);
        }
    }
}
