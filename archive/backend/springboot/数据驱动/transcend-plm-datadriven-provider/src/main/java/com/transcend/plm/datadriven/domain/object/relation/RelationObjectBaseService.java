package com.transcend.plm.datadriven.domain.object.relation;

import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import com.transcend.plm.datadriven.domain.object.version.VersionObjectManageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 关系对象-基础服务（承接 与仓储层得转换）
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
public class RelationObjectBaseService {

    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Resource
    private VersionObjectManageService versionObjectManageService;



}
