package com.transcend.plm.datadriven.apm.space.pojo.bo;

import lombok.Builder;
import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 字段更新事件
 * @createTime 2023-11-02 10:06:00
 */
@Data
@Builder
public class FieldUpdateEvent {
    private String instanceBid;
    private String fieldName;
    private String spaceAppBid;
    private String spaceBid;
    private Object fieldValue;
    private Object fieldValueOld;
}
