package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.page.PageMethod;
import com.transcend.plm.configcenter.object.infrastructure.po.ObjectRelationPO;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectRelationModelAttrMapper;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ObjectRelationModelMapper;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectRelationAttrDTO;
import com.transcend.plm.configcenter.api.model.object.qo.ObjectRelationQO;
import com.transsion.framework.common.CollectionUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transsion.framework.dto.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 关系仓储
 *
 * @author jinpeng.bai
 * @date 2022/12/20 15:31
 **/
@Repository
@Slf4j
public class RelationObjectConfigRepository {

    @Resource
    private ObjectRelationModelMapper objectRelationModelMapper;

    @Resource
    private ObjectRelationModelAttrMapper objectRelationModelAttrMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 关系对象新增
     *
     * @param po
     * @return
     * @version: 1.0
     * @date: 2022/11/28 10:19
     * @author: jingfang.luo
     */
    public Boolean bachAdd(List<ObjectRelationPO> po) {
        return transactionTemplate.execute(transactionStatus -> {
            //新增新表
            Boolean insert = objectRelationModelMapper.insert(po) > 0;
            log.info("执行对象新增操作，新增新表结果：{}", insert);
            return insert;
        });
    }


    /**
     * 关系对象修改
     *
     * @param po
     * @return
     * @version: 1.0
     * @date: 2022/11/28 10:19
     * @author: jingfang.luo
     */
    public Integer edit(List<ObjectRelationPO> po) {
        return transactionTemplate.execute(transactionStatus -> {
            return objectRelationModelMapper.edit(po);
        });
    }

    /**
     * 关系对象查询
     *
     * @param qo
     * @return
     * @version: 1.0
     * @date: 2022/11/28 10:19
     * @author: jingfang.luo
     */
    public List<ObjectRelationPO> find(ObjectRelationQO qo) {
        return objectRelationModelMapper.find(qo);
    }

    /**
     * 关系对象删除
     *
     * @param relationList
     * @return
     * @version: 1.0
     * @date: 2022/11/28 10:19
     * @author: jingfang.luo
     */
    public Integer delete(List<ObjectRelationPO> relationList) {
        return objectRelationModelMapper.delete(relationList);
    }


    /**
     * 关系对象查询
     *
     * @param qo
     * @return
     * @version: 1.0
     * @date: 2022/11/28 10:19
     * @author: jingfang.luo
     */
    public IPage<ObjectRelationPO> queryForPage(BaseRequest<ObjectRelationQO> qo) {
        com.github.pagehelper.Page<ObjectRelationPO> page = PageMethod.startPage(qo.getCurrent(), qo.getSize());
        return objectRelationModelMapper.query(page,qo.getParam());
    }

    /**
     * 关系对象查询
     *
     * @param qo
     * @return
     * @version: 1.0
     * @date: 2022/11/28 10:19
     * @author: jingfang.luo
     */
    public List<ObjectRelationPO> query(ObjectRelationQO qo) {
        return objectRelationModelMapper.query(qo);
    }

    /**
     * 根据源对象id列表查询关系->对象继承
     * @param sourceModelCodes
     * @return
     */
    public List<ObjectRelationPO> findBySourceModelCodes(List<String> sourceModelCodes){
        return objectRelationModelMapper.findBySourceModelCodes(sourceModelCodes);
    }


    /**
     * 查询关系对象
     * @param relationList
     * @return
     */
    public List<ObjectRelationPO> findByrelationFlagect(List<ObjectRelationPO> relationList){
        return objectRelationModelMapper.findByrelationFlagect(relationList);
    }

    public void batchInsertAttr(List<ObjectRelationAttrDTO> dtoList){
        if(CollectionUtil.isEmpty(dtoList)){
            return;
        }
        dtoList.forEach(e ->{
            e.setCreatedBy(SsoHelper.getJobNumber());
            e.setUpdatedBy(SsoHelper.getJobNumber());
            e.setCreatedTime(LocalDateTime.now());
            e.setUpdatedTime(LocalDateTime.now());
            e.setTenantId(100L);
            e.setIsDelete(false);
            if(e.getColumnWidth() !=null && e.getColumnWidth() <= 1){
                e.setColumnWidth(null);
            }
        });
        objectRelationModelAttrMapper.saveBatch(dtoList);
    }


    /**
     * 查询列表
     * @param objectRelationAttrDTO
     * @return
     */
    public List<ObjectRelationAttrDTO> queryAttrList(ObjectRelationAttrDTO objectRelationAttrDTO){
        return objectRelationModelAttrMapper.queryList(objectRelationAttrDTO);
    }

    /**
     * 移除属性配置列表
     */
    public void removerAttr(List<String> bids){
        objectRelationModelAttrMapper.remove(bids);
    }

}
