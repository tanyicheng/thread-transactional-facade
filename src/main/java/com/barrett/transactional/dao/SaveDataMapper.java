package com.barrett.transactional.dao;

import com.barrett.transactional.domain.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author cmd
 * @data 2020/4/10 19:19
 */
@Repository
public interface SaveDataMapper {
    /**
     * 报错数据
     *
     * @param userInfos
     * @throws Exception sql异常
     */
    void saveDateList(@Param("userInfos") List<UserInfo> userInfos) throws Exception;
    void saveDate(UserInfo userInfo) throws Exception;

}
