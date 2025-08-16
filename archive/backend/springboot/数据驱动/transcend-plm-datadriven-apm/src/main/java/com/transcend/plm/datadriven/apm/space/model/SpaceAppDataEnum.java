package com.transcend.plm.datadriven.apm.space.model;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象模型配置常量
 *
 * @author jie.luo1
 */
public enum SpaceAppDataEnum implements BaseEnum<String> {


    /**
     *工作项类型
     */
    WORK_ITEM_TYPE("workItemType","工作项类型"),
    /**
     *空间BID
     */
    SPACE_BID("spaceBid", "空间BID"),
    /**
     *空间BID
     */
    BELONG_SPACE_BID("belongSpaceBid", "空间BID"),
    /**
     *空间应用BID
     */
    SPACE_APP_BID("spaceAppBid", "空间应用BID"),
    /**
     *空间应用BID
     */
    SPACE_APP_BID_TABLE("space_app_bid", "空间应用BID"),
    /**
     *空间BID
     */
    SPACEBID_TABLE("space_bid", "空间BID"),
    /**
     *空间BID
     */
    BELONG_SPACEBID_TABLE("belong_space_bid", "空间BID"),
    /**
     *角色成员列表
     */
    ROLE_USER("roleUser","角色成员列表"),
    /**
     *挂载空间业务ID
     */
    MOUNT_SPACE_BID("mountSpaceBid","挂载空间业务ID"),
    /**
     *是否挂载数据
     */
    IS_MOUNT("isMount", "是否挂载数据")
    ;



    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    SpaceAppDataEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
