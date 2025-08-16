package com.transcend.plm.configcenter.api.model.dictionary.qo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("对象分页信息查询")
@Data
public class CfgDictionaryListQo extends BaseDto{
	
	@ApiModelProperty("名称")
	String name;

	@ApiModelProperty("化编码")
	Integer code;
	
	@ApiModelProperty("状态")
	Integer enableFlag;

}