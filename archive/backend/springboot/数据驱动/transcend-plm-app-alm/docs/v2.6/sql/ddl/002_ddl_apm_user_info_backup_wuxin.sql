create table apm_user_info_backup
(
    id                      varchar(10)                        not null comment '工号' primary key,
    `name`                  varchar(64)                        null comment '人员姓名',
    level_1_department_id   varchar(64)                        null comment '一级部门id',
    level_1_department_name varchar(128)                       null comment '一级部门id',
    level_2_department_id   varchar(64)                        null comment '二级部门id',
    level_2_department_name varchar(128)                       null comment '二级部门id',
    level_3_department_id   varchar(64)                        null comment '三级部门id',
    level_3_department_name varchar(128)                       null comment '三级部门id',
    created_time            datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_time            datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '本地用户备份表,用于BI视图展现' row_format = DYNAMIC;
