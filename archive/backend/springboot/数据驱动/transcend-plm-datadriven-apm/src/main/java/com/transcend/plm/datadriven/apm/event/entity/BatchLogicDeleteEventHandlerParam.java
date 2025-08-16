package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.space.pojo.qo.BatchLogicDelAndRemQo;
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
public class BatchLogicDeleteEventHandlerParam extends EventHandlerParam {


    private BatchLogicDelAndRemQo qo;

}
