package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.*;

import java.util.List;

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
public class BatchUpdateEventHandlerParam extends EventHandlerParam {

    private List<String> bids;

    private MSpaceAppData mSpaceAppData;

}
