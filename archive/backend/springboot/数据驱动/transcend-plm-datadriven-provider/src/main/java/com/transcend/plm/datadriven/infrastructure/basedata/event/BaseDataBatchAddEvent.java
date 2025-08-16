package com.transcend.plm.datadriven.infrastructure.basedata.event;

import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 底层数据批量新增事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 10:38
 */
@Getter
@ToString(callSuper = true)
public class BaseDataBatchAddEvent extends AbstractBaseDataEvent {

    /**
     * 批量更新的数据
     */
    private final List<? extends MBaseData> dataList;

    public BaseDataBatchAddEvent(TableDefinition table, List<? extends MBaseData> dataList) {
        super(table);
        this.dataList = dataList;
    }
}
