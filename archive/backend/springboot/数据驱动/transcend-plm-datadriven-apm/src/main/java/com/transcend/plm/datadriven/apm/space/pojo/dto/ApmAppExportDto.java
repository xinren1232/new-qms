package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transsion.framework.dto.BaseRequest;
import lombok.Data;

/**
 * @author shu.zhang
 * @version 1.0
 * @className ApmAppExportDto
 * @description desc
 * @date 2024/5/31 10:00
 */
@Data
public class ApmAppExportDto {

    /**
     * 空间BID
     */
    private String spaceBid;

    /**
     * 应用BID
     */
    private String spaceAppBid;

    /**
     * 模板BID
     */
    private String templateBid;

    private String interFaceName;

    /**
     * page接口
     */
    private BaseRequest<ModelMixQo> pageRequestParam;

    /**
     * tree接口
     */
    private ModelMixQo treeRequestParam;

    /**
     * relation page接口参数
     */
    private BaseRequest<RelationMObject> relationPageRequestParam;

    /**
     * relation tree接口参数
     */
    private RelationMObject relationTreeRequestParam;

    /**
     * relation multiTree接口参数
     */
    private ApmMultiTreeDto relationMultiTreeRequestParam;

}
