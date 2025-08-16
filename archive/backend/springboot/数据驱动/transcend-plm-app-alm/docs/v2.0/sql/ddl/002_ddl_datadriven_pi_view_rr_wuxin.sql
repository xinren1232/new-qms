#创建RR领域展平视图
CREATE VIEW view_transcend_model_rr AS
SELECT jt.domain_id, tmd.name AS domain_name, rr.*
FROM transcend_model_RR AS rr
         LEFT JOIN JSON_TABLE(belong_domain, '$[*]'
                              COLUMNS (domain_id VARCHAR(50) PATH '$')) AS jt ON TRUE
         LEFT JOIN transcend_model_DOMAIN tmd ON tmd.bid = jt.domain_id;

#创建RR状态基础视图
CREATE VIEW view_rr_dept_status AS
SELECT first_depart_sub_id as `dept_id`,
       first_depart_sub    as `dept_name`,
       life_cycle_code     as `status`,
       whether_to_return   as `is_refuse`
FROM transcend_model_RR
WHERE delete_flag = FALSE
  AND created_time >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR);

#统计RR部门汇总统计数量
CREATE VIEW view_rr_dept_summary_count AS
SELECT dept_name,
       COUNT(1)                                               AS `total`,
       COUNT(IF(is_refuse IS NULL OR is_refuse = 0, 1, NULL)) AS `accept`,
       COUNT(IF(is_refuse = 1, 1, NULL))                      AS `refuse`
FROM view_rr_dept_status
where status IN ('PRELIMINARY', 'SUSPEND', 'ANALYSIS', 'DISTRIBUTE', 'ACHIEVE', 'CHECK', 'CLOSE')
GROUP BY dept_name;

#统计RR部门各状态统计数量
CREATE VIEW view_rr_dept_status_summary_count AS
SELECT dept_name,
       COUNT(1)                                   AS `total`,
       COUNT(IF(status = 'PRELIMINARY', 1, NULL)) AS `preliminary`,
       COUNT(IF(status = 'SUSPEND', 1, NULL))     AS `suspend`,
       COUNT(IF(status = 'ANALYSIS', 1, NULL))    AS `analysis`,
       COUNT(IF(status = 'DISTRIBUTE', 1, NULL))  AS `distribute`,
       COUNT(IF(status = 'ACHIEVE', 1, NULL))     AS `achieve`,
       COUNT(IF(status = 'CHECK', 1, NULL))       AS `check`,
       COUNT(IF(status = 'CLOSE', 1, NULL))       AS `close`
FROM view_rr_dept_status
WHERE status IN ('PRELIMINARY', 'SUSPEND', 'ANALYSIS', 'DISTRIBUTE', 'ACHIEVE', 'CHECK', 'CLOSE')
  AND (is_refuse IS NULL OR is_refuse = 0)
GROUP BY dept_name;

