package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.*;

import java.util.List;

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
public class BatchAddEventHandlerParam extends EventHandlerParam {

    private List<MSpaceAppData> mSpaceAppDataList;


}
