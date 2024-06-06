package org.example;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.api.DemoBeanInter;
import org.example.domain.entity.Param;
import org.example.mapper.ParamMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Component
public class DemoService implements DemoServiceInterface{
    @Resource
    private DemoBeanInter bean;
    @Resource
    private ParamMapper mapper;
    @Override
    public String generateName(String firstName) {
        QueryWrapper<Param> wrapper = new QueryWrapper();
        wrapper.select("start_version","end_version");
        wrapper.eq("value","20");

        List<Param> result = mapper.selectList(wrapper);
        return bean.sayHello()+firstName+result.get(0).getEndVersion();
    }
}
