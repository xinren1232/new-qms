package com.transcend.plm.configcenter.object.infrastructure.common.constant;

/**
 * 关系对象常量
 *
 * @author jinpeng.bai
 * @date 2022/12/21 10:10
 **/
public class RelationObjectConstants {

    public static  final String STATE_OFF="off";

    public static final  String KEY_INNERNAME = "1";

    public static final   String SIGN=",";

    public static final int CHILDREN_SIZE =2 ;

    //强浮动
    public static final String STRONG_FLOAT = "STRONG_FLOAT";
    //软浮动
    public static final String SOFT_FLOAT = "SOFT_FLOAT";
    //强固定
    public static final String STRONG_FIXED = "STRONG_FIXED";
    //软固定
    public static final String SOFT_FIXED = "SOFT_FIXED";
    //浮动
    public static final String FLOAT = "FLOAT";
    //固定
    public static final String FIXED = "FIXED";

    //浮动到固定
    public static final String FLOAT2FIXED = "FLOAT2FIXED";
    //固定到浮动
    public static final String FIXED2FLOAT = "FIXED2FLOAT";


    public static final String ZERO = "0";
    public static final String ROOT_PARENT_BID = "0";

    public static final String IPM = "IPM";

    public static class objectRelationAttr{
        public objectRelationAttr(){

        }
        public static final String TARGET="target";
        public static final String RELATION="relation";
    }

    public static final String MODEL_ACTION_GET_METHODE = "get";

    public static class MODEL_FIELD_ATTR{
        public MODEL_FIELD_ATTR(){

        }
        public static final String COMMON_FIELD_BID = "bid";
        public static final String COMMON_FIELD_LC_TEMPLATE_BID = "lc_templ_bid";
        public static final String COMMON_FIELD_LC_TEMPLATE_VERSION = "lc_templ_version";
        public static final String COMMON_FIELD_LIFE_CYCLE_CODE = "life_cycle_code";

    }
}
