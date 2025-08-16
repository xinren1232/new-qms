package com.transcend.plm.configcenter.common.pojo.dto;

import lombok.Data;

@Data
public class ImportDto {
    //对应bean名字
    private String beanName;
    //导出方法需要的参数
    private Object param;

}
