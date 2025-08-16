package com.transcend.plm.datadriven.apm.tools;

import com.alibaba.fastjson.TypeReference;
import com.transcend.plm.datadriven.apm.space.pojo.dto.RelationActionPermission;

import java.util.List;

/**
 * @author unknown
 */
public class RelationActionPermissionListTypeHandler extends ListTypeHandler<RelationActionPermission> {

    @Override
    protected TypeReference<List<RelationActionPermission>> specificType() {
        return new TypeReference<List<RelationActionPermission>>() {
        };
    }

}