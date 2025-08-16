package com.transcend.plm.datadriven.apm.space.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Mickey Qiu
 * @desc
 * @date 2025/3/15
 */
@Accessors
@Data(staticConstructor = "of")
public class RelationActionPermission {

    private List<String> roleBids;

    private List<FieldConditionParam> fieldConditionParams;
}
