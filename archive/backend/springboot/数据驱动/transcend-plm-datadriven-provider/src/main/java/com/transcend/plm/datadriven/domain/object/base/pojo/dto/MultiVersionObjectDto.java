package com.transcend.plm.datadriven.domain.object.base.pojo.dto;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import lombok.Data;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class MultiVersionObjectDto {
    private String baseModel;
    private MVersionObject mObject;
    private List<QueryWrapper> wrappers;
    private List<MVersionObject> mBaseDataList;
    private String dataBid;
}

