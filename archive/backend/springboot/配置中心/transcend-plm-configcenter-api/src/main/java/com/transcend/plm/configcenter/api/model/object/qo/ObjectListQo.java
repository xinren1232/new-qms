package com.transcend.plm.configcenter.api.model.object.qo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@ApiModel("对象分页信息查询")
@Data
public class ObjectListQo extends BaseDto {
	
	@ApiModelProperty("名称")
	String name;

	@ApiModelProperty("模型优化编码")
	Integer modelCode;
	
	@ApiModelProperty("状态")
	@Min(1)
	@Max(5)
	Integer enableFlag;
	
//	@ApiModelProperty("数量")
//	Integer qty;
//
//	@ApiModelProperty("测试正则")
//	@EmailDesensitize
//	@Email
//	@NotBlank
	String email;
	
	//@ApiModelProperty("测试正则")
	//@Pattern(regexp ="\\d+",message="test {tr.validation.number.message}")
	//String test;
}