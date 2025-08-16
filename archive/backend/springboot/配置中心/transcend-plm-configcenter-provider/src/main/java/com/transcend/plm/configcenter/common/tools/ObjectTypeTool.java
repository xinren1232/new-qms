package com.transcend.plm.configcenter.common.tools;

import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import org.assertj.core.util.Sets;

import java.util.Set;

/**
 * ObjectTypeTool工具箱
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/7/13 9:31
 * @since 1.0
 */
public class ObjectTypeTool {
    public static Set<String> collectObjectTypeAssemble(String objectType) {
        Set<String> collectSet = Sets.newLinkedHashSet();
        if (ObjectTypeEnum.NORMAL.getCode().equals(objectType)){
            collectSet.add(ObjectTypeEnum.NORMAL.getCode());
        }
        if (ObjectTypeEnum.VERSION.equals(objectType)){
            collectSet.add(ObjectTypeEnum.NORMAL.getCode());
            collectSet.add(ObjectTypeEnum.VERSION.getCode());
        }
        if (ObjectTypeEnum.RELATION.equals(objectType)){
            collectSet.add(ObjectTypeEnum.NORMAL.getCode());
            collectSet.add(ObjectTypeEnum.RELATION.getCode());
        }
        return collectSet;
    }
}
