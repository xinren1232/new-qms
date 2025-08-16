package com.transcend.plm.datadriven.apm.log.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 操作日志ES数据
 * @author yinbin
 * @version:
 * @date 2023/09/28 09:18
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class OperationLogEsData extends BaseEsData {

    /**
     * 空间id
     */
    private String spaceBid;

    /**
     * 空间应用id
     */
    private String spaceAppBid;

    /**
     * 对象modelCode
     */
    private String modelCode;

    /**
     * 扩展json字符串,存入修改属性的视图配置和前后值
     */
    private String ext;

    public static OperationLogEsData of() {
        return new OperationLogEsData();
    }

}
