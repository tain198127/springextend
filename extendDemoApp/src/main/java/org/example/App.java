package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Hello world!
 */

//@EnableSwagger2
@EnableSwagger2
@EnableWebMvc
@EnableAsync
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = {"org.example", "com.baomidou.mybatisplus.mapper"})
public class App {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(App.class);
        springApplication.setWebApplicationType(WebApplicationType.SERVLET);
        springApplication.run(App.class, args);
    }
}
