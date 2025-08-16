-- 创建SR领域展平视图
CREATE VIEW view_transcend_model_sr AS
SELECT jt.domain_id, tmd.name AS domain_name, sr.*
FROM transcend_model_SR AS sr
         LEFT JOIN JSON_TABLE(belong_domain, '$[*]'
                              COLUMNS (domain_id VARCHAR(50) PATH '$')) AS jt ON TRUE
         LEFT JOIN transcend_model_DOMAIN tmd ON tmd.bid = jt.domain_id;

CREATE VIEW view_sr_dept_status AS
SELECT resource_department                                AS `dept_name`,
       COUNT(1)                                           AS `total`,
       COUNT(IF(life_cycle_code = 'SUBMIT', 1, NULL))     AS `submit`,
       COUNT(IF(life_cycle_code = 'DESIGN', 1, NULL))     AS `design`,
       COUNT(IF(life_cycle_code = 'SCHEDULING', 1, NULL)) AS `scheduling`,
       COUNT(IF(life_cycle_code = 'DEVELOP', 1, NULL))    AS `develop`,
       COUNT(IF(life_cycle_code = 'TEST', 1, NULL))       AS `test`,
       COUNT(IF(life_cycle_code = 'CHECK', 1, NULL))      AS `check`,
       COUNT(IF(life_cycle_code = 'COMPLETE', 1, NULL))   AS `complete`
FROM transcend_model_SR
WHERE delete_flag = FALSE
  AND life_cycle_code IN ('SUBMIT', 'DESIGN', 'SCHEDULING', 'DEVELOP', 'TEST', 'CHECK', 'COMPLETE')
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
GROUP BY resource_department;

CREATE VIEW view_sr_domain_status AS
SELECT domain_name                                        AS `domain_name`,
       COUNT(1)                                           AS `total`,
       COUNT(IF(life_cycle_code = 'SUBMIT', 1, NULL))     AS `submit`,
       COUNT(IF(life_cycle_code = 'DESIGN', 1, NULL))     AS `design`,
       COUNT(IF(life_cycle_code = 'SCHEDULING', 1, NULL)) AS `scheduling`,
       COUNT(IF(life_cycle_code = 'DEVELOP', 1, NULL))    AS `develop`,
       COUNT(IF(life_cycle_code = 'TEST', 1, NULL))       AS `test`,
       COUNT(IF(life_cycle_code = 'CHECK', 1, NULL))      AS `check`,
       COUNT(IF(life_cycle_code = 'COMPLETE', 1, NULL))   AS `complete`
FROM view_transcend_model_sr
WHERE life_cycle_code IN ('SUBMIT', 'DESIGN', 'SCHEDULING', 'DEVELOP', 'TEST', 'CHECK', 'COMPLETE')
  AND delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
GROUP BY domain_name;

CREATE VIEW view_sr_domain_status_join_rl AS
SELECT sr.*,ir.requirementlevel as irRequirementlevel
FROM view_transcend_model_sr sr
         LEFT JOIN transcend_model_a0d r ON r.target_bid = sr.bid AND r.delete_flag = FALSE
         LEFT JOIN transcend_model_ir ir ON ir.bid = r.source_bid AND ir.delete_flag = FALSE
WHERE sr.delete_flag = FALSE

