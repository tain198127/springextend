package org.example;

import org.example.api.DemoBeanInter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/")
public class DemoController {
    @Resource
    private DemoBeanInter bean;

    @GetMapping("/hello")
    public String hello() {
        return bean.sayHello();
    }
}
