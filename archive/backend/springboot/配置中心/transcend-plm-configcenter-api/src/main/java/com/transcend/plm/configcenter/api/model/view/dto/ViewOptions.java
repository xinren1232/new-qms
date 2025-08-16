package com.transcend.plm.configcenter.api.model.view.dto;

import lombok.Data;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 视图自定义选择项
 * @createTime 2023-12-05 11:20:00
 */
@Data
public class ViewOptions {
    private String name;
    private String size;
    private String type;
    private String label;
    private boolean hidden;
    private boolean multiple;
    private String onBlur;
    private String remoteDictType;
    private String onFocus;
    private String onInput;
    private boolean disabled;
    private String onChange;
    private boolean readonly;
    private boolean required;
    private boolean clearable;
    private String onCreated;
    private String onMounted;
    private String buttonIcon;
    private String labelAlign;
    private String onValidate;
    private String prefixIcon;
    private String suffixIcon;
    private String validation;
    private String columnWidth;
    private String customClass;
    private boolean labelHidden;
    private String placeholder;
    private boolean appendButton;
    private String defaultValue;
    private String requiredHint;
    private boolean showPassword;
    private boolean showWordLimit;
    private String validationHint;
    private String labelIconPosition;
    private String onAppendButtonClick;
    private boolean appendButtonDisabled;
    private List<OptionItem> optionItems;
    /**
     * 增加关系参数
     */
    private String relation;
    private RelationInfo relationInfo;
    private SourceModelCodeInfo sourceModelCodeInfo;
}