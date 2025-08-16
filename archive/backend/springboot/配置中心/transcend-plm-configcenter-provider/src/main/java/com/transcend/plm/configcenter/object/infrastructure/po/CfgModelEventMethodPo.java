package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * ModelEventMethodPO
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/18 10:06
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("cfg_object_event_method")
public class CfgModelEventMethodPo extends BasePoEntity {
    public static CfgModelEventMethodPo of(){
        return  new CfgModelEventMethodPo();
    }

    /**
     * 事件bid
     */
    private String eventBid;
    /**
     * 描述
     */
    private String description;
    /**
     * 对象bid
     */
    private String modelBid;
    /**
     * 对象code
     */
    private String modelCode;
    /**
     * 方法bid
     */
    private String methodBid;
    /**
     * 执行类型，1前置，2后置
     */
    private Integer executeType;
}
