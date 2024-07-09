package org.example.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("param")
public class Param {
    @TableId(value = "id",type = IdType.AUTO)
    private int id;
    @TableField(value = "biz_key")
    private String bizKey;
    @TableField(value = "biz_value")
    private String bizValue;
    @TableField("start_version")
    private int startVersion;
    @TableField(value = "end_version")
    private int endVersion;
}
