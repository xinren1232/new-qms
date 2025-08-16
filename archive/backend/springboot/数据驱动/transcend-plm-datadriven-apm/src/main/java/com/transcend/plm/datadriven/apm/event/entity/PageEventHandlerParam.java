package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transsion.framework.dto.BaseRequest;
import lombok.*;

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
public class PageEventHandlerParam extends EventHandlerParam {

    private BaseRequest<QueryCondition> pageQo;

    private Boolean filterRichText;
}
