package com.autotest.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动程序
 *
 * @author autotest
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableAsync
@EnableScheduling
public class AutoTestPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoTestPlatformApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  自动化测试平台启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}