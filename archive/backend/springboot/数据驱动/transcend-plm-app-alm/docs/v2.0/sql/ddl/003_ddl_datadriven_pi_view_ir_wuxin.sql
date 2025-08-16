-- 创建IR领域展平视图
CREATE VIEW view_transcend_model_ir AS
SELECT jt.domain_id, tmd.name AS domain_name, ir.*
FROM transcend_model_IR AS ir
         LEFT JOIN JSON_TABLE(belong_domain, '$[*]'
                              COLUMNS (domain_id VARCHAR(50) PATH '$')) AS jt ON TRUE
         LEFT JOIN transcend_model_DOMAIN tmd ON tmd.bid = jt.domain_id;

-- ir资源部门分割视图
CREATE VIEW view_ir_dept_split AS
SELECT jt.dept_name AS `dept_name`, ir.*
FROM transcend_model_IR AS ir
         LEFT JOIN JSON_TABLE(resource_department, '$[*]' COLUMNS (dept_name VARCHAR(100) PATH '$')) AS jt ON TRUE
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR);

CREATE VIEW view_ir_dept_status AS
SELECT dept_name                                        AS `dept_name`,
       COUNT(1)                                         AS `total`,
       COUNT(IF(life_cycle_code = 'SUBMIT', 1, NULL))   AS `submit`,
       COUNT(IF(life_cycle_code = 'RECHECK', 1, NULL))  AS `recheck`,
       COUNT(IF(life_cycle_code = 'LOCK', 1, NULL))     AS `lock`,
       COUNT(IF(life_cycle_code = 'DEVELOP', 1, NULL))  AS `develop`,
       COUNT(IF(life_cycle_code = 'TEST', 1, NULL))     AS `test`,
       COUNT(IF(life_cycle_code = 'CHECK', 1, NULL))    AS `check`,
       COUNT(IF(life_cycle_code = 'COMPLETE', 1, NULL)) AS `complete`
FROM view_ir_dept_split
WHERE delete_flag = FALSE
  AND life_cycle_code IN ('SUBMIT', 'RECHECK', 'LOCK', 'DEVELOP', 'TEST', 'CHECK', 'COMPLETE')
GROUP BY dept_name;

# 统计IR领域汇总统计数量
CREATE VIEW view_ir_domain_status AS
SELECT domain_name,
       COUNT(1)                                         AS `total`,
       COUNT(IF(life_cycle_code = 'SUBMIT', 1, NULL))   AS `submit`,
       COUNT(IF(life_cycle_code = 'RECHECK', 1, NULL))  AS `recheck`,
       COUNT(IF(life_cycle_code = 'LOCK', 1, NULL))     AS `lock`,
       COUNT(IF(life_cycle_code = 'DEVELOP', 1, NULL))  AS `develop`,
       COUNT(IF(life_cycle_code = 'TEST', 1, NULL))     AS `test`,
       COUNT(IF(life_cycle_code = 'CHECK', 1, NULL))    AS `check`,
       COUNT(IF(life_cycle_code = 'COMPLETE', 1, NULL)) AS `complete`
FROM view_transcend_model_ir
WHERE life_cycle_code IN ('SUBMIT', 'RECHECK', 'LOCK', 'DEVELOP', 'TEST', 'CHECK', 'COMPLETE')
  AND delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
GROUP BY domain_name;

#部门联合查询视图
CREATE VIEW view_dept_union_all AS
SELECT 'IR需求' AS `type`, bid, dept_name, life_cycle_code, requirementlevel
FROM view_ir_dept_split
WHERE delete_flag = FALSE
UNION ALL
SELECT 'SR需求' AS `type`, bid, resource_department, life_cycle_code, irRequirementlevel
FROM view_sr_domain_status_join_rl sr
WHERE sr.delete_flag = FALSE
  AND sr.created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
UNION ALL
SELECT 'AR需求' AS `type`, bid, resource_department, life_cycle_code, NULL
FROM transcend_model_ar
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR);


#领域联合查询视图
CREATE VIEW view_domain_union_all AS
SELECT 'IR需求' AS `type`, bid, domain_id, domain_name, life_cycle_code, requirementlevel
FROM view_transcend_model_ir
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
UNION ALL
SELECT 'SR需求' AS `type`, sr.bid, domain_id, sr.domain_name, sr.life_cycle_code, sr.irRequirementlevel
FROM view_sr_domain_status_join_rl sr
WHERE sr.delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
UNION ALL
SELECT 'AR需求' AS `type`, bid, domain_id, domain_name, life_cycle_code, NULL
FROM view_transcend_model_ar
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR);



#部门联合查询视图分别统计完成
CREATE VIEW view_dept_union_all_complete AS
SELECT 'IR需求' AS `type`, bid, dept_name, life_cycle_code, requirementlevel
FROM view_ir_dept_split
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
UNION ALL
SELECT 'IR验收完成' AS `type`, bid, dept_name, life_cycle_code, requirementlevel
FROM view_ir_dept_split
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
  AND life_cycle_code = 'COMPLETE'
UNION ALL
SELECT 'SR需求' AS `type`, bid, resource_department, life_cycle_code, irRequirementlevel
FROM view_sr_domain_status_join_rl sr
WHERE sr.delete_flag = FALSE
  AND sr.created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
UNION ALL
SELECT 'SR验收完成' AS `type`, bid, resource_department, life_cycle_code, irRequirementlevel
FROM view_sr_domain_status_join_rl sr
WHERE sr.delete_flag = FALSE
  AND sr.created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
  AND life_cycle_code = 'COMPLETE';


#领域联合查询视图分别统计完成
CREATE VIEW view_domain_union_all_complete AS
SELECT 'IR需求' AS `type`, bid, domain_id, domain_name, life_cycle_code, requirementlevel
FROM view_transcend_model_ir
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
UNION ALL
SELECT 'IR验收完成' AS `type`, bid, domain_id, domain_name, life_cycle_code, requirementlevel
FROM view_transcend_model_ir
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
  AND life_cycle_code = 'COMPLETE'
UNION ALL
SELECT 'SR需求' AS `type`, sr.bid, domain_id, sr.domain_name, sr.life_cycle_code, sr.irRequirementlevel
FROM view_sr_domain_status_join_rl sr
WHERE sr.delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
UNION ALL
SELECT 'SR验收完成' AS `type`, sr.bid, domain_id, sr.domain_name, sr.life_cycle_code, sr.irRequirementlevel
FROM view_sr_domain_status_join_rl sr
WHERE sr.delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
  AND life_cycle_code = 'COMPLETE';


CREATE VIEW view_ir_domain_status_lock AS
SELECT '其他'         AS `type`,
       domain_name,
       total - `lock` as count,
       `lock` / total AS proportion
FROM view_ir_domain_status
UNION ALL
SELECT '锁定'         AS `type`,
       domain_name,
       `lock`         as count,
       `lock` / total AS proportion
FROM view_ir_domain_status;


CREATE VIEW view_ir_domain_status_complete AS
SELECT '其他'         AS `type`,
       domain_name,
       total - complete as count,
       complete / total AS proportion
FROM view_ir_domain_status
UNION ALL
SELECT '完成'         AS `type`,
       domain_name,
       complete         as count,
       complete / total AS proportion
FROM view_ir_domain_status;


