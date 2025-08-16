package com.transcend.plm.datadriven.core.model;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * 增删改查接口
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/18 17:11
 * @since 1.0
 */
public interface SimpleModelStandardI<M> extends StandardI<MBaseData> {

    /**
     * 新增方法
     *
     * @param m 数据类型
     * @return MObject
     */
    M add(M m);


    /**
     * 逻辑删除方法
     *
     * @param bid 实例bid
     * @return Boolean
     */
    Boolean logicalDelete(String bid);

    /**
     * 更新方法
     *
     * @param mObject  模型实例
     * @param wrappers 查询条件
     * @return Boolean
     */
    Boolean update(M mObject, List<QueryWrapper> wrappers);

    /**
     * 新增方法
     *
     * @param wrappers 查询条件
     * @return Boolean
     */
    List<M> list(List<QueryWrapper> wrappers);

    /**
     * 保存或者更新
     *
     * @param m 实例
     * @return MObject
     */
    M saveOrUpdate(M m);

    /**
     * 根据bid查询
     *
     * @param bid 业务bid
     * @return MObject
     */
    M getByBid(String bid);

    /**
     * 分页查询
     *
     * @param pageQo 分页查询条件
     * @return Boolean
     */
    PagedResult<M> page(BaseRequest<QueryCondition> pageQo);

    /**
     * 批量新增
     *
     * @param mObjects 实例数据集
     * @return Boolean
     */
    Boolean addBatch(List<MObject> mObjects);

    /**
     * 逻辑删除
     *
     * @param bid bid
     * @return Boolean
     */
    Boolean logicalDeleteByBid(String bid);

    /**
     * 删除
     *
     * @param bid bid
     * @return Boolean
     */
    Boolean deleteByBid(String bid);

    /**
     * updateByBid
     *
     * @param bid     bid
     * @param mObject mObject
     * @return {@link Boolean}
     */
    Boolean updateByBid(String bid, MObject mObject);
}
