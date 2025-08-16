package com.transcend.plm.datadriven.apm.event.entity;

import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import lombok.*;

import java.util.List;

/**
 * @author bin.yin
 * @description: 批量取消检出事件入参
 * @version:
 * @date 2024/06/17 10:49
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchCancelCheckOutHandlerParam extends EventHandlerParam {
    private List<String> bids;
}
