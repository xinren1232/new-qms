package com.transcend.plm.datadriven.common.constant;

/**
 * TranscendModel基础字段
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/9/9 10:42
 */
public interface TranscendModelBaseFields {

    /**
     * 生命周期编码
     * 类型：文本
     */
    String LIFE_CYCLE_CODE = "lifeCycleCode";

    /**
     * 生命周期模型编码
     * 类型：文本
     */
    String LC_MODEL_CODE = "lcModelCode";

    /**
     * 绑定的生命周期模板 bid
     * 类型：文本
     */
    String LC_TEMPL_BID = "lcTemplBid";

    /**
     * 绑定的生命周期模板对应版本
     * 类型：文本
     */
    String LC_TEMPL_VERSION = "lcTemplVersion";

    /**
     * 名称
     * 类型：文本
     */
    String NAME = "name";


    /**
     * CODING
     * 类型：文本
     */
    String CODING = "coding";

    /**
     * 单表唯一标识
     * 类型：整型
     */
    String ID = "id";

    /**
     * 全局唯一标识
     * 类型：文本
     */
    String BID = "bid";

    /**
     * 数据标识
     * 类型：文本
     */
    String DATA_BID = "dataBid";

    /**
     * 模型编码
     * 类型：文本
     */
    String MODEL_CODE = "modelCode";

    /**
     * 扩展内容
     * 类型：JSON
     */
    String EXT = "ext";

    /**
     * 数据所有者
     * 类型：文本
     */
    String OWNER = "owner";

    /**
     * 启用标识
     * 类型：文本
     */
    String ENABLE_FLAG = "enableFlag";

    /**
     * 删除标识
     * 类型：文本
     */
    String DELETE_FLAG = "deleteFlag";

    /**
     * 租户 ID
     * 类型：文本
     */
    String TENANT_ID = "tenantId";

    /**
     * 创建人
     * 类型：文本
     */
    String CREATED_BY = "createdBy";

    /**
     * 更新人
     * 类型：文本
     */
    String UPDATED_BY = "updatedBy";

    /**
     * 创建时间
     * 类型：日期
     */
    String CREATED_TIME = "createdTime";

    /**
     * 更新时间
     * 类型：日期
     */
    String UPDATED_TIME = "updatedTime";

    /**
     * 工作项类型
     * 类型：文本
     */
    String WORK_ITEM_TYPE = "workItemType";

    /**
     * 空间 BID
     * 类型：文本
     */
    String SPACE_BID = "spaceBid";

    /**
     * 应用 BID
     * 类型：文本
     */
    String SPACE_APP_BID = "spaceAppBid";

    /**
     * 父节点 BID
     * 类型：文本
     */
    String PARENT_BID = "parentBid";

    /**
     * 排序
     * 类型：整型
     */
    String SORT = "sort";

    /**
     * 多对象树是否是头节点
     * 类型：整型
     */
    String MULTI_TREE_HEAD = "multiTreeHead";

    /**
     * 挂载空间 bid
     * 类型：文本
     */
    String MOUNT_SPACE_BID = "mountSpaceBid";

    /**
     * 权限 bid
     * 类型：文本
     */
    String PERMISSION_BID = "permissionBid";

    /**
     * 责任人
     * 类型：JSON
     */
    String PERSON_RESPONSIBLE = "personResponsible";

}
