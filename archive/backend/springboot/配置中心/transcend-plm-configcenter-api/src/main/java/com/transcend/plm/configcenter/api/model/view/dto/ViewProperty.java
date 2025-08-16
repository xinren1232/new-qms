package com.transcend.plm.configcenter.api.model.view.dto;

import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 视图属性
 * @createTime 2023-12-05 15:01:00
 */
@Data
public class ViewProperty {
    private String name;
    private String type;
    private ViewField field;
}
