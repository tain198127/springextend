package org.example;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.api.DemoBeanInter;
import org.example.domain.entity.Param;
import org.example.domain.po.DemoData;
import org.example.mapper.DemoDataMapper;
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
    @Resource
    private DemoDataMapper demoDataMapper;
    @Override
    public String generateName(String firstName) {
        QueryWrapper<Param> wrapper = new QueryWrapper();
        wrapper.select("start_version","end_version");
        wrapper.eq("value","20");

        List<Param> result = mapper.selectList(wrapper);

        List<DemoData> data = demoDataMapper.selectList(null);
        if(data.stream().anyMatch(item->item.getStatus() == 2)){
            System.out.println("找到对应的数据");
        }
        return bean.sayHello()+firstName+result.get(0).getEndVersion()+data.size();
    }
}
