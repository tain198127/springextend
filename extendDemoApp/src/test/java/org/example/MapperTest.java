package org.example;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.domain.entity.Param;
import org.example.mapper.ParamMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MapperTest {
    @SpyBean
    private ParamMapper mapper;
    @Autowired
    private ConfigurationCustomizer configurationCustomizer;
    @Test
    public void selectTest(){
        QueryWrapper<Param> wrapper = new QueryWrapper();

        wrapper.select("start_version","end_version");
        wrapper.eq("biz_value","20");
        List<Param> list = mapper.selectList(wrapper);
        Assert.notNull(list);
    }
    @Test
    @Transactional
    @Rollback
    public void insertTest(){
        Param param = new Param();
        param.setBizKey(UUID.randomUUID().toString());
        param.setBizValue(UUID.randomUUID().toString());
        param.setEndVersion(1);
        param.setStartVersion(0);
        mapper.insert(param);
    }
    @Test
    @Transactional
    @Rollback
    public void updateTest(){
        QueryWrapper<Param> wrapper = new QueryWrapper();
        wrapper.eq("end_version",0);
        Page<Param> page = new Page<>();
        page.setCurrent(0).setSize(1);
        Page<Param> selectPages = mapper.selectPage(page,wrapper);
        List<Param> list = selectPages.getRecords();
        if(null != list && !list.isEmpty()){
            Param param = list.get(0);
            param.setBizValue(UUID.randomUUID().toString());
            mapper.updateById(param);
        }
    }
}
