package org.example.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("param")
public class Param {
    @TableId(value = "id",type = IdType.AUTO)
    private int id;
    @TableField(value = "biz_key")
    private String key;
    @TableField(value = "biz_value")
    private String value;
    @TableField("start_version")
    private int startVersion;
    @TableField(value = "end_version")
    private int endVersion;
}
