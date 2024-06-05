package org.example.defaultimpl;

import org.example.api.DemoBeanInter;

public class DemoBean implements DemoBeanInter {
    @Override
    public String sayHello() {
        return "Hello World!";
    }
}
