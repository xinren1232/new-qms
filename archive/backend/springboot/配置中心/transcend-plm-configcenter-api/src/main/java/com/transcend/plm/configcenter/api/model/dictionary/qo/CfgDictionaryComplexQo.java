package com.transcend.plm.configcenter.api.model.dictionary.qo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author jie.luo1
 */
@ApiModel("对象分页信息查询")
@Data
public class CfgDictionaryComplexQo extends CfgDictionaryQo{


	@ApiModelProperty("状态-集合")
	List<Integer> enableFlags;

	@ApiModelProperty("编码-集合")
	List<String> codes;

}