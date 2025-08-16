package com.transcend.plm.alm.demandmanagement.entity.ao;

import com.transcend.plm.datadriven.apm.space.model.SpaceAppRelationAddParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @Describe 领域组件新增IR关系参数
 * @Author yuhao.qiu
 * @Date 2024/6/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class IRAddAndRelateAo {

    @NotBlank(message = "领域组件bid不能为空")
    private String domainBid;


    private SpaceAppRelationAddParam relationAddParam;
}
