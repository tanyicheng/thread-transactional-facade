package com.barrett.transactional.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AppContextHandle implements ApplicationContextAware {

    public void setApplicationContext(ApplicationContext app) {
        ContextUtils.setApplicationContext(app);
    }

}
