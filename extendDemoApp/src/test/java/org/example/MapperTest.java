package org.example;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.api.DemoBeanInter;
import org.example.defaultimpl.DemoBean;
import org.example.domain.entity.Param;
import org.example.mapper.ParamMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MapperTest {
    @SpyBean
    private ParamMapper mapper;
    @Test
    public void selectTest(){
        QueryWrapper<Param> wrapper = new QueryWrapper();
        wrapper.select("start_version","end_version");
        wrapper.eq("value","20");
        List<Param> list = mapper.selectList(wrapper);
        Assert.notNull(list);
    }
}
