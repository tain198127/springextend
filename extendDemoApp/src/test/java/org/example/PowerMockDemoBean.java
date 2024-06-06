package org.example;

import org.example.api.DemoBeanInter;
import org.example.defaultimpl.DemoBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.example.mapper.ParamMapper;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(value = {DemoBean.class, DemoBeanInter.class})
@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = App.class)
@PowerMockIgnore(value = {"javax.management.*", "jdk.nashorn.*", "javax.script.*", "org.apache.log4j.*"})
//@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
//@AutoConfigureMybatis
public class PowerMockDemoBean {
    @BeforeEach
    void setUp() {
//        MockitoAnnotations.initMocks(this);
//        MapperRegistry registry = new MybatisMapperRegistry(new MybatisConfiguration());
//        Collection<Class<?>> mappers = registry.getMappers();
    }

    //    @AfterClass
//    public static void classSetUp(){
//        MapperRegistry registry = new MybatisMapperRegistry(new MybatisConfiguration());
//        Collection<Class<?>> mappers = registry.getMappers();
//    }
    @Test
    public void DemoBeanStaticTest() {
        String expeced = "fuckme";
        PowerMockito.mockStatic(DemoBean.class);
        PowerMockito.when(DemoBean.killMe(org.mockito.Mockito.anyMap())).thenReturn(expeced);
        String result = DemoBean.killMe(new HashMap<>());
        Assert.assertEquals(result, expeced);
    }

    @Test
    public void DemoBeansayHello() throws Exception {
        String expeced = "fuckme";
        Map<String, String> input = new HashMap<>();
        input.put("check", "name");
        DemoBean bean = new DemoBean();
        DemoBean spyInstance = PowerMockito.spy(bean);
        PowerMockito.when(spyInstance, "checkName", input).thenReturn(expeced);
        String result = Whitebox.invokeMethod(spyInstance, "checkName", input);
        Assert.assertEquals(result, expeced);
    }

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

    @Test
    public void selectTest() throws Exception {
        String expecd = "hello";
        String input = "input";
        PowerMockito.when(inter.sayHello()).thenReturn(expecd);
        String result = serviceInterface.generateName(input);


    }

}
