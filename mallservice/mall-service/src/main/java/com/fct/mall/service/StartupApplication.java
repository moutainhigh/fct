package com.fct.mall.service;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by jon on 2017/5/17.
 */
@SpringBootApplication(scanBasePackages = "com.fct.mall")
public class StartupApplication {

    public static void main(String[] args){
        SpringApplication application = new SpringApplication(StartupApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }
}
