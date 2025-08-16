package com.transcend.plm.datadriven.infrastructure.basedata.event;

import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

/**
 * 底层数据更新事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 10:43
 */
@Getter
@ToString(callSuper = true)
public class BaseDataUpdateEvent extends AbstractBaseDataEvent {

    /**
     * 跟新的数据
     */
    private final MBaseData data;
    /**
     * 更新条件
     */
    private final List<QueryWrapper> wrappers;

    public BaseDataUpdateEvent(@NonNull TableDefinition table, MBaseData data, List<QueryWrapper> wrappers) {
        super(table);
        this.data = data;
        this.wrappers = wrappers;
    }
}
