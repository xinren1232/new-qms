package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.dto.ProjectSetQueryDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ProjectSetDemandViewVo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;
import java.util.Map;

/**
 * 项目集
 *
 * @author unknown
 */
public interface IApmProjectSetService {

    /**
     * getProjectSetDemandPage
     *
     * @param mixQoBaseRequest mixQoBaseRequest
     * @return {@link PagedResult<MObject>}
     */
    PagedResult<MObject> getProjectSetDemandPage(BaseRequest<ModelMixQo> mixQoBaseRequest);

    /**
     * getAllDemandBaseViews
     *
     * @return {@link Map<String, ProjectSetDemandViewVo>}
     */
    Map<String, ProjectSetDemandViewVo> getAllDemandBaseViews();

    /**
     * listProjectSetDemands
     *
     * @return {@link List<MObject>}
     */
    List<MObject> listProjectSetDemands();

    /**
     * getProjectDemandDetail
     *
     * @param mObject mObject
     * @return {@link List<MObjectTree>}
     */
    List<MObjectTree> getProjectDemandDetail(MObject mObject);

    /**
     * listFristProjectSetDemands
     *
     * @return {@link List<MObject>}
     */
    List<MObject> listFristProjectSetDemands();

    /**
     * getProjectSetPage
     *
     * @param projectSetQueryDto projectSetQueryDto
     * @return {@link PagedResult<MObject>}
     */
    PagedResult<MObject> getProjectSetPage(ProjectSetQueryDto projectSetQueryDto);
}
