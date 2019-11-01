package com.bw.fyj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Created by 钰杰 on 2019/8/21.
 */
@Configuration
public class TokenInterceptor extends WebMvcConfigurationSupport{

    @Autowired
    private MyInterceptor myInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(myInterceptor).addPathPatterns("/**").excludePathPatterns("/**/login");
    }
}
