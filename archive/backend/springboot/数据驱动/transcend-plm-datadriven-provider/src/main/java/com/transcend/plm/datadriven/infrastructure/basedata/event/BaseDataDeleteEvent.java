package com.transcend.plm.datadriven.infrastructure.basedata.event;

import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

/**
 * 底层数据删除事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 10:53
 */
@Getter
@ToString(callSuper = true)
public class BaseDataDeleteEvent extends AbstractBaseDataEvent {

    /**
     * 删除条件
     */
    private final List<QueryWrapper> wrappers;

    /**
     * 是否逻辑删除
     */
    private final boolean isLogicalDelete;

    public BaseDataDeleteEvent(@NonNull TableDefinition table, List<QueryWrapper> wrappers, boolean isLogicalDelete) {
        super(table);
        this.wrappers = wrappers;
        this.isLogicalDelete = isLogicalDelete;
    }
}
