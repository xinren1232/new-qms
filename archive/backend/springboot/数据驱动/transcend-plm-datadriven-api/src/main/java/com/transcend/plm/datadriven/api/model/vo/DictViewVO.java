package com.transcend.plm.datadriven.api.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @BelongsProject: transcend-plm-datadriven
 * @BelongsPackage: com.transcend.plm.datadriven.api.model.vo
 * @Author: WWX
 * @CreateTime: 2024-08-27  10:40
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class DictViewVO {
    private String label;
    private String value;
    /**
     * 子数据
     * */
    private List<DictViewVO> children;
}
