package com.transcend.plm.datadriven.common.dynamic.fields.lang;

import cn.hutool.core.date.DatePattern;
import com.transcend.plm.datadriven.common.dynamic.fields.DynamicFields;
import com.transsion.framework.common.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * 当前时间
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 11:11
 */
@Component
public class CurrentDateTime implements DynamicFields {

    @Override
    public String getCode() {
        return "NOW_TIME";
    }

    @Override
    public String getName() {
        return "当前时间";
    }

    @Override
    public String getValue() {
        return DateUtil.getCurDateByPattern(DatePattern.NORM_DATETIME_PATTERN);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
