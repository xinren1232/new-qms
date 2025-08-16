package com.transcend.plm.configcenter.api.model.view.dto;

import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 视图属性
 * @createTime 2023-12-05 11:19:00
 */
@Data
public class ViewField {

    private String id;
    private String icon;
    private String type;
    private ViewOptions options;
    private boolean formItemFlag;
}