package com.barrett.transactional.dao;

import com.barrett.transactional.domain.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author barrett
 * @Date 2020/10/26 11:03
 **/
@Repository
public interface SaveDataMapper {
    /**
     * 报错数据
     *
     * @param userInfos
     * @throws Exception sql异常
     */
    void saveDateList(@Param("userInfos") List<UserInfo> userInfos) throws Exception;
    void saveUser(@Param("userInfo") UserInfo userInfo) throws Exception;

}
