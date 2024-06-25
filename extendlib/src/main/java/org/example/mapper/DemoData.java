package org.example.mapper;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("demo_data")
public class DemoData {
    private String bizKey;
    private String dataName;
    private String versions;
    private String createTime;
    private String updateTime;
    private int status;
    private String request;
    private String response;
}
