package com.barrett.transactional.controller;

import com.barrett.transactional.domain.UserInfo;
import com.barrett.transactional.server.ThreadTransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author cmd
 * @data 2020/4/10 19:54
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class SaveDataController {

    @Autowired
    private ThreadTransactionalService service;

    /**
     * //TODO 自定义
     * @Author barrett
     * @Date 2020/10/23 09:10
     **/
    @GetMapping("/test")
    public boolean test(@RequestParam("size") Integer size) {
        if (size < 1) {
            log.warn("createUser param is error");
            return false;
        }

        service.handleTransaction(size);
        return true;
    }

}
