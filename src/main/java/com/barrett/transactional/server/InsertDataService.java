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
 * 批量插入数据
 *
 * @author cmd
 * @data 2020/4/10 19:16
 */
@Slf4j
@Service
public class InsertDataService extends BaseServer {
    @Autowired
    private SaveDataMapper saveDataMapper;

    //不加也可以 transactionManager = "transactionalTransactionManager"
//    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionalTransactionManager", propagation = Propagation.REQUIRED)
    @Transactional(rollbackFor = Exception.class,  propagation = Propagation.REQUIRED)
    public void saveData(TransactionInfo transactionInfo) throws Exception {
        Exception ex = null;
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setName("子线程");
            SaveDataMapper mapper= (SaveDataMapper) ContextUtils.getBean("saveDataMapper");
            mapper.saveDate(userInfo);

            System.out.println(1 / 0);
            transactionInfo.getThreadResult().put(ThreadTransactionalService.thread_result_succ);
        } catch (Exception e) {
            log.error("saveData error ", e);
            transactionInfo.getThreadResult().put(ThreadTransactionalService.thread_result_err);
            ex = e;
        } finally {
            // 执行完成 等待主线程通知回滚
            handleException(transactionInfo, ex);
        }
    }
}
