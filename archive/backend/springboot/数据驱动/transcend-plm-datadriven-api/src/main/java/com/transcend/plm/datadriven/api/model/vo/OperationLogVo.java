package com.transcend.plm.datadriven.api.model.vo;

import com.transcend.plm.datadriven.api.model.MBaseData;
import lombok.Data;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class OperationLogVo {
    private String date;
    private List<MBaseData> data;
}
