if test.demo_data not
create table test.demo_data
(
    biz_key     int auto_increment
        primary key,
    data_name   varchar(128) charset utf8mb3  null,
    versions    varchar(128)                  null,
    create_time varchar(64)                   null,
    update_time varchar(64)                   null,
    status      int default 0                 null,
    request     varchar(2048) charset utf8mb3 null,
    response    varchar(2048) charset utf8mb3 null
)
    comment 'demo_data';

create index demo_data_data_name_versions_index
    on test.demo_data (data_name, versions);

