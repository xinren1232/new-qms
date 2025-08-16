package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo;

import lombok.Data;

import java.util.List;
@Data
public class LifeCycleStateImportVo {
    List<String> headList ;
    List<LifeCycleStateExcelVO> values;
    private boolean result;
}
