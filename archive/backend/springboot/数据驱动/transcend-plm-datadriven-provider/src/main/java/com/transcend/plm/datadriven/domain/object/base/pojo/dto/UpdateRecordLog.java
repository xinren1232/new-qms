package com.transcend.plm.datadriven.domain.object.base.pojo.dto;

import lombok.Data;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class UpdateRecordLog {
    private String filed;
    private String filedName;
    private String oldValue;
    private String newValue;
}
