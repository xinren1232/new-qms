package com.transcend.plm.datadriven.infrastructure.basedata.event;

import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import lombok.Getter;
import lombok.ToString;

/**
 * 底层数据新增事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 10:38
 */
@Getter
@ToString(callSuper = true)
public class BaseDataAddEvent extends AbstractBaseDataEvent {

    /**
     * 新增的数据
     */
    private final MBaseData data;

    public BaseDataAddEvent(TableDefinition table, MBaseData data) {
        super(table);
        this.data = data;
    }
}
