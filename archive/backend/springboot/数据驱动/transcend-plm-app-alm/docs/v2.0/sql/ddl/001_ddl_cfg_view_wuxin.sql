#调整视图字段
alter table cfg_view
    add belong_bid varchar(32) null comment '所属bid' after bid,
    add view_type  varchar(32) null comment '视图类型' after type,
    add view_scope varchar(32) null comment '视图作用域（实例，表格，流程）' after view_type,
    add priority   int         null comment '优先级' after tag;

#调整视图规则字段
alter table cfg_object_view_rule
    change role_code role_codes JSON null comment '角色编码列表',
    change lc_state_code property_match_params JSON null comment '字段匹配参数';

