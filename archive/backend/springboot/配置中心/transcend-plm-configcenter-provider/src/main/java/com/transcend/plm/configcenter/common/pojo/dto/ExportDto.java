package com.transcend.plm.configcenter.common.pojo.dto;

import lombok.Data;

@Data
public class ExportDto {
    //导出excel名称
    private String title;
    //对应bean名字
    private String beanName;
    //导出方法需要的参数
    private Object param;
    //是否是导出模板
    private boolean template;
}
