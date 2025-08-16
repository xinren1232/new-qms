package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectAttributePo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectModelAttrHistoryMapper;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectModelAttrMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ObjectAttrRepository
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/05 18:22
 */
@Repository
public class CfgObjectAttributeRepository {

    @Resource
    private ObjectModelAttrMapper objectModelAttrMapper;

    @Resource
    private ObjectModelAttrHistoryMapper objectModelAttrHistoryMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 根据modelCodeList查询属性
     *
     * @param codeList
     * @return
     * @version: 1.0
     * @date: 2022/12/7 11:50
     * @author: jingfang.luo
     */
    public List<CfgObjectAttributePo> findInModelCodeList(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Lists.newArrayList();
        }
        return objectModelAttrMapper.findInModelCodeList(codeList);
    }

    /**
     * 根据modelVersionCode列表查询属性
     * @param modelVersionCodeList modelVersionCodeList
     * @return
     * @version: 1.0
     * @date: 2023/1/7 16:18
     * @author: bin.yin
     */
    public List<CfgObjectAttributePo> findHistoryByModelVersionCodeList(List<String> modelVersionCodeList) {
        return objectModelAttrHistoryMapper.findByModelVersionCodeList(modelVersionCodeList);
    }

    /**
     * 根据modelCode删除属性
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/7 11:59
     * @author: jingfang.luo
     */
    public Boolean delete(String modelCode) {
        return objectModelAttrMapper.delete(modelCode) > 0;
    }

    public Boolean deleteExcludeCodes(String modelCode,List<String> codeList) {
        if(CollectionUtils.isNotEmpty(codeList)){
            return objectModelAttrMapper.deleteExcludeCodes(modelCode, codeList) > 0;
        }else{
            return objectModelAttrMapper.delete(modelCode) > 0;
        }
    }

    /**
     * 根据modelCodeList删除属性
     *
     * @param modelCodeList
     * @return
     * @version: 1.0
     * @date: 2022/12/13 14:55
     * @author: jingfang.luo
     */
    public Boolean deleteByList(List<String> modelCodeList) {
        return objectModelAttrMapper.deleteByList(modelCodeList) > 0;
    }

    /**
     * 批量新增
     *
     * @param poList
     * @return
     * @version: 1.0
     * @date: 2022/12/7 17:05
     * @author: jingfang.luo
     */
    public Boolean bulkAdd(List<CfgObjectAttributePo> poList) {
        return transactionTemplate.execute(transactionStatus -> {
            //新增新表
            int insert = objectModelAttrMapper.bulkInsert(poList);
            //新增历史表
            int historyInsert = objectModelAttrHistoryMapper.bulkInsert(poList);
            return insert > 0 && historyInsert > 0;
        });
    }

    /**
     * 根据对象的modelCode和属性bid查询单条属性
     *
     * @param modelCode
     * @param bid
     * @return
     * @version: 1.0
     * @date: 2022/12/14 15:44
     * @author: jingfang.luo
     */
    public CfgObjectAttributePo findAttrByModelCodeAndBid(String modelCode, String bid) {
        return objectModelAttrMapper.findAttrByModelCodeAndBid(modelCode, bid);
    }

    /**
     * 根据modelCode Like出所有属性
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2023/1/4 16:07
     * @author: jingfang.luo
     */
    public List<CfgObjectAttributePo> findLikeModelCode(String modelCode) {
        return objectModelAttrMapper.findLikeModelCode(modelCode);
    }

    /**
     * 批量更新发布状态
     */
    public Boolean batchUpdatePublishStatus(List<String> bidList,boolean published) {
        return objectModelAttrMapper.batchUpdatePublishStatus(bidList,published) > 0;
    }

    public List<CfgObjectAttributePo> listSupperAttr(Set<String> objectTypes) {
        if(CollectionUtils.isEmpty(objectTypes)){
            return new ArrayList<>();
        }
        return objectModelAttrMapper.listSupperAttr(objectTypes);
    }
}
