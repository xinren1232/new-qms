update permission_plm_rule_condition
set attr_code_value = replace(attr_code_value, 'CURRENT_USER_FIRST_DEPT', '{CURRENT_USER_FIRST_DEPT}'),
    value_type='custom'
where attr_code_value like '%CURRENT_USER_FIRST_DEPT%';

update permission_plm_rule_condition
set value_type='custom'
where attr_code_value like '%LOGIN_USER%';