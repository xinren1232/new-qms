package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/17
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchDeleteEventHandlerParam extends EventHandlerParam {


    private List<String> bids;

    // 用于处理IR对象
   private Map<String,List<MObject>> irMap;
}
