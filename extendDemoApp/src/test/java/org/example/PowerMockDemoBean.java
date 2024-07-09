package org.example;

import org.example.api.DemoBeanInter;
import org.example.defaultimpl.DemoBean;
import org.example.mapper.DemoDataMapper;
import org.example.mapper.ParamMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(value = {DemoBean.class, DemoBeanInter.class})
@ExtendWith(value = {SpringExtension.class})
@PowerMockIgnore(value = {"javax.management.*", "jdk.nashorn.*", "javax.script.*", "org.apache.log4j.*"})
@SpringBootTest
public class PowerMockDemoBean {
    /**
     * 被测试的类
     */
    @InjectMocks
    @Resource
    private DemoService serviceInterface;
    /**
     * DemoService中用到的类，例如mapper
     */
    @Mock
    private DemoBeanInter inter = PowerMockito.mock(DemoBean.class);
    @SpyBean
    private ParamMapper mapper;

    @SpyBean
    private DemoDataMapper demoDataMapper;
    @Before
    public void setup(){
        PowerMockito.mockStatic(DemoBean.class);
    }
    @Test
    @DisplayName("DemoBeanStaticTest")
    public void DemoBeanStaticTest() {
        String expeced = "fuckme";
        PowerMockito.mockStatic(DemoBean.class);
        Mockito.when(DemoBean.killMe(org.mockito.Mockito.anyMap())).thenReturn(expeced);
//        PowerMockito.when(DemoBean.killMe(org.mockito.Mockito.anyMap())).thenReturn(expeced);
        String result = DemoBean.killMe(new HashMap<>());
        Assert.assertEquals(result, expeced);
    }

    @Test
    @DisplayName("DemoBeansayHello")
    public void DemoBeansayHello() throws Exception {
        String expeced = "fuckme";
        Map<String, String> input = new HashMap<>();
        input.put("check", "name");
//        DemoBean bean = new DemoBean();
        DemoBean spyInstance = PowerMockito.mock(DemoBean.class);
        PowerMockito.when(spyInstance, "checkName", input).thenReturn(expeced);
        String result = Whitebox.invokeMethod(spyInstance, "checkName", input);
        Assert.assertEquals(result, expeced);
    }


    @Sql(scripts = {"paraminit.sql"})
    @CsvFileSource(resources = "/paramtest.csv", numLinesToSkip = 1)
    @Transactional
    @Rollback
    @ParameterizedTest
    @DisplayName("演示测试")
    public void selectTest(String expectd, String input, int idx) {
        System.out.println(expectd);
        PowerMockito.when(inter.sayHello()).thenReturn(expectd);
        String result = serviceInterface.generateName(input);
        serviceInterface.batchInsert("debug");
        System.out.println(result);

    }

}



