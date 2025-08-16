package com.transcend.plm.datadriven.domain.object.base.pojo.dto;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import lombok.Data;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class MutiBaseClassDto {
    private String baseModel;
    private MObject mObject;
    private List<QueryWrapper> wrappers;
}
