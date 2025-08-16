package com.transcend.plm.datadriven.common.pojo.dto;

import com.transcend.plm.datadriven.api.model.MObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/05/29 17:19
 */
@Data
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class NotifyPreEventBusDto {
    private String modelCode;

    private List<? extends MObject> dataList;

    private MObject data;

    private String jobNumber;
}
