package com.transcend.plm.datadriven.api.model.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @BelongsProject: transcend-plm-datadriven
 * @BelongsPackage: com.transcend.plm.datadriven.api.model.vo
 * @Author: WWX
 * @CreateTime: 2024-08-27  11:06
 * @Description: TODO
 * @Version: 1.0
 */
@Getter
@Setter
@ToString
public class ResponseVO<T> {
    private String code;
    private String message;
    private T data;
}
