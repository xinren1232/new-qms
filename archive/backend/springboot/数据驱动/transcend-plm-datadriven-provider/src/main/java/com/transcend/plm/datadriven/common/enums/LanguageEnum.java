package com.transcend.plm.datadriven.common.enums;

import lombok.Getter;

/**
 * 语言枚举
 * @author yinbin
 * @version:
 * @date 2023/10/08 16:19
 */
@Getter
public enum LanguageEnum {
    /**
     * zh
     */
    ZH("zh"),
    /**
     * en
     */
    EN("en")
    ;

    LanguageEnum(String code) {
        this.code = code;
    }

    /**
     * code
     */
    private final String code;
}
