package com.barrett.transactional.controller;

import com.barrett.transactional.server.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author barrett
 * @Date 2020/10/26 10:59
 **/
@Slf4j
@RestController
@RequestMapping("/user")
public class SaveDataController {

    @Autowired
    private TaskService service;

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

        service.HandleTask(size);
        return true;
    }

}
