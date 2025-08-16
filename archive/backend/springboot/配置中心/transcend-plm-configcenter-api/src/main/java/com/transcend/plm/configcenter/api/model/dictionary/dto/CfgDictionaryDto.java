package com.transcend.plm.configcenter.api.model.dictionary.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@ApiModel("字典信息")
@Data
public class CfgDictionaryDto implements Serializable {

	/**
	 * 业务id
	 */
	@ApiModelProperty("业务id")
	private String bid;


	@ApiModelProperty("名称")
	@NotBlank(message="test {config.dictionary.query.key.message}")
	String name;

	/**
	 * 类型：list 枚举   tree
	 */
	@ApiModelProperty("类型：list 枚举   tree")
	private String type;

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
	 * 代码(接口查询对应的code)
	 */
	@ApiModelProperty("编码")
	private String code;

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

	/**
	 * 启用  禁用
	 */
	private Integer enableFlag;
	/**
	 * 删除标识
	 */
	private Integer deleteFlag;

    /**
     * 字典条目
     */
    private List<CfgDictionaryItemDto> dictionaryItems;

}