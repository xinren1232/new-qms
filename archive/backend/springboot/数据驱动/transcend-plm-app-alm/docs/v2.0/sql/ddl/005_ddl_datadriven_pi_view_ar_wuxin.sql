#创建AR领域展平视图
CREATE VIEW view_transcend_model_ar AS
SELECT jt.domain_id, tmd.name AS domain_name, ar.*
FROM transcend_model_AR AS ar
         LEFT JOIN JSON_TABLE(belong_domain, '$[*]'
                              COLUMNS (domain_id VARCHAR(50) PATH '$')) AS jt ON TRUE
         LEFT JOIN transcend_model_DOMAIN tmd ON tmd.bid = jt.domain_id;

