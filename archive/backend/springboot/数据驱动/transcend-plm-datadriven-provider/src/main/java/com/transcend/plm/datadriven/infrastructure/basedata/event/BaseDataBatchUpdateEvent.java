package com.transcend.plm.datadriven.infrastructure.basedata.event;

import com.transcend.plm.datadriven.api.model.BatchUpdateBO;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

/**
 * 底层数据批量更新事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 10:39
 */
@Getter
@ToString(callSuper = true)
public class BaseDataBatchUpdateEvent extends AbstractBaseDataEvent {

    /**
     * 更新对象
     */
    private final List<? extends BatchUpdateBO<? extends MBaseData>> updateList;

    public BaseDataBatchUpdateEvent(@NonNull TableDefinition table,
                                    List<? extends BatchUpdateBO<? extends MBaseData>> updateList) {
        super(table);
        this.updateList = updateList;
    }

}
