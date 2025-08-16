package com.transcend.plm.datadriven.common.constant;

/**
 * 通用配置常量
 *
 * @author jie.luo1
 * @date 2024/07/24
 */
public class CommonConst {

    /**
     * 删除标识-0非删除状态，1删除状态
     */
    public static final Integer DELETE_FLAG_NOT_DELETED = 0;
    public static final Integer DELETE_FLAG_DELETED = 1;


    /**
     * 启用标识-非删除状态 0未启用，1启用，2禁用
     */
    public static final Integer ENABLE_FLAG_DISABLE = 0;
    public static final Integer ENABLE_FLAG_ENABLE = 1;
    public static final Integer ENABLE_FLAG_FORBIDDEN = 2;

    /**
     * app
     */
    public static final String APP_FLAG = "APP:";

    /**
     * spaceAppBid
     */
    public static final String SPACE_APP_BID = "spaceAppBid";

    /**
    *私有标志
     */
    public static final String PRI_KEY = "PRI:";

    /**
     * 角色根节点BID
     */
    public static final String ROLE_TREE_DEFAULT_ROOT_BID = "0";

    /**
     * 角色类型-0空间，2私有角色
     */
    public static final int PRI_ROLE_TYPE = 2;

    /**
     * 角色类型-0空间，2私有角色
     */
    public static final int SPACE_ROLE_TYPE = 0;

    /**
     * 公共使用的,
     */
    public static final String  COMMA = ",";

    /**
     * 树结构默认父BID
     */
    public static final String DEFAULT_PARENT_BID = "0";
    /**
     * 多对象树根节点标识
     */
    public static final int MULTI_TREE_HEAD_FLAG = 1;

    /**
     * LIFE_CYCLE_CODE_STR
     */
    public static final String LIFE_CYCLE_CODE_STR = "lifeCycleCode";

    /**
     * BID_STR
     */
    public static final String BID_STR = "bid";

    /**
     * GROUP_PROPERTY_STR
     */
    public static final String GROUP_PROPERTY_STR = "groupProperty";

    /**
     * CURRENT_HANDLER
     */
    public static final String CURRENT_HANDLER = "currentHandler";
    /**
     * LIFE_CYCLE_NAME_STR
     */
    public static final String LIFE_CYCLE_NAME_STR = "lifeCycleName";
    /**
     * INNER_CREATER_STR
     */
    public static final String INNER_CREATER_STR = "INNER_CREATER";
    /**
     * PRODUCT_AREA_STR
     */
    public static final String PRODUCT_AREA_STR = "productArea";
    /**
     * DOMAIN_BID_STR
     */
    public static final String DOMAIN_BID_STR = "domainBid";
    /**
     * PRODUCT_MANAGER_STR
     */
    public static final String PRODUCT_MANAGER_STR = "productManager";

    /**
     * followers
     */
    public static final String FOLLOWERS = "followers";

    /**
     * DOMAIN_DEVELOPMEN_REPRESENTATI
     */
    public static final String DOMAIN_DEVELOPMEN_REPRESENTATI = "domainDevelopmenRepresentati";

    /**
     * SOFTWARE_TESTING_REPRESE
     */
    public static final String SOFTWARE_TESTING_REPRESE = "softwareTestingReprese";
    /**
     * ALL
     */
    public static final String ALL = "all";
    /**
     * ANY
     */
    public static final String ANY = "any";
    /**
     * STAY_DURATION
     */
    public static final String STAY_DURATION = "stayDuration";

    public  static  final String SWITH_ON="on";
}
