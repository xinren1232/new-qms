package com.transcend.plm.datadriven.apm.flow.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Describe Apm启动流程QO
 * @Author yuhao.qiu
 * @Date 2024/3/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)

public class ApmFlowUserDto {

    /**
     * 空间Bid
     */
    private String spaceBid;

    /**
     * 空间应用Bid
     */
    private String spaceAppBid;

    /**
     * 人员列表
     */
    List<String> roleUserList;
}
