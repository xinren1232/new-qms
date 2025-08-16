package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.BaseEntity;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author yuanhu.huang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_life_cycle_state")
public class LifeCycleStatePo extends BasePoEntity implements Serializable {

    private String color;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;


    /**
     * 所属组编码
     */
    private String groupCode;


    /**
     * 说明
     */
    private String description;




    /**
     * 是否被绑定，0未被绑定，1被绑定
     */
    private Boolean bindingFlag;



}
