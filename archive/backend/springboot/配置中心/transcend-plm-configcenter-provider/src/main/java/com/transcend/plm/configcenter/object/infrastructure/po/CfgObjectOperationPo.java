package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author:jie.luo
 * 权限操作
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
@TableName("cfg_object_operation")
public class CfgObjectOperationPo extends BasePoEntity {
    private String code;
    @ApiModelProperty(value = "显示名称", example = "创建")
    private String name;
    @ApiModelProperty(value = "对象类型", example = "doc")
    private String baseModel;

    public static CfgObjectOperationPo of(){
        return new CfgObjectOperationPo();
    }

    private static final long serialVersionUID = 1L;
}
