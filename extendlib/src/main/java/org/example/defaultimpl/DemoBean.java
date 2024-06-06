package org.example.defaultimpl;

import org.example.api.DemoBeanInter;

import java.util.Map;

public class DemoBean implements DemoBeanInter {
    @Override
    public String sayHello() {
        return "Hello World!";
    }
    private String checkName(Map<String ,String> input){
        if(null!=input && !input.isEmpty()){
            return input.keySet().stream().findFirst().get();
        }
        return "";
    }
    public static String killMe(Map<String,String> input){
        return "999";
    }
}
