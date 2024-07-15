package org.example.interceptor;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorConfiguration {
    @Bean
    public SqlParseInterceptor sqlParseInterceptor() {
        return new SqlParseInterceptor();
    }
    @Bean
    public SqlParseInterceptorExecutor SqlParseInterceptorExecutor(){
        return new SqlParseInterceptorExecutor();
    }
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer(SqlParseInterceptor sqlParseInterceptor,SqlParseInterceptorExecutor sqlParseInterceptorExecutor) {
        return configuration -> {
//            configuration.addInterceptor(sqlParseInterceptor);
            configuration.addInterceptor(sqlParseInterceptorExecutor);
        };
    }
}
