DELIMITER $$

CREATE PROCEDURE UpdateParentHasChild(
    IN parent_table_name VARCHAR(255),
    IN child_table_name VARCHAR(255),
    IN rel_table_name VARCHAR(255),
    IN has_child_column VARCHAR(255)
)
BEGIN
    SET @sql = CONCAT(
            'UPDATE ', parent_table_name, ' parent ',
            'LEFT JOIN ( ',
            '    SELECT rel.source_bid, COUNT(rel.target_bid) > 0 AS has_child ',
            '    FROM ', child_table_name, ' child ',
            '    LEFT JOIN ', rel_table_name, ' rel ON rel.target_bid = child.bid AND rel.delete_flag = 0 ',
            '    WHERE child.delete_flag = 0 ',
            '    GROUP BY rel.source_bid ',
            ') AS child ON parent.bid = child.source_bid ',
            'SET parent.', has_child_column, ' = if(child.has_child,''Y'' ,''N'') ',
            'WHERE parent.delete_flag = 0'
               );

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END$$

DELIMITER ;

DELIMITER $$

CREATE EVENT IF NOT EXISTS UpdateIrHasChildEvent
    ON SCHEDULE EVERY 6 HOUR
        STARTS '2025-06-19 12:00:00'
    DO
BEGIN
CALL UpdateParentHasChild('transcend_model_ir', 'transcend_model_sr', 'transcend_model_a0d', 'whether_to_break_down');
END$$

DELIMITER ;

DELIMITER $$

CREATE EVENT IF NOT EXISTS UpdateSrHasChildEvent
    ON SCHEDULE EVERY 6 HOUR
        STARTS '2025-06-19 12:00:00'
    DO
    BEGIN
        CALL UpdateParentHasChild('transcend_model_sr', 'transcend_model_ar', 'transcend_model_a0e', 'whether_to_break_down');
    END$$

DELIMITER ;


DELIMITER $$

CREATE EVENT IF NOT EXISTS UpdateRrHasChildEvent
    ON SCHEDULE EVERY 6 HOUR
        STARTS '2025-06-19 12:00:00'
    DO
    BEGIN
        CALL UpdateParentHasChild('transcend_model_rr', 'transcend_model_ir', 'transcend_model_a0a', 'whether_to_break_down');
    END$$

DELIMITER ;

