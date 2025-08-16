package com.transcend.plm.configcenter.api.model.dictionary.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("对象信息")
@Data
public class CfgDictionaryVo extends BaseDto {


	/**
	 * 字典权限范围
	 */
	@ApiModelProperty("字典权限范围")
	private String permissionScope;

	/**
	 * 分组
	 */
	@ApiModelProperty("分组")
	private String groupName;

	/**
	 * 类型：list 枚举  tree tree
	 */
	private String type;

	/**
	 * 代码(接口查询对应的code)
	 */
	private String code;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 描述
	 */
	private String description;

	/**
	 * 排序号
	 */
	private Integer sort;
	/**
	 * 版本（修改了key表的属性或者value 下面的值都会更改版本，使版本加一）
	 */
	private Integer version;

	private String custom1;

	private String custom2;

	private String custom3;

	List<CfgDictionaryItemVo> dictionaryItems;
}