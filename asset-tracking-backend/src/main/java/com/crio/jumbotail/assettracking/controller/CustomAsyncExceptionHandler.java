package com.crio.jumbotail.assettracking.controller;

import java.lang.reflect.Method;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

@Log4j2
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
 
        LOG.error("Exception message - " + throwable.getMessage());
        LOG.error("Method name - " + method.getName());
        for (Object param : obj) {
            LOG.error("Parameter value - " + param);
        }
    }

}