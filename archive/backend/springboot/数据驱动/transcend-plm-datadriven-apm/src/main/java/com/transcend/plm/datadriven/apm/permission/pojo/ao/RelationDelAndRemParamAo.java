package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Qiu Yuhao
 * @Date 2024/1/22 15:01
 * @Describe 用于删除和移除关系的参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RelationDelAndRemParamAo extends RelationBaseParamAo{

    private String modelCode;

    private String sourceBid;

    private String viewModel;

    private List<String> targetBids;

    private List<String> relationBids;
}
