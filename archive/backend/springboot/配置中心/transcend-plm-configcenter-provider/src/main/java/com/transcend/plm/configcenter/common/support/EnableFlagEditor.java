package com.transcend.plm.configcenter.common.support;

import java.beans.PropertyEditorSupport;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/3/13 16:38
 * @since 1.0
 */
public class EnableFlagEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            Integer value = Integer.valueOf(text);
            setValue(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid enableFlag value: " + text, e);
        }
    }
}
