package com.transcend.plm.configcenter.object.application.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectRelationDTO;
import com.transcend.plm.configcenter.api.model.object.qo.ObjectRelationQO;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectRelationApplyVO;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectRelationVO;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * IrelationFlagectConfigAppService
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 10:27
 */
public interface ICfgRelationObjectConfigAppService {

    /**
     * 批量新增
     *
     * @param relationList 关系列表
     * @return List<ObjectRelationVO> 新增结果
     * @version: 1.0
     * @date: 2021/8/30 18:46
     * @author: chaonan.xu
     */
    List<ObjectRelationVO> add(List<ObjectRelationDTO> relationList);

    /**
     * 批量编辑
     *
     * @param relationList 关系列表
     * @return Integer 更新结果
     * @version: 1.0
     * @date: 2021/8/30 18:47
     * @author: chaonan.xu
     */
    Integer edit(List<ObjectRelationDTO> relationList);

    /**
     * @param qo 查询条件
     * @return List<ObjectRelationVO> 查询结果
     * @version: 1.0
     * @date: 2021/8/30 18:47
     * @author: chaonan.xu
     */
    List<ObjectRelationVO> find(ObjectRelationQO qo);

    /**
     * @param qo 分页条件
     * @return PagedResultVO<ObjectRelationVO> 查询结果
     * @version: 1.0
     * @date: 2021/9/2 14:51
     * @author: chaonan.xu
     */
    PagedResult<ObjectRelationVO> query(BaseRequest<ObjectRelationQO> qo);

    /**
     * 删除关系
     *
     * @param relationList 关系列表
     * @return 删除结果
     * @version: 1.0
     * @date: 2021/11/17 16:10
     * @author: chaonan.xu
     */
    Integer delete(List<ObjectRelationDTO> relationList);

    /**
     * 获取对象配置的关联对象以及关系对象的属性名称版本等信息
     *
     * @param sourceObjectBid 源对象编码
     * @return List<ObjectRelationApplyVO> 关联的对象属性
     * @version: 1.0
     * @date: 2021/11/19 11:38
     * @author: chaonan.xu
     */
    List<ObjectRelationApplyVO> getRelationApplyAttr(String sourceObjectBid);


    /**
     * 获取对象配置的关联对象以及关系对象的属性名称版本等信息(继承继承版本)
     *
     * @return List<ObjectRelationApplyVO> 关联的对象属性
     * @version: 1.0
     * @date: 2021/11/19 11:38
     * @author: chaonan.xu
     */
    List<ObjectRelationApplyVO> getRelationExtendInfo(ObjectRelationQO objectRelationQO);

    /**
     * 获取对象配置的关联对象关系信息
     *
     * @param relationQO 查询实体
     * @return List<ObjectRelationApplyVO> 关联的对象属性
     * @version: 1.0
     * @date: 2022/11/29 11:38
     * @author: jinpeng.bai
     */
    List<ObjectRelationVO> getRelation(ObjectRelationQO relationQO);


    /**
     * 获取对象配置的关联对象以及关系对象的属性名称版本等信息(继承继承版本)
     *
     * @param sourceObjectBid 源对象编码
     * @return List<ObjectRelationApplyVO> 关联的对象属性
     * @version: 1.0
     * @date: 2021/11/19 11:38
     * @author: chaonan.xu
     */
    List<ObjectRelationVO> getRelationContainExtend(String sourceObjectBid, String scope);

    List<ObjectRelationVO> getObjectRelationVOsBySourceModelCode(String modelCode);
}
