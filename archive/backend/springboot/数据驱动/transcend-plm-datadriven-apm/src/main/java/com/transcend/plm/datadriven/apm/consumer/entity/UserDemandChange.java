package com.transcend.plm.datadriven.apm.consumer.entity;

import lombok.Data;

/**
 * @author yinbin
 * @version:
 * @date 2023/11/01 20:39
 */
@Data
public class UserDemandChange {
    private String newValue;
    private String oldValue;
}
