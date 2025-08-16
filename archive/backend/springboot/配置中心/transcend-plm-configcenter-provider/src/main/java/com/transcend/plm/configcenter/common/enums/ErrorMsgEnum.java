package com.transcend.plm.configcenter.common.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * 错误消息枚举
 * @author zhihui.yu
 *
 */
public enum ErrorMsgEnum implements BaseEnum<String> {

	ERROR("500","系统异常！"),
	DATA_IS_WRONG("500","数据异常！"),

	/**
	 * 字典code重复异常
	 */
	DICT_ERROR_CODE_REPERT("E0101003","{config.dictionary.validation.code.NotRepeat}"),
	/**
	 * 字典名称不能为空异常
	 */
	USER_NAME_NOT_NULL("TR_CFG_DICT_001","{dict.validation.name.NotNull}"),

	/**
	 * 字典codes不能为空
	 */
	DICT_ERROR_CODES_NOT_NULL("E0101007","{config.dictionary.validation.codes.NotNull}"),

	/**
	 * 字典codes查询不能超过1000
	 */
	DICT_ERROR_CODES_LIMIT("E0101009","{config.dictionary.validation.codes.limit}"),

	CONFIG_ERROR_CODE_DUPLICATE("CONFIG_ERROR_CODE_DUPLICATE","config.validation.codes.duplicate"),
	CONFIG_ERROR_BID_BLANK("CONFIG_ERROR_BID_BLANK","config.validation.bid.blank")

	;


	String code;
	String desc;
	ErrorMsgEnum(String code,String desc){
		this.code=code;
		this.desc=desc;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getDesc() {
		return this.desc;
	}

}
