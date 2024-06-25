package org.example.defaultimpl;

import org.example.api.DemoBeanInter;
import org.example.mapper.DemoData;
import org.example.mapper.DemoDataMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class DemoBean implements DemoBeanInter {

    @Autowired
    private DemoDataMapper mapper;

    @Override
    public String sayHello() {
        List<DemoData> list = mapper.selectList(null);

        return "Hello World!"+list.size();
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
