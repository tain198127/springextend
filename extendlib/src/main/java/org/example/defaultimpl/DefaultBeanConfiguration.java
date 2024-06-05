package org.example.defaultimpl;

import org.example.api.DemoBeanInter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


public class DefaultBeanConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public DemoBeanInter conditionInstance(){
        return new DemoBean();
    }
}
