package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 需求状态自动更新服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/26 16:05
 */
public interface RequirementsStatusAutoUpdateService {

    /**
     * 非关注的模型编码
     *
     * @param modelCode 模型编码
     * @return 非关注编码 true，关注编码 false
     */
    boolean notFocusModelCode(String modelCode);

    /**
     * 非关注的关系模型编码
     *
     * @param modelCode 模型编码
     * @return 非关注关系编码 true，关注编码 false
     */
    boolean notFocusRelModelCode(String modelCode);

    /**
     * 获取父层配置信息
     *
     * @param obj 对象
     * @return 配置信息
     */
    List<Config> getSelfConfig(TranscendObjectWrapper obj);

    /**
     * 自动更新状态
     *
     * @param configList 配置信息
     * @param wrappers   查询条件
     */
    @Transactional(rollbackFor = Exception.class)
    void autoUpdateSelf(List<Config> configList, List<QueryWrapper> wrappers);

    /**
     * 获取父层配置
     *
     * @param obj 子对象
     * @return 配置信息
     */
    List<Config> getParentUpdateConfig(TranscendObjectWrapper obj);

    /**
     * 自动更新父层状态
     *
     * @param configList 配置信息
     * @param wrappers   查询条件
     */
    @Transactional(rollbackFor = Exception.class)
    void autoUpdateParent(List<Config> configList, List<QueryWrapper> wrappers);

    /**
     * 自动更新关系源数据的状态
     *
     * @param relationData 关系数据
     */
    @Transactional(rollbackFor = Exception.class)
    void autoUpdateRelationSource(TranscendRelationWrapper relationData);

    /**
     * 自动更新关系源数据的状态
     *
     * @param modelCode 关系模型编码
     * @param wrappers  关系查询条件
     */
    @Transactional(rollbackFor = Exception.class)
    void autoUpdateRelationSource(String modelCode, List<QueryWrapper> wrappers);


    /**
     * 写入修改前状态
     * 将修改前的状态写入对象中
     *
     * @param modelCode 模型编码
     * @param obj       对象
     * @param wrappers  查询条件
     */
    void writePreModifyStatus(String modelCode, TranscendObjectWrapper obj, List<QueryWrapper> wrappers);


    @Data
    @Accessors(chain = true)
    class Config {
        /**
         * 父层模型
         */
        String parentModelCode;

        /**
         * 父层当前状态
         * 父层状态到此装备表示可以进行升级逻辑运算
         */
        List<String> parentStatusCode;
        /**
         * 父层新状态
         */
        String parentNewStatusCode;

        /**
         * 子层模型
         */
        String childModelCode;
        /**
         * 子层状态
         * 子层到此状态时，表示可以进行匹配机组但
         */
        String childStatusCode;
        /**
         * 关系对象编码
         * 应为是通过中间表关联，故需要通过关联表进行查询
         */
        String relationCode;
        /**
         * 是否反转关系
         */
        boolean reverseRelation;
        /**
         * 任意匹配
         * 是否任意一条状态到达，父状态跟随变化
         */
        boolean anyMatch;

        /**
         * 是否通过流程来翻转
         */
        boolean isFlow;

        /**
         * 流程节点Bid
         */
        String flowNodeWebBid;

    }

}
