package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.web.exception.ExceptionConstructor;
import com.transcend.plm.configcenter.object.domain.aggergate.CfgObjectAttrRoot;
import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectMapper;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectHistoryMapper;
import com.transcend.plm.configcenter.object.pojo.CfgObjectConverter;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.ObjectUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态模型资源库
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2021/9/22 17:21
 * @since 1.0
 */
@Slf4j
@Repository
public class CfgObjectRepository {

    @Resource
    private CfgObjectMapper cfgObjectMapper;

    @Resource
    private ObjectHistoryMapper objectHistoryMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 查询所有对象
     *
     * @param
     * @return
     * @version: 1.0
     * @date: 2022/11/24 17:07
     * @author: jingfang.luo
     */
    public List<CfgObjectPo> findAll() {
        return cfgObjectMapper.findAll();
    }

    /**
     * 查询出所有子对象（包含本身）
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/13 14:17
     * @author: jingfang.luo
     */
    public List<CfgObjectPo> findAllChildrenByModelCode(String modelCode) {
        return cfgObjectMapper.findAllChildrenByModelCode(modelCode);
    }

    /**
     * 对象新增
     *
     * @param po
     * @return
     * @version: 1.0
     * @date: 2022/11/28 10:19
     * @author: jingfang.luo
     */
    public Boolean add(CfgObjectPo po) {
        return transactionTemplate.execute(transactionStatus -> {
            //新增新表
            Boolean insert = cfgObjectMapper.insert(po) > 0;
            //新增历史表
            Boolean historyInsert = objectHistoryMapper.insert(po) > 0;
            log.info("执行对象新增操作，新增新表结果：{}；新增历史表结果：{}", insert, historyInsert);
            return insert && historyInsert;
        });
    }

    /**
     * 新增对象和属性(BOM使用，暂未考虑通用)
     *
     * @param vo vo
     * @return
     * @version: 1.0
     * @date: 2023/1/9 10:01
     * @author: bin.yin
     */
    public Boolean addObjectAndAttr(CfgObject vo) {
        CfgObjectPo po = BeanUtil.copy(vo, CfgObjectPo.class);
        return transactionTemplate.execute(transactionStatus -> {
            // 新增对象新表
            Boolean insert = cfgObjectMapper.insert(po) > 0;
            // 新增对象历史表
            Boolean historyInsert = objectHistoryMapper.insert(po) > 0;
            log.info("执行对象新增操作，新增新表结果：{}；新增历史表结果：{}", insert, historyInsert);
            // 新增属性
            CfgObjectAttrRoot.build().checkin(vo);
            return insert && historyInsert;
        });
    }


    /**
     * 查询根节点的最大modelCode
     *
     * @param
     * @return
     * @version: 1.0
     * @date: 2022/12/8 18:01
     * @author: jingfang.luo
     */
    public String findMaxModelCodeInRoot() {
        return cfgObjectMapper.findMaxModelCodeInRoot();
    }

    /**
     * 根据父的modelCode查询子中modelCode最大的值
     *
     * @param parentCode
     * @return
     * @version: 1.0
     * @date: 2022/11/29 10:20
     * @author: jingfang.luo
     */
    public String findMaxModelCodeByParentCode(String parentCode) {
        return cfgObjectMapper.findMaxModelCodeByParentCode(parentCode);
    }

    /**
     * 对象删除
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/11/29 10:20
     * @author: jingfang.luo
     */
    public Boolean delete(String modelCode) {
        return cfgObjectMapper.delete(modelCode) > 0;
    }

    /**
     * 根据modelCodeList删除对象
     *
     * @param modelCodeList
     * @return
     * @version: 1.0
     * @date: 2022/12/13 14:52
     * @author: jingfang.luo
     */
    public Boolean deleteByList(List<String> modelCodeList) {
        return cfgObjectMapper.deleteByModelCodes(modelCodeList) > 0;
    }

    /**
     * 对象编辑 - 不会升版本
     *
     * @param poList
     * @return
     * @version: 1.0
     * @date: 2022/11/29 23:15
     * @author: jingfang.luo
     */
    public Boolean batchUpdateAndHistory(List<CfgObjectPo> poList) {
        return transactionTemplate.execute(transactionStatus -> {
            //更新新表
            Boolean update = cfgObjectMapper.bulkUpdate(poList) > 0;
            //更新历史表 -- 不会升版，所以直接更新历史表，不插入历史表，但是历史表需要查出对象的对应版本
            //获取父modelVersionCode
            CfgObjectPo po = poList.get(0);
            String modelCode = po.getModelCode();
            String parentMvc = cfgObjectMapper.find(modelCode).getParentModelVersionCode();
            //根据父查出所有子对象 设置bid bid的唯一性更高，防止修改了相同的历史数据（固定bid会存在覆盖数据的问题）
            List<CfgObjectPo> allChildren = cfgObjectMapper.findAllChildrenByParentMvc(parentMvc);
            Map<String, String> codeAndBidMap = allChildren.stream()
                    .collect(Collectors.toMap(CfgObjectPo::getModelCode, CfgObjectPo::getBid));
            for (CfgObjectPo object : poList) {
                object.setBid(codeAndBidMap.get(object.getModelCode()));
            }
            //更新历史表
            Boolean updateHistory = objectHistoryMapper.batchUpdate(poList) > 0;

            log.info("执行批量编辑对象操作，更新新表结果：{}；更新历史表结果：{}", update, updateHistory);
            return update && updateHistory;
        });
    }

    /**
     * 根据modelVersionCode查询对象
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/1 16:01
     * @author: jingfang.luo
     */
    public CfgObjectPo getByModelCode(String modelCode) {
        return cfgObjectMapper.find(modelCode);
    }

    /**
     * 更新子对象的父版本 && 把最新的子数据复制一份到历史表 - 应用于检入
     *
     * @param object
     * @return
     * @version: 1.0
     * @date: 2022/12/7 10:22
     * @author: jingfang.luo
     */
    public Boolean updateChildrenAndCopyToHistory(CfgObjectPo object) {
        return transactionTemplate.execute(transactionStatus -> {
            String parentModelVersionCodeForChildren = object.getModelVersionCode();
            Integer newParentVersion = object.getVersion();
            String oldParentVersionCodeForChildren = object.getModelCode() + ":" + (newParentVersion - 1);
            Boolean update = cfgObjectMapper.updateChildren(oldParentVersionCodeForChildren, parentModelVersionCodeForChildren,
                    newParentVersion, SsoHelper.getJobNumber()) > 0;
            //把最新的子数据复制一份到历史表
            Boolean copy = objectHistoryMapper.copyToHistory(parentModelVersionCodeForChildren) > 0;
            log.info("执行更新子对象操作，更新父版本结果：{}；新增历史表结果：{}", update, copy);
            return update && copy;
        });
    }

    /**
     * 根据modelCode查版本
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/8 11:07
     * @author: jingfang.luo
     */
    public Integer findVersionByModelCode(String modelCode) {
        return cfgObjectMapper.find(modelCode).getVersion();
    }

    /**
     * 根据ParentModelVersionCode查询子对象的个数
     *
     * @param parentMvc
     * @return
     * @version: 1.0
     * @date: 2022/12/8 14:47
     * @author: jingfang.luo
     */
    public Integer findSortByParentMvc(String parentMvc) {
        return cfgObjectMapper.findAllChildrenByParentMvc(parentMvc).size();
    }

    /**
     * 根据对象name查询对象的个数
     *
     * @param name
     * @return
     * @version: 1.0
     * @date: 2022/12/8 15:40
     * @author: jingfang.luo
     */
    public Integer findObjectCountByName(String name) {
        return cfgObjectMapper.findListByNameList(Lists.newArrayList(name)).size();
    }

    /**
     * 根据objectBid查询对象modelCode
     *
     * @param objectBid
     * @return
     * @version: 1.0
     * @date: 2022/12/14 15:32
     * @author: jingfang.luo
     */
    public String getModelCodeByObjectBid(String objectBid) {
        CfgObjectPo po = cfgObjectMapper.getByObjectBid(objectBid);
        if (ObjectUtil.isEmpty(po)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.DICT_ERROR_CODES_NOT_NULL.getCode(),
                    "查询不到bid为【" + objectBid + "】的对象");
        }
        return po.getModelCode();
    }

    /**
     * 根据bid查询对象
     *
     * @param bid
     * @return
     * @version: 1.0
     * @date: 2022/12/14 15:32
     * @author: jingfang.luo
     */
    public CfgObjectVo getByBid(String bid) {
        CfgObjectPo po = cfgObjectMapper.getByObjectBid(bid);
        if (ObjectUtil.isEmpty(po)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.DICT_ERROR_CODES_NOT_NULL.getCode(),
                    "查询不到bid为【" + bid + "】的对象");
        }
        return CfgObjectConverter.INSTANCE.po2vo(po);
    }

    /**
     * 根据对象名称查询对象List
     *
     * @param nameList
     * @return
     * @version: 1.0
     * @date: 2022/12/19 17:38
     * @author: jingfang.luo
     */
    public List<CfgObjectPo> findListByNameList(List<String> nameList) {
        return cfgObjectMapper.findListByNameList(nameList);
    }

    /**
     * 根据对象名称模糊查询对象List
     *
     * @param name
     * @return
     * @version: 1.0
     * @date: 2022/12/19 17:38
     * @author: jingfang.luo
     */
    public List<CfgObjectPo> findLikeName(String name) {
        return cfgObjectMapper.findLikeName(name);
    }


    /**
     * 根据bidList查询对象List
     *
     * @param bidList
     * @return
     * @version: 1.0
     * @date: 2022/12/21 11:37
     * @author: jingfang.luo
     */
    public List<CfgObjectPo> findListByObjectBidList(List<String> bidList) {
        return cfgObjectMapper.findModelCodeListByObjectBidList(bidList);
    }

    /**
     * 根据modelCode查所有对象包括其子对象
     * @param modelCode
     * @return
     */
    public List<CfgObjectPo> findLikeModelCode(String modelCode) {
        return cfgObjectMapper.findLikeModelCode(modelCode);
    }

    /**
     * 查询基类对象
     *
     * @param rootObject 基类对象
     * @return List<ObjectModelVO>
     */
    public List<CfgObjectPo> findByBaseModel(String rootObject) {
        return cfgObjectMapper.findByBaseModel(rootObject);
    }

    public String getModelCodeByBaseModel(String baseModel) {
        CfgObjectPo po = cfgObjectMapper.getOneByBaseModel(baseModel);
        if (ObjectUtil.isEmpty(po)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(),
                    "查询不到baseModel为【" + baseModel + "】的对象");
        }
        return po.getModelCode();
    }

    /**
     * 查询对象历史
     *
     * @param modelCode modelCode
     * @param version   version
     * @return
     * @version: 1.0
     * @date: 2023/1/7 14:09
     * @author: bin.yin
     */
    public CfgObjectPo findHistory(String modelCode, Integer version) {
        return objectHistoryMapper.findHistory(modelCode, version);
    }

    /**
     * 根据modelCodeList查询对象List
     *
     * @param modelCodeList
     * @return
     * @version: 1.0
     * @date: 2023/1/9 16:45
     * @author: jingfang.luo
     */
    public List<CfgObjectPo> findListByModelCodeList(List<String> modelCodeList) {
        return cfgObjectMapper.findListByModelCodeList(modelCodeList);
    }
}
