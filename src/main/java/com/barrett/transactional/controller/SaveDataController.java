package com.barrett.transactional.controller;

import com.barrett.transactional.server.TaskService;
import com.barrett.transactional.util.service.MainTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author barrett
 * @Date 2020/10/26 10:59
 **/
@Slf4j
@RestController
@RequestMapping("/user")
public class SaveDataController {

    @Autowired
    private MainTaskService mainTaskService;

    /**
     * 进一步封装，解耦
     * @author created by barrett in 2021/1/4 16:40
     **/
    @GetMapping("/test")
    public String test(@RequestParam("param") Integer param) {
        if (param < 1) {
            log.warn("createUser param is error");
            return "createUser param is error";
        }
        Map<String,Object> map=new HashMap<>();
        String s = mainTaskService.handleTask(param, 1);
        return s;
    }


}
