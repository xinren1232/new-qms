package com.transcend.plm.datadriven.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * ModelQueryWrapper
 *
 * @author leigang.yang
 * @date 2022/10/12 09:40
 */

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Slf4j
public class ModelQueryWrapper extends QueryWrapper {


    private Boolean relation;


}
