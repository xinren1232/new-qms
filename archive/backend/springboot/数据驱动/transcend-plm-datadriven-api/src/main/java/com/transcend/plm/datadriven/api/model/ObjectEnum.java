package com.transcend.plm.datadriven.api.model;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象模型配置常量
 *
 * @author jie.luo1
 */
public enum ObjectEnum implements BaseEnum<String> {


    /**
     * 对象模型编码
     */
    MODEL_CODE("modelCode", "对象模型编码"),

    /**
     * 生命周期编码
     */
    LIFE_CYCLE_CODE("lifeCycleCode", "生命周期编码"),

    /**
     * 责任人
     */
    PERSON_RESPONSIBLE("personResponsible", "责任人"),

    /**
     * 处理人
     */
    HANDLER("handler", "处理人"),

    /**
     * 处理人
     */
    UX_SCORE("uxScore", "UX代表"),



    /**
     * 关注人
     */
    FOLLOW_MEMBER("followMember", "关注人"),

    /**
     * 是否加密需求
     */
    IS_CONL_REQUIRE("isConlRequire", "是否加密需求"),

    /**
     * 加密人
     */
    CONFIDENTIAL_MEMBER("confidentialMember", "加密人"),
    /**
     * 生命周期编码
     */
    LC_TEMPL_BID("lcTemplBid", "生命周期编码"),
    /**
     * 生命周期编码版本
     */
    LC_TEMPL_VERSION("lcTemplVersion", "生命周期编码版本"),
    /**
     * 生命周期编码+对象模型编码组合 code
     */
    LC_MODEL_CODE("lcModelCode", "生命周期编码+对象模型编码组合 code"),
    /**
     * 权限BID
     */
    NAME("name", "名称"),
    /**
     * 业务编码
     */
    CODING("coding", "业务编码"),
    /**
     * 权限BID
     */
    PERMISSION_BID("permissionBid", "权限BID"),
    /**
     * 外部唯一标识
     */
    FOREIGN_BID("foreignBid", "外部唯一标识"),
    /**
     * 到达时间
     */
    REACH_TIME("reachTime", "到达时间"),
    /**
     * 扩展内容
     */
    EXT("ext", "扩展内容"),
    /**
     * 数据所有者
     */
    OWNER("owner", "数据所有者"),
    /**
     * 工作项类型
     */
    WORK_ITEM_TYPE("workItemType", "工作项类型"),
    /**
     * 空间BID
     */
    SPACE_BID("spaceBid", "空间BID"),
    /**
     * 应用BID
     */
    SPACE_APP_BID("spaceAppBid", "应用BID"),
    /**
     * 挂载空间bid
     */
    MOUNT_SPACE_BID("mountSpaceBid", "挂载空间bid"),
    ;

    /**
     * code
     */
    String code;

    /**
     * desc
     */
    String desc;

    ObjectEnum(String code, String desc) {
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
