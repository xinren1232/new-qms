package com.transcend.plm.datadriven.common.dao;

import java.io.Serializable;

/**
 * DAO公共基类，由MybatisGenerator自动生成请勿修改
 *
 * @param <Model> The Model Class 这里是泛型不是Model类
 * @param <PK>    The Primary Key Class 如果是无主键，则可以用Model来跳过，如果是多主键则是Key类
 * @author unknown
 */
public interface MyBatisBaseDao<Model, PK extends Serializable> {
    /**
     * deleteByPrimaryKey
     *
     * @param id id
     * @return {@link int}
     */
    int deleteByPrimaryKey(PK id);

    /**
     * insert
     *
     * @param record record
     * @return {@link int}
     */
    int insert(Model record);

    /**
     * insertSelective
     *
     * @param record record
     * @return {@link int}
     */
    int insertSelective(Model record);

    /**
     * selectByPrimaryKey
     *
     * @param id id
     * @return {@link Model}
     */
    Model selectByPrimaryKey(PK id);

    /**
     * updateByPrimaryKeySelective
     *
     * @param record record
     * @return {@link int}
     */
    int updateByPrimaryKeySelective(Model record);

    /**
     * updateByPrimaryKey
     *
     * @param record record
     * @return {@link int}
     */
    int updateByPrimaryKey(Model record);
}
