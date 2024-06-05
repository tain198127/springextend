package org.example.extend;

import org.example.api.DemoBeanInter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultBeanConfiguration2 {
    @Bean
    public DemoBeanInter defaultInstance() {
        return new DemoBeanNew();
    }
}
