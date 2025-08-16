package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ALM数据投递队列服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/7 09:15
 */
public interface AlmDataDeliverQueueService {

    /**
     * 交换机
     */
    String EXCHANGE = "/model/data";

    /**
     * 默认投递字段，不管是否配置都会投递
     */
    String[] DEFAULT_FIELDS = new String[]{"modelCode", "bid", "dataBid", "deleteFlag",
            "createdBy", "updatedBy", "createdTime", "updatedTime","spaceBid","spaceAppBid"};

    /**
     * 配置
     */
    @Data
    @Accessors(chain = true)
    class Config {

        /**
         * 模型编码
         */
        private String modelCode;

        /**
         * 需要同步的字段
         */
        private String[] fields;

        /**
         * 条件字段
         * 满足匹配条件的第一个配置则会适用改配置
         * 可为空,为空或者未匹配到时则直接使用fields字段
         */
        private List<ConditionField> conditionFields;
    }

    @Data
    @Accessors(chain = true)
    class ConditionField {

        /**
         * 是否匹配任意条件
         */
        private boolean anyMatch;

        /**
         * 控制表达式
         */
        private List<Expression> expressions;

        /**
         * 同步的字段
         */
        private String[] fields;

    }

    /**
     * 表达式
     */
    @Data
    @AllArgsConstructor
    class Expression {
        /**
         * 字段名称
         */
        private String fieldName;
        /**
         * 条件
         */
        private String condition;
        /**
         * 值
         */
        private String value;

    }


    /**
     * 数据投递
     *
     * @param data 需要投递的数据
     * @return 是否投递成功
     */
    boolean deliver(MBaseData data);


    /**
     * 投递多个同步数据
     *
     * @param dataList 需要投递的数据
     * @return 投递数量
     */
    long deliver(List<MBaseData> dataList);

    /**
     * 依据条件投递数据
     *
     * @param modelCode 模型模式
     * @param wrappers  查询条件
     * @return 投递数量
     */
    long deliverByCondition(String modelCode, List<QueryWrapper> wrappers);

    /**
     * 补偿投递
     *
     * @param modelCode 模型编码
     * @param params    查询参数
     */
    void compensateDeliver(String modelCode, ModelMixQo params);


}
