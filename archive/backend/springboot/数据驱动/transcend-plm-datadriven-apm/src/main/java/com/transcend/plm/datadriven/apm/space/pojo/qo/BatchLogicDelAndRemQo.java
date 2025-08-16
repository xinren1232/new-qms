package com.transcend.plm.datadriven.apm.space.pojo.qo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/3/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BatchLogicDelAndRemQo {

    private String sourceBid;


    /**
     * 实例Bid
     */
    private List<String> insBids;

    /**
     * 关系Bid
     */
    private List<String> relationBids;

    /**
     * 关系ModelCode
     */
    private String relationModelCode;

    /**
     * 关系TargetModelCode
     */
    private String relationSourceModelCode;

    /**
     * 是否是Tab页操作
     */
    private boolean isTabOperation;

    /**
     * 关系Tab页Id
     */
    private String tabId;


    /**
     * 对象编码
     */
    private String modelCode;

    public boolean isTabOperation() {
        return this.isTabOperation;
    }

    public BatchLogicDelAndRemQo setTabOperation(final boolean isTabOperation) {
        this.isTabOperation = isTabOperation;
        return this;
    }

    public BatchLogicDelAndRemQo setIsTabOperation(final boolean isTabOperation) {
        this.isTabOperation = isTabOperation;
        return this;
    }
}
