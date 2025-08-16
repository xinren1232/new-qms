package com.transcend.plm.datadriven.infrastructure.basedata.event;

import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 底层数据事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 10:39
 */
@RequiredArgsConstructor
@ToString
public abstract class AbstractBaseDataEvent {

    /**
     * 表定义
     */
    @NonNull
    private final TableDefinition table;

    /**
     * 模型编码
     *
     * @return 模型编码
     */
    public String getModelCode() {
        return table.getModelCode();
    }

}
