package com.transcend.plm.datadriven.filemanager.pojo.ao;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author bin.yin
 * @date 2024/04/29 18:03
 */
@Data
public class FileCopyExecutionAo {

    @NotEmpty(message = "执行状态不能为空")
    private List<Integer> executeState;

    private String fileName;

    private String copyRuleName;

    private List<String> fileTypeCodes;

    private String executeStartTime;

    private String executeEndTime;
}
