package com.barrett.transactional.util.threadTransaction;

import lombok.Data;

/**
 * //TODO 事务控制
 * 如果使用变量会造成线程之间不可共享变量值
 * @Author barrett
 * @Date 2020/10/26 10:06
 **/
@Data
public class RollBack {
    private Boolean isRollBack;

    public RollBack(Boolean isRollBack) {
        this.isRollBack = isRollBack;
    }
}