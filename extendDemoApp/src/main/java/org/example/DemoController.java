package org.example;

import org.example.api.DemoBeanInter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

@RestController("/")
public class DemoController {

    private SseEmitter sseEmitterService;


    @Resource
    private DemoServiceInterface name;
    @GetMapping("/hello")
    public String hello(@RequestParam("greeding") String greeding) {
        return name.generateName(greeding);

    }
    @GetMapping("/insert")
    public String insertParam(@RequestParam("greeding") String greeding){
        return name.insertName(greeding);
    }

}
