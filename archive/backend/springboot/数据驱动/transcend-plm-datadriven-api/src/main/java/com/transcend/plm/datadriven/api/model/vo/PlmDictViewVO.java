package com.transcend.plm.datadriven.api.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 视图级联字段需要的VO
 * @author quan.cheng
 * @title DictViewVO
 * @date 2024/3/4 11:06
 */
@Data
public class PlmDictViewVO {
    private String label;
    private String value;
    /**
     * 子数据
     */
    private List<PlmDictViewVO> children;
}
