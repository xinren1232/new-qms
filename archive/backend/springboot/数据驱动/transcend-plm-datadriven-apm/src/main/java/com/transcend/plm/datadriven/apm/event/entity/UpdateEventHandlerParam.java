package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.*;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/14
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventHandlerParam extends EventHandlerParam {

    private String  bid;

    private MSpaceAppData mSpaceAppData;

}
