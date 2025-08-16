package com.transcend.plm.configcenter.object.domain.entity;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.common.tools.ObjectTypeTool;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.common.web.exception.ExceptionConstructor;
import com.transcend.plm.configcenter.object.domain.aggergate.CfgObjectLockRoot;
import com.transcend.plm.configcenter.object.domain.aggergate.CfgObjectAttrRoot;
import com.transcend.plm.configcenter.object.infrastructure.common.constant.ObjectModelConstants;
import com.transcend.plm.configcenter.object.infrastructure.extension.ExtensionService;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectAttributePo;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectLifeCycleRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectAttributeRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectRepository;
import com.transcend.plm.configcenter.object.pojo.CfgObjectAttributeConverter;
import com.transcend.plm.configcenter.object.pojo.CfgObjectConverter;
import com.transcend.plm.configcenter.api.model.object.dto.*;
import com.transcend.plm.configcenter.api.model.object.vo.*;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.ObjectUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ObjectModel
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/25 15:42
 */
@Slf4j
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CfgObject extends BasePoEntity {

    @Resource
    private CfgObjectRepository cfgObjectRepository = PlmContextHolder.getBean(CfgObjectRepository.class);

    @Resource
    private CfgObjectAttributeRepository cfgObjectAttributeRepository = PlmContextHolder.getBean(CfgObjectAttributeRepository.class);

    @Resource
    private CfgObjectLifeCycleRepository cfgObjectLifeCycleRepository = PlmContextHolder.getBean(CfgObjectLifeCycleRepository.class);

    @Resource
    private TransactionTemplate transactionTemplate = PlmContextHolder.getBean(TransactionTemplate.class);

    @ApiModelProperty(value = "模型code(优化使用)")
    private String modelCode;

    @ApiModelProperty(value = "版本")
    private Integer version;

    @ApiModelProperty(value = "modelCode+version的唯一id")
    private String modelVersionCode;

    @ApiModelProperty(value = "父的modelCode+version的唯一id")
    private String parentModelVersionCode;

    @ApiModelProperty(value = "父对象版本")
    private Integer parentVersion;

    @ApiModelProperty(value = "业务编码（对应SAP的编码）")
    private String code;

    @ApiModelProperty(value = "对象名称")
    private String name;

    @ApiModelProperty(value = "表bid")
    private String tableBid;

    @ApiModelProperty(value = "图标(存放路径)")
    private String avatar;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "根模型")
    private String baseModel;

    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @ApiModelProperty(value = "是否有草稿")
    private Boolean draft;

    @ApiModelProperty(value = "是否关系对象")
    private Byte relationFlag;

    @ApiModelProperty(value = "是否有版本")
    private Byte versionFlag;

    @ApiModelProperty(value = "是否是多对象")
    private Byte isMultiObjectModel;

    @ApiModelProperty(value = "是否树形结构")
    private Boolean treeFlag;

    @ApiModelProperty(value = "锁信息：提示哪个对象被谁锁了")
    private String lockInfo;

    @ApiModelProperty(value = "检出人")
    private String checkoutBy;

    @ApiModelProperty(value = "属性")
    private List<CfgObjectAttributeVo> attrList;

    @ApiModelProperty(value = "生命周期")
    private ObjectModelLifeCycleVO lifeCycle;

    @ApiModelProperty(value = "权限")
    private List<CfgObjectPermissionVo> authList;

    @ApiModelProperty(value = "视图")
    private List<ObjectViewVO> viewList;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "多语言字典")
    private Map<String, String> langDict;

    /**
     * 构建add ObjectModel
     */
    public static CfgObject buildWithAddOrUpdate(ObjectAddParam objectAddParam) {

        CfgObject cfgObject = CfgObjectConverter.INSTANCE.addParem2entity(objectAddParam);
        cfgObject.setVersion(ObjectModelConstants.DEFAULT_OBJECT_VERSION)
                .setAvatar(ObjectModelConstants.DEFAULT_OBJECT_PNG)
                .setDraft(Boolean.FALSE)
                .setRelationFlag((byte)0)
                .setVersionFlag((byte)0);
        cfgObject.setCreatedBy(SsoHelper.getJobNumber());
        cfgObject.setUpdatedBy(SsoHelper.getJobNumber());
        cfgObject.setEnableFlag(CommonConst.ENABLE_FLAG_DISABLE);

        String bid = objectAddParam.getBid();
        if (StringUtils.isNotEmpty(bid)) {
            cfgObject.setBid(bid);
        } else {
            cfgObject.setBid(SnowflakeIdWorker.nextIdStr());
        }

        return cfgObject;
    }

    /**
     * 根据父的modelCode生成modelCode && 填充modelCode && bid && 父信息 && 排序 等相关信息
     */
    public CfgObject populateInfoByParentCode(String parentCode) {
        String genModelCode;
        String maxModelCode;
        // 父的modelCode为空 || 为0
        if (StringUtils.isEmpty(parentCode) || ObjectUtil.equals(parentCode, ObjectModelConstants.OBJECT_ROOT_BID)) {
            parentCode = ObjectModelConstants.OBJECT_ROOT_BID;
            //是根节点，查出子下面的最大的modelCode
            maxModelCode = cfgObjectRepository.findMaxModelCodeInRoot();
        } else {
            //不是根节点，查出子下面的最大的modelCode
            maxModelCode = cfgObjectRepository.findMaxModelCodeByParentCode(parentCode);
        }

        if (StringUtils.isEmpty(maxModelCode)) {
            //没有子就以ObjectCodeUtils.getInitCode()开头
            ObjectCodeUtils.checkLevelLimit(parentCode);
            if (StringUtils.isEmpty(parentCode) || CommonConst.ROLE_TREE_DEFAULT_ROOT_BID.equals(parentCode)){
                genModelCode = ObjectCodeUtils.getInitCode();
            } else {
                genModelCode = parentCode + ObjectCodeUtils.getInitCode();
            }
        } else {
            //有就生成
            genModelCode = ObjectCodeUtils.increaseCode(maxModelCode);
        }
        //获取父版本 - 根节点的父版本为1 不是根节点的父版本需查库
        Integer parentVer;
        if (ObjectUtil.equals(ObjectModelConstants.OBJECT_ROOT_BID, parentCode)) {
            parentVer = ObjectModelConstants.DEFAULT_OBJECT_VERSION;
        } else {
            parentVer = cfgObjectRepository.findVersionByModelCode(parentCode);
        }
        String parentMvc = parentCode + ":" + parentVer;
        //设置modelCode信息
        this.setModelCode(genModelCode).setModelVersionCode(genModelCode + ":" + ObjectModelConstants.DEFAULT_OBJECT_VERSION)
                //设置父信息
                .setParentModelVersionCode(parentMvc).setParentVersion(parentVer);
        //排序为空设为子的末尾
        if (ObjectUtil.isEmpty(sort)) {
            this.setSort(cfgObjectRepository.findSortByParentMvc(parentMvc) + 1);
        }
        return this;
    }

    /**
     * 新增基类 - 填充baseModel
     */
    public CfgObject populateBaseModel() {
        //基类的modelCode只有ObjectCodeUtils.MODEL_CODE_SIZE_THREE位
        if (ObjectCodeUtils.isBaseModel(modelCode)) {
            if (StringUtils.isNotEmpty(baseModel)) {
                //baseModel不为空需要校验是否有重复
                int count = cfgObjectRepository.findByBaseModel(baseModel).size();
                if (count > 0) {
                    throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(),
                            "对象基类中已存在相同的BaseModel【" + baseModel + "】");
                }
            } else {
                //baseModel为空则把modelCode设置进去
                this.setBaseModel(modelCode);
            }
        }
        return this;
    }

    /**
     * 对象校验 - 校验对象名称不能相同
     */
    public CfgObject checkObjectNameCannotBeSame() {
        Integer count = cfgObjectRepository.findObjectCountByName(name);
        if (count > 0) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(),
                    "对象中已存在相同名称【" + name + "】的对象");
        }
        return this;
    }

    /**
     * 对象模型行为 - 新增
     */
    public CfgObjectVo add() {
        CfgObjectPo po = CfgObjectConverter.INSTANCE.entity2po(this);
        cfgObjectRepository.add(po);
        return BeanUtil.copy(this, CfgObjectVo.class);
    }

    /**
     * 对象模型行为 - 新增
     */
    public CfgObjectVo update() {
        CfgObjectPo po = CfgObjectConverter.INSTANCE.entity2po(this);
        cfgObjectRepository.batchUpdateAndHistory(Lists.newArrayList(po));
        return BeanUtil.copy(this, CfgObjectVo.class);
    }

    /**
     * 对象模型行为 - 新增对象和属性(BOM使用，暂未考虑通用)
     */
    public CfgObjectVo addObjectAndAttr() {
        cfgObjectRepository.addObjectAndAttr(this);
        return BeanUtil.copy(this, CfgObjectVo.class);
    }

    /**
     * 构建getOne ObjectModel
     */
    public static CfgObject buildWithModelCode(String modelCode) {
        CfgObject cfgObject = new CfgObject();
        return cfgObject.setModelCode(modelCode);
    }

    /**
     * 构建getOne ObjectModel
     */
    public static CfgObject buildWithModelCodeToBase(String modelCode) {
        CfgObject cfgObject = new CfgObject();
        String baseModelCode = ObjectCodeUtils.getBaseModelCode(modelCode);
        return cfgObject.setModelCode(baseModelCode);
    }

    /**
     * 对象模型行为 - getOne -填充基本信息
     */
    public CfgObject populateBaseInfo() {
        return CfgObjectConverter.INSTANCE.vo2entity(cfgObjectRepository.getByBid(super.getBid()));
    }

    /**
     * 对象模型行为 - getOne -填充锁信息
     */
    public CfgObject populateLockInfo() {
        ObjectLockVO objectLockVO = CfgObjectLock.buildWithLock(getBid(), getModelCode(), getName()).listInheritLockByModelCode();
        if(objectLockVO != null){
            this.setLockInfo(objectLockVO.getLockInfo()).setCheckoutBy(objectLockVO.getCheckoutBy());
        }
        return this;
    }

    /**
     * 对象模型行为 - getOne -填充属性
     */
    public CfgObject populateAttributes() {
        //根据modelCode查询出对象属性并填充
        List<CfgObjectAttributeVo> list = CfgObjectAttrRoot.build().findByModelCode(modelCode);
        this.setAttrList(list);
        return this;
    }

    /**
     * 对象模型行为 - getOne -填充生命周期
     */
    public CfgObject populateLifeCycle() {
        //根据modelCode查询出生命周期并填充属性
        ObjectModelLifeCycleVO objectModelLifeCycleVO = CfgObjectLifecycle.buildWithFind(modelCode).findAndPopulate();
        this.setLifeCycle(objectModelLifeCycleVO);
        return this;
    }

    /**
     * 对象模型行为 - getOne -填充权限
     */
    public CfgObject populateAuth() {
        //根据InModelCodeList查询出所有权限并填充权限
        List<CfgObjectPermissionVo> authObjRoleVOList = PlmContextHolder.getBean(ExtensionService.class).findAuthInModelCode(modelCode);
        this.setAuthList(authObjRoleVOList);
        return this;
    }

    /**
     * 对象模型行为 - getOne -填充视图
     */
    public CfgObject populateView() {
        //根据modelCode查询视图
        List<ObjectViewVO> viewConfigVOList = PlmContextHolder.getBean(ExtensionService.class).findViewByModelCode(modelCode);
        this.setViewList(viewConfigVOList);
        return this;
    }

    /**
     * 构建vo返回
     */
    public CfgObjectVo build() {
        return BeanUtil.copy(this, CfgObjectVo.class);
    }

    /**
     * 构建checkout ObjectModel
     */
    public static CfgObject buildWithCheckout(String bid) {
        return buildWithBid(bid).populateBaseInfo();
    }

    /**
     * 对象模型行为 - checkout
     */
    public Boolean checkout() {
        //校验对象链中是否有人检出
        String lock = CfgObjectLockRoot.build().listAllChainLockInfoByModelCode(getModelCode());
        if (StringUtils.isNotEmpty(lock)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), lock);
        }
        //上锁
        return CfgObjectLock.buildWithLock(getBid(), getModelCode(), getName()).lock();
    }

    /**
     * 构建暂存 ObjectModel
     */
    public static CfgObject buildWithStaging(StagingParam stagingParam) {
        return BeanUtil.copy(stagingParam, CfgObject.class);
    }

    /**
     * 对象模型行为 - 暂存
     */
    public Boolean staging() {
        return transactionTemplate.execute(transactionStatus -> {
            //更新草稿状态
            Boolean updateObject = cfgObjectRepository.batchUpdateAndHistory(
                    Lists.newArrayList(CfgObjectPo.of().setModelCode(modelCode).setDraft(Boolean.TRUE))
            );
            //草稿服务的操作
            this.setDraft(Boolean.TRUE);
            Boolean staging = PlmContextHolder.getBean(ExtensionService.class).staging(this);
            log.info("执行暂存操作，草稿暂存结果：{}；更新对象结果：{}", staging, updateObject);
            return true;
        });
    }

    /**
     * 构建读取草稿 ObjectModel
     */
    public static CfgObject buildWithReadDraft(String bid) {
        return CfgObject.buildWithBid(bid);
    }

    /**
     * 对象模型行为 - 读取草稿
     */
    public CfgObjectVo readDraft() {
        return PlmContextHolder.getBean(ExtensionService.class).readDraft(super.getBid());
    }

    /**
     * 构建undoCheckout ObjectModel
     */
    public static CfgObject buildWithUndoCheckout(UndoCheckoutParam undoCheckoutParam) {
        return BeanUtil.copy(undoCheckoutParam, CfgObject.class);
    }

    /**
     * 对象模型行为 - undoCheckout
     */
    public Boolean undoCheckout() {
        return transactionTemplate.execute(transactionStatus -> {
            //无论有没有草稿都删草稿 && 更新对象
            //删草稿 - 无论有没有草稿都执行删除草稿操作
            Boolean deleteDraft = PlmContextHolder.getBean(ExtensionService.class).deleteDraftData(getBid());
            //更新对象 - 无论有没有草稿都把草稿状态去掉
            this.setDraft(Boolean.FALSE);
            CfgObjectPo po = BeanUtil.copy(this, CfgObjectPo.class);
            Boolean updateObject = cfgObjectRepository.batchUpdateAndHistory(Lists.newArrayList(po));
            //删锁
            Boolean deleteLock = CfgObjectLock.build(getModelCode()).deleteLock();
            log.info("执行撤销检出操作，删除草稿结果：{}；更新对象结果：{}；删除锁结果：{}", deleteDraft, updateObject, deleteLock);
            return true;
        });
    }

    /**
     * 构建checkin ObjectModel
     */
    public static CfgObject buildWithCheckin(CheckinParam checkinParam) {
        return BeanUtil.copy(checkinParam, CfgObject.class);
    }

    public CfgObject checkObjectNameCannotBeSameWithCheckin() {
        CfgObjectPo po = cfgObjectRepository.getByModelCode(modelCode);
        Integer count = cfgObjectRepository.findObjectCountByName(name);
        if (!ObjectUtil.equals(po.getName(), name) && count > 0) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(),
                    "对象中已存在相同名称【" + name + "】的对象");
        }
        return this;
    }

    /**
     * 对象模型行为 - checkin
     */
    public Boolean checkin(CheckinParam checkinParam) {
        //过滤重复熟悉数据
        checkinParam.setAttrList(filterAttrList(checkinParam.getAttrList()));
        //检入之前先查询下锁是否存在（查询的时候需要传工号，确认是同一人），如果不存在说明有人登录账号检入或者撤销检入
        CfgObjectLock lock = CfgObjectLock.build(modelCode).findAdapterModelCode();
        if (ObjectUtil.isEmpty(lock)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), "对象已被检入或者撤销检入");
        }
        editObjectAndAttr(checkinParam);
        return true;
    }

    /**
     * 过滤CheckinParam对象中List<CfgObjectAttributeVo> attrList中的重复code数据
     */
    public static List<CfgObjectAttributeVo> filterAttrList(List<CfgObjectAttributeVo> attrList) {
        List<CfgObjectAttributeVo> list = Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(attrList)) {
            Map<String, CfgObjectAttributeVo> map = Maps.newHashMap();
            for (CfgObjectAttributeVo vo : attrList) {
                if (map.containsKey(vo.getCode())) {
                    continue;
                }
                map.put(vo.getCode(), vo);
                list.add(vo);
            }
        }
        return list;
    }
    /**
     * 对象模型行为 - editObjectAndAttr
     */
    public CfgObject editObjectAndAttr(CheckinParam checkinParam) {
        transactionTemplate.execute(transactionStatus -> {
            //对象 -- 删新表的对象，插入数据 && 子对象 -- 父版本+1
            //删除新表对象
            Boolean deleteObject = cfgObjectRepository.delete(modelCode);
            Integer thisVersion = checkinParam.getVersion()!=null?checkinParam.getVersion():version;
            Integer newVersion = thisVersion + 1;
            this.setDraft(Boolean.FALSE).setVersion(newVersion).setModelVersionCode(modelCode + ":" + thisVersion);
            CfgObjectPo po = cfgObjectRepository.getByModelCode(modelCode);
            CfgObjectPo object = BeanUtil.copy(this, CfgObjectPo.class);
            if(po != null){
                object.setTableBid(po.getTableBid());
            }
            //插入数据(包含新表和历史表)
            object.setCreatedBy(SsoHelper.getJobNumber());
            object.setUpdatedBy(SsoHelper.getJobNumber());
            object.setVersionFlag(checkinParam.getVersionFlag()!=null?checkinParam.getVersionFlag():versionFlag);
            object.setType((StringUtils.isNotEmpty(checkinParam.getType()))?checkinParam.getType():type);
            Boolean addObject = cfgObjectRepository.add(object);
            //子对象 -- 父版本+1 && 把最新的子数据复制一份到历史表
            Boolean updateChildren = cfgObjectRepository.updateChildrenAndCopyToHistory(object);

            //属性的检入操作
            Boolean checkinAttr = CfgObjectAttrRoot.build().checkin(this);

            //删草稿 - 无论有没有草稿都执行删除草稿操作
            Boolean deleteDraft = PlmContextHolder.getBean(ExtensionService.class).deleteDraftData(modelCode);

            //删锁
            Boolean deleteLock = CfgObjectLock.build(modelCode).deleteLock();

            log.info("执行检入操作，删除对象：{}；插入对象结果：{}；更新子对象结果：{}；属性检入结果：{}，删除草稿结果：{}；删除锁结果：{}",
                    deleteObject, addObject, updateChildren, checkinAttr, deleteDraft, deleteLock);

            return true;
        });
        return this;
    }

    /**
     * 构建bid转modelCode ObjectModel
     */
    public static CfgObject buildWithBid(String bid) {
        CfgObject cfgObject = new CfgObject();
        cfgObject.setBid(bid);
        return cfgObject;
    }

    /**
     * 对象模型行为 - 转modelCode
     */
    public String toModelCode() {
        return cfgObjectRepository.getModelCodeByObjectBid(getBid());
    }

    public CfgObject populateBaseInfoByModelCode() {
        CfgObjectPo cfgObject = cfgObjectRepository.getByModelCode(modelCode);
        if(cfgObject == null){
            cfgObject = new CfgObjectPo();
        }
        return CfgObjectConverter.INSTANCE.po2entity(cfgObject);
    }

    /**
     * 填充超类属性
     * @return CfgObject
     */
    public CfgObject populateSupperAttributes() {
        String objectType = this.getType();
        Set<String> objectTypes = ObjectTypeTool.collectObjectTypeAssemble(objectType);
        // 收集超类
//        List<CfgObjectAttributeVo> superAttrs = Lists.newArrayList();
        List<CfgObjectAttributePo> listByModelCodeList = cfgObjectAttributeRepository.listSupperAttr(objectTypes);
        List<CfgObjectAttributeVo> superAttrs = CfgObjectAttributeConverter.INSTANCE.pos2vos(listByModelCodeList);
        if (!CollectionUtils.isEmpty(superAttrs)){
            superAttrs.forEach(attr->
                attr.setInherit(Boolean.TRUE)
            );
            superAttrs.addAll(this.getAttrList());
            setAttrList(superAttrs);
        }
        // 收集的通用属性
        return this;
    }


}
