package com.transcend.plm.configcenter.dictionary.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * ObjectModelPO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 15:59
 */
@Data
@TableName("cfg_dictionary")
public class CfgDictionaryPo  extends BasePoEntity implements Serializable {

    /**
     * 字典权限范围
     */
    private String permissionScope;

    /**
     * 分组编码
     */
    private String groupName;
    /**
     * 名称
     */
    private String name;

    /**
     * 类型：list 枚举  tree tree
     */
    private String type;

    /**
     * 代码(接口查询对应的code)
     */
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

    private static final long serialVersionUID = 1L;

}
