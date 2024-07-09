package org.example;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.api.DemoBeanInter;
import org.example.domain.entity.Param;
import org.example.mapper.DemoData;
import org.example.mapper.DemoDataMapper;
import org.example.mapper.ParamMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Component
public class DemoService implements DemoServiceInterface {
    @Resource
    private DemoBeanInter bean;
    @Resource
    private ParamMapper mapper;
    @Resource
    private DemoDataMapper demoDataMapper;



    @Override
    public String generateName(String firstName) {
        QueryWrapper<Param> wrapper = new QueryWrapper();
        wrapper.select("start_version", "end_version");
        wrapper.eq("biz_value", "20");


        List<Param> result = mapper.selectList(wrapper);

        List<DemoData> data = demoDataMapper.selectList(null);
        if (data.stream().anyMatch(item -> item.getStatus() == 2)) {
            System.out.println("找到对应的数据");
        }
        return bean.sayHello() + data.size();
    }

    @Transactional
    @Override
    public String batchInsert(String tempLate) {
        int count = 0;
        for (int i = 0; i < 10000; i++) {
            Param param = new Param();
            param.setBizValue(tempLate + UUID.randomUUID().toString() + "_" + i);
            param.setBizKey(tempLate + System.currentTimeMillis() + "_" + i);
            param.setEndVersion(1);
            param.setStartVersion(0);
            count += mapper.insert(param);
        }
        return String.valueOf(count);
    }

    @Transactional
    @Override
    public String insertName(String firstName) {
        Param param = new Param();
        param.setBizValue(firstName);
        param.setBizKey(firstName);
        param.setEndVersion(1);
        param.setStartVersion(0);
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int id = mapper.insert(param);
        System.out.println("第一步已经完成");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mapper.insert(param);
        System.out.println("第二步已经完成");
        return String.valueOf(id);

    }
}
