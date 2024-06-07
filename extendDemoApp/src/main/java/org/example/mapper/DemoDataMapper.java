package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.domain.po.DemoData;
@Mapper
public interface DemoDataMapper  extends BaseMapper<DemoData> {
}
