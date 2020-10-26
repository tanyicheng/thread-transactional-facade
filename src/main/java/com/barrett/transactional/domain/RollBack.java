package com.barrett.transactional.domain;

import lombok.Data;

/**
 * //TODO 事务控制
 * @Author barrett
 * @Date 2020/10/26 13:37
 **/
@Data
public class RollBack {
    private Boolean isRollBack;

    public RollBack(Boolean isRollBack) {
        this.isRollBack = isRollBack;
    }
}