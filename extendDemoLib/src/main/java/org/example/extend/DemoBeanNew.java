package org.example.extend;

import org.example.api.DemoBeanInter;
import org.example.defaultimpl.DemoBean;
import org.example.mapper.DemoDataMapper;
import org.springframework.beans.factory.annotation.Autowired;

public class DemoBeanNew extends DemoBean {
    @Autowired
    private DemoDataMapper demoDataMapper;
    @Override
    public String sayHello() {
        return "I'm the new ONE"+demoDataMapper.selectList(null).get(0).getBizKey();
    }
}
