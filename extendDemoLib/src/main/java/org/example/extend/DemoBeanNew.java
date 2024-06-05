package org.example.extend;

import org.example.api.DemoBeanInter;
import org.example.defaultimpl.DemoBean;

public class DemoBeanNew extends DemoBean {
    @Override
    public String sayHello() {
        return "I'm the new ONE";
    }
}
