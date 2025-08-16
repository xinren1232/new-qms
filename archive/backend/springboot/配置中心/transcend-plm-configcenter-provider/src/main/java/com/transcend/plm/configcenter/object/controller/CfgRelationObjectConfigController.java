package com.transcend.plm.configcenter.object.controller;


import com.transcend.plm.configcenter.api.model.object.dto.ObjectRelationDTO;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.object.application.service.ICfgRelationObjectConfigAppService;
import com.transcend.plm.configcenter.api.model.object.qo.ObjectRelationQO;
import com.transcend.plm.configcenter.api.model.object.vo.*;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对象控制层
 * 只能调用AppService，不能调用实体或者仓储
 *
 * @author yikai.lian
 * @version: 1.0
 * @date 2021/01/29 18:15
 */

@RestController
public class CfgRelationObjectConfigController {

    @Resource
    private ICfgRelationObjectConfigAppService relationFlagectConfigAppService;



    public List<ObjectRelationVO> findObjectRelation(ObjectRelationQO qo) {
        return relationFlagectConfigAppService.find(qo);
    }


    public PagedResult<ObjectRelationVO> queryObjectRelation(BaseRequest<ObjectRelationQO> qo) {
        return relationFlagectConfigAppService.query(qo);
    }


    public List<ObjectRelationVO> addObjectRelation(List<ObjectRelationDTO> relationList) {
        return relationFlagectConfigAppService.add(relationList);
    }


    public Integer editObjectRelation(List<ObjectRelationDTO> relationList) {
        return relationFlagectConfigAppService.edit(relationList);

    }


    public Integer deleteObjectRelation(List<ObjectRelationDTO> relationList) {
        return relationFlagectConfigAppService.delete(relationList);

    }


    public List<ObjectRelationApplyVO> getRelationApplyInfo(String bid) {
        return relationFlagectConfigAppService.getRelationApplyAttr(bid);
    }



    public List<ObjectRelationApplyVO> getRelationExtendInfo(ObjectRelationQO objectRelationQO) {
        return relationFlagectConfigAppService.getRelationExtendInfo(objectRelationQO);

    }


    public List<ObjectRelationVO> getRelation(ObjectRelationQO relationQO) {
        return relationFlagectConfigAppService.getRelation(relationQO);
    }
}
