package org.example.domain.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("param")
public class Param {
    private int id;

    private String key;

    private String value;
    private int startVersion;
    private int endVersion;
}
