package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.space.model.ApmSpaceAppDataDrivenOperationFilterBo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiObjectUpdateDto;
import lombok.*;

import java.util.List;

/**
 * @author bin.yin
 * @description: 新增实例事件参数
 * @version:
 * @date 2024/06/13 15:32
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultiObjectUpdateEventHandlerParam extends EventHandlerParam {
    private List<MultiObjectUpdateDto> multiObjectUpdateDtoList;

}
