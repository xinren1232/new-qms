package com.transcend.plm.alm.sync;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.alm.demandmanagement.entity.ao.SelectAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.datadriven.api.feign.DictionaryFeign;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.api.service.UserDemandService;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmFlowQo;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.repository.entity.TonesAr;
import com.transcend.plm.datadriven.apm.permission.repository.entity.TonesIr;
import com.transcend.plm.datadriven.apm.permission.repository.entity.TonesSr;
import com.transcend.plm.datadriven.apm.permission.repository.service.TonesArService;
import com.transcend.plm.datadriven.apm.permission.repository.service.TonesIrService;
import com.transcend.plm.datadriven.apm.permission.repository.service.TonesSrService;
import com.transcend.plm.datadriven.apm.permission.service.IPlatformUserWrapper;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.NumberConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.sdk.core.config.CredentialsProperties;
import com.transsion.framework.uac.service.IUacLoginService;
import com.transsion.sdk.push.domain.message.element.Button;
import com.transsion.sdk.push.domain.message.element.Text;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.DASH;

@Service
@Slf4j
public class SyncIrTaskService {
    @Autowired
    private TonesIrService irService;

    @Autowired
    private TonesSrService srService;

    @Autowired
    private TonesArService arService;

    @Autowired
    private DictionaryFeign IpmDictionaryFeign;

    @Resource
    private UserDemandService userDemandService;
    @Resource
    private IUacLoginService iUacLoginService;
    @Resource
    private CredentialsProperties credentialsProperties;

    @Resource
    private DemandManagementService demandManagementService;

    @Autowired
    private SyncTonesIrProperties irProperties;

    @Resource
    private IAppDataService appDataService;


    @Value("${spring.application.sys-code}")
    private String sysCode;

    @Resource
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IPlatformUserWrapper platformUserWrapper;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;
    @Resource
    private DictionaryFeignClient transcendDictionaryFeignClient;

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;

    @Resource
    private IRuntimeService runtimeService;

    @Value("${apm.web.url:https://alm.transsion.com/#}")
    private String apmWebUrl;

    @Value("${apm.instance.web.path:/share/info/%s/%s/%s?viewMode=tableView}")
    private String apmInstanceWebPathTemplate;

    @Value("${apm.flow.todoCenter.appName:Transcend}")
    private String todoCenterAppName;

    @Value("${apm.msg.picture.url:https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1702983291787/20231219-185031.png}")
    private String apmMsgPictureUrl;



    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final Pattern JOBNUM_PATTERN = Pattern.compile("(?<=（)[^）]+");

    private static final String ERROR_MESSAGE = "Transcend/IPM字典[%s]没有找到对应的中文:[%s]";

/**
 * 需要登录认证的文件类型
 * **

 private static final List<String> AUTH_FILE_TYPE = Lists.newArrayList("png", "jpg", "jpeg", "svg", "gif", "mp4","avi","mov","wmv","rmvb","mkv","m4v","3gp","flv");

 */
/**
 * 特殊国家替换
 * **

 private static final Map<String,String> COUNTRY_CODE_MAP = new HashMap<>();
 static {
 COUNTRY_CODE_MAP.put("俄罗斯", "EE1");
 COUNTRY_CODE_MAP.put("沙特", "沙特阿拉伯");
 COUNTRY_CODE_MAP.put("内罗毕", "肯尼亚");
 COUNTRY_CODE_MAP.put("SSA", "肯尼亚");
 COUNTRY_CODE_MAP.put("刚果", "刚果（金）");
 COUNTRY_CODE_MAP.put("印尼", "印度尼西亚");
 COUNTRY_CODE_MAP.put("海外", "中国");
 COUNTRY_CODE_MAP.put("国内", "中国");
 COUNTRY_CODE_MAP.put("科尼亚", "土耳其");
 COUNTRY_CODE_MAP.put("全市场", "中国");
 COUNTRY_CODE_MAP.put("委内瑞拉", "SA1");
 COUNTRY_CODE_MAP.put("孟加拉", "孟加拉国");
 }

 */
/**
 * 特殊领域替换
 * **

 private static final Map<String,String> DOMAIN_MAP = new HashMap<>();
 static {
 DOMAIN_MAP.put("1007870", "影像");
 DOMAIN_MAP.put("1007477", "系统应用-系统功能与特性");
 DOMAIN_MAP.put("1007330", "AI");
 DOMAIN_MAP.put("1007235", "系统应用-系统功能与特性");
 DOMAIN_MAP.put("1007214", "系统应用-系统功能与特性");
 DOMAIN_MAP.put("1006339", "互联互通");
 DOMAIN_MAP.put("1005904", "三方体验");
 DOMAIN_MAP.put("1005516", "系统应用-系统功能与特性");
 DOMAIN_MAP.put("1005446", "系统应用-系统功能与特性");
 DOMAIN_MAP.put("1005301", "多媒体");
 DOMAIN_MAP.put("1005220", "系统应用-用户服务与运营");
 DOMAIN_MAP.put("1005191", "系统应用-");
 DOMAIN_MAP.put("1002285", "预装");
 }

 */
/**
 * 创建人离职人员工号替换
 * **

 private static final Map<String,String> RESIGNED_JOB_NUM = new HashMap<>();
 static {
 RESIGNED_JOB_NUM.put("18651076", "18653434");
 RESIGNED_JOB_NUM.put("18620249", "18653434");
 RESIGNED_JOB_NUM.put("18651913", "18650099");
 RESIGNED_JOB_NUM.put("18645254", "18653434");
 RESIGNED_JOB_NUM.put("18653106", "18653434");
 }

 */
/**
 * 特殊OS版本替换
 * **

 private static final Map<String,String> OS_VERSION = new HashMap<>();
 static {
 OS_VERSION.put("XOS10.6.0", "XOS10.6");
 OS_VERSION.put("13.1.0 XOS", "XOS13.1.0");
 OS_VERSION.put("hios 13.6", "HiOS13.6.0");
 OS_VERSION.put("xos10.6.0", "XOS10.6");
 OS_VERSION.put("HiOS13", "HiOS13.0.0");
 OS_VERSION.put("HiOS 13.0", "HiOS13.0.0");
 OS_VERSION.put("XOS13.0.0", "XOS13.0.0");
 OS_VERSION.put("13.5.0 HiOS", "HiOS13.5.0");
 OS_VERSION.put("XOS8.6", "影像");
 OS_VERSION.put("13.0.0 HiOS", "HiOS13.0.0");
 OS_VERSION.put("XOS 14.0.0", "XOS14.0.0");
 OS_VERSION.put("XOS14.0.0", "XOS14.0.0");
 OS_VERSION.put("XOS8.0", "影像");
 OS_VERSION.put("hios13.0.0", "HiOS13.0.0");
 OS_VERSION.put("13.5.0 XOS", "XOS13.5.0");
 OS_VERSION.put("XOS 13.0", "XOS13.0.0");
 OS_VERSION.put("10.6.0 XOS", "XOS10.6");
 OS_VERSION.put("OS12", "XOS12.0.0");
 OS_VERSION.put("14.0.0 HiOS", "HiOS14.0.0");
 OS_VERSION.put("xos13.5.0", "XOS13.5.0");
 OS_VERSION.put("itelOS13.5", "itelOS13.5.0");
 OS_VERSION.put("HiOS 13.5", "HiOS13.5.0");
 OS_VERSION.put("12.0.0 HiOS", "HiOS12.0");
 OS_VERSION.put("12.6.0 HiOS", "HiOS12.6.0");
 OS_VERSION.put("13.0.0 HiOS（直板机13.0.0）", "HiOS13.0.0");
 OS_VERSION.put("12.0.0 XOS", "XOS12.0.0");
 OS_VERSION.put("HiOS13.0.0", "HiOS13.0.0");
 OS_VERSION.put("XOS 13.1", "XOS13.1.0");
 OS_VERSION.put("8.6.0 HiOS", "HiOS8.6");
 OS_VERSION.put("HiOS13.5", "HiOS13.5.0");
 OS_VERSION.put("XOS13.1", "XOS13.1.0");
 OS_VERSION.put("13.0 XOS", "XOS13.0.0");
 }

 public Boolean createRrCode(JSONObject params) {
 查询数据库每天的最大编码
 List<MObject> rrList = getRrList();
 Set<String> existCodes = rrList.stream()
 .filter(rr -> !ObjectUtils.isEmpty(rr.get("requirementcoding")))
 .map(rr -> (String)rr.get("requirementcoding"))
 .collect(Collectors.toSet());
 Map<String, Integer> existCodeMap = existCodes.stream()
 .filter(code -> code.length() >= 13)
 .collect(Collectors.toMap(code -> code.substring(3, 9), code -> Integer.parseInt(code.substring(10, 13)), Math::max));
 查询Tones数据
 List<TonesRr> list;
 if (params.containsKey("tonesIds")){
 List tonesIds = (List)params.get("tonesIds");
 list = tonesRrService.list(Wrappers.<TonesRr>lambdaQuery().in(TonesRr::getTonesId, tonesIds));
 }else if (params.containsKey("startTonesId") && params.containsKey("endTonesId")){
 String startTonesId = params.getString("startTonesId");
 String endTonesId = params.getString("endTonesId");
 list = tonesRrService.list(Wrappers.<TonesRr>lambdaQuery().between(TonesRr::getTonesId, startTonesId, endTonesId));
 }else {
 list = tonesRrService.list();
 }
 for (TonesRr tonesRr : list) {
 Date createdTime = tonesRr.getCreatedTime() == null ? new Date() : tonesRr.getCreatedTime();
 StringBuilder resultCode = new StringBuilder("RR");
 resultCode.append(DASH);
 String currentDate = DateUtil.format(createdTime, "yyMMdd");
 resultCode.append(currentDate);
 resultCode.append(DASH);
 Integer currentSerialNumber = 0;
 if (existCodeMap.containsKey(currentDate)) {
 currentSerialNumber = existCodeMap.get(currentDate);
 }
 currentSerialNumber++;
 existCodeMap.put(currentDate, currentSerialNumber);
 if (currentSerialNumber < NumberConstant.TEN) {
 resultCode.append("00").append(currentSerialNumber);
 } else if (currentSerialNumber < NumberConstant.HUNDRED) {
 resultCode.append("0").append(currentSerialNumber);
 } else {
 resultCode.append(currentSerialNumber);
 }
 tonesRr.setRequirementcoding(resultCode.toString());
 }
 tonesRrService.updateBatchById(list);
 return true;
 }

 public Boolean syncData(JSONObject params) throws Exception {
 来源国家为空默认为中国
 产品线为空的默认为全系产品线

 来源渠道查的Transcend字典SOURCECHANNEL，都是单选
 国家查的IPM ITEM_NATION字典
 版本号型号 service-ipm-tones/ipm-tones/project/info/getCommonCheckBox
 品牌查的IPM ALM_BRAND （tones是单选）
 产品线查的IPM ALM_SERIES（tones是多选，用','分割）
 机型查的接口 service-mp-project/mp-project/queryAllProjectName
 android版本号查的IPM ANDROID_VERSION
 反馈人数：int类型
 OS版本查的IPM字典  OS_VERSION
 JIRA编号:文本
 优先级查的Transcend字典：     PRORITY
 提报人：transcend是工号单选，tones是姓名(工号)：Hamza Jamal Pasha(19220264)
 原始需求提出人：transcend是'工号'，  tones是：包维瑾
 小模块 待处理
 需求类型分类 查的transcend字典：CLASSTYPES  tones是多选
 需求类型分类子项  查的transcend字典：REQUIREMENTTYPECLASSIFICATIONSUBITEMS
 新功能子线 查的transcend字典：NEWFEATURESUBITEMS
 价值评估（评估结果）查的transcend字典：PASSED_AND_NOT_PASSED
 评估不通过原因(评估拒绝原因）查的transcend字典：RESONFORVALUENOTPASSING
 评估不通过原因说明（评估拒绝原因说明）：文本
 预估实现信息备注：预计导入项目+“;”+预计发布的OS版本号+“;”+预计发布独立应用版本号
 是否通过（验证结果）PASSEDORNOT
 不通过原因类型（最近一次驳回原因）查的transcend字典：REASONTYPEFORFAILURE
 申诉评估不通过原因说明（退回原因补充说明）
 Set<String> errorMsg = new HashSet<>();
 Set<String> notFindModule = new HashSet<>();
 Set<String> notFindDomain = new HashSet<>();
 查询所有数据
 List<TonesRr> rrlist = new ArrayList<>();
 if (params.containsKey("tonesIds")){
 List tonesIds = (List)params.get("tonesIds");
 rrlist = tonesRrService.list(Wrappers.<TonesRr>lambdaQuery().in(TonesRr::getTonesId, tonesIds));
 }else if (params.containsKey("startTonesId") && params.containsKey("endTonesId")){
 String startTonesId = params.getString("startTonesId");
 String endTonesId = params.getString("endTonesId");
 rrlist = tonesRrService.list(Wrappers.<TonesRr>lambdaQuery().between(TonesRr::getTonesId, startTonesId, endTonesId));
 }else {
 rrlist = tonesRrService.list();
 }
 查询小模块查询小模块与领域对应关系
 List<TonesRrModuleDomain> moduleDomainList = tonesRrModuleDomainService.list();
 Map<String, TonesRrModuleDomain> moduleDomainMap = moduleDomainList.stream().collect(Collectors.toMap(TonesRrModuleDomain::getModuleName, Function.identity(), (v1, v2) -> v1));
 通过子领域名称查询BID
 Map<String, MObject> domainNameMap = getDomainNameMap();
 查询Tones字典
 List<TonesRrDict> tonesRrDictList = tonesRrDictService.list();
 Map<String, Map<String, String>> tonesRrDictMap = tonesRrDictList.stream().collect(Collectors.groupingBy(TonesRrDict::getAttr, Collectors.toMap(TonesRrDict::getDictKey, TonesRrDict::getDictValue,(k1, k2)->k1)));
 Map<String, Map<String, String>> tonesRrDictDescMap = tonesRrDictList.stream().collect(Collectors.groupingBy(TonesRrDict::getAttr, Collectors.toMap(TonesRrDict::getDictKey, TonesRrDict::getDictDesc,(k1, k2)->k1)));

 查询ipm字典
 Object ipmDictVoList = IpmDictionaryFeign.getChangeMessage("IPM", Lists.newArrayList("ITEM_NATION",
 "ALM_BRAND", "ALM_SERIES", "ANDROID_VERSION", "OS_VERSION"));
 Map<String, Map<String, String>> ipmDictMap = toMap(ipmDictVoList);
 查询transcend字典
 CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
 qo.setCodes(Lists.newArrayList("REQUIREMENTTYPECLASSIFICATIONSUBITEMS","CLASSTYPES","PASSED_AND_NOT_PASSED","PRORITY","PASSEDORNOT", "SOURCECHANNEL","NEWFEATURESUBITEMS","RESONFORVALUENOTPASSING","REASONTYPEFORFAILURE"));
 List<CfgDictionaryVo> transcendDictVoList = transcendDictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getData();
 Map<String, Map<String, String>> transcendDictMap = new HashMap<>();
 for (CfgDictionaryVo cfgDictionaryVo : transcendDictVoList) {
 List<CfgDictionaryItemVo> dictionaryItems = cfgDictionaryVo.getDictionaryItems();
 HashMap<String, String> objectObjectHashMap = new HashMap<>();
 for (CfgDictionaryItemVo dictionaryItem : dictionaryItems) {
 objectObjectHashMap.put(dictionaryItem.get("zh").toString(),dictionaryItem.getKeyCode());
 }
 transcendDictMap.put(cfgDictionaryVo.getCode(),objectObjectHashMap);
 }
 查询tones版本号型号
 Object tonesDict = getTonesDict();

 需求增加多个领域的数据
 Map<String, List<String>> addDomainMap = new ConcurrentHashMap<>();
 Map<String, String> domainBidNameMap = new HashMap<>();
 组装transcend RR实例数据
 List<MSpaceAppData> targetMObjects = new ArrayList<>();
 for (TonesRr tonesRr : rrlist) {
 MSpaceAppData mSpaceAppData = new MSpaceAppData();
 String bid = SnowflakeIdWorker.nextIdStr();
 mSpaceAppData.put("tonesId", tonesRr.getTonesId());
 mSpaceAppData.setBid(bid);
 mSpaceAppData.put("dataBid",bid);
 mSpaceAppData.setName(tonesRr.getName());
 mSpaceAppData.put("demandDesc", tonesRr.getDemandDesc());
 附件
 mSpaceAppData.put("files",tonesRr.getFiles());
 软硬件分类
 mSpaceAppData.put("softwareAndHardwareClassification", "Softwarerequirements");
 产品经理
 mSpaceAppData.put("tonesProductManager", getJobNumList(tonesRr.getTonesProductManager()));
 需求编码
 mSpaceAppData.setCreatedTime(asLocalDate(tonesRr.getCreatedTime()));
 mSpaceAppData.put("requirementcoding",tonesRr.getRequirementcoding());
 来源渠道，都是单选字符串
 if (StringUtils.isNoneBlank(tonesRr.getSourceChannel())){
 String tonesValue = getTonesValue("source_channel",tonesRr.getSourceChannel(),tonesRrDictMap, errorMsg);
 String sourcechannel = getTranscendDictKeyByZh("SOURCECHANNEL", tonesValue, transcendDictMap,errorMsg);
 mSpaceAppData.put("sourceChannel",sourcechannel);
 }
 来源国家，Tones多选,transcend多选
 if (StringUtils.isNoneBlank(tonesRr.getSourceCountry())){
 List<String> list = Arrays.asList(tonesRr.getSourceCountry().split("[,/]"));
 List<String> sourceCountry = getSourceCountry("ITEM_NATION", list, ipmDictMap,errorMsg);
 mSpaceAppData.put("sourceCountry",sourceCountry);
 }else {
 mSpaceAppData.put("sourceCountry",Lists.newArrayList("CN"));
 }
 mSpaceAppData.put("versionNumberModel", tonesRr.getVersionNumberModel());
 mSpaceAppData.put("comment", tonesRr.getComment());
 banbenno os版本号
 if (StringUtils.isNotEmpty(tonesRr.getBanbenno())){
 String banbenno = tonesRr.getBanbenno();
 if (OS_VERSION.containsKey(tonesRr.getBanbenno())){
 banbenno = OS_VERSION.get(tonesRr.getBanbenno());
 }
 mSpaceAppData.put("banbenno", getTranscendDictKeyByZh("OS_VERSION", banbenno, ipmDictMap, errorMsg));
 }
 品牌，默认为全系品牌
 String almBrand = getBrand("ALM_BRAND", tonesRr.getBrand(), ipmDictMap,errorMsg);
 mSpaceAppData.put("brand", almBrand);
 产品线,Tones单选,transcend单选,默认全系产品线
 String productLine = getProductLine("ALM_SERIES", tonesRr.getProductLine(), ipmDictMap, errorMsg);
 mSpaceAppData.put("productLine",productLine);
 mSpaceAppData.put("model", tonesRr.getModel());
 Android版本号
 if ("Android 13-Go".equals(tonesRr.getAndroidVerNo())){
 tonesRr.setAndroidVerNo("Android 13 GO");
 }
 if ("Android 12-Go".equals(tonesRr.getAndroidVerNo())){
 tonesRr.setAndroidVerNo("Android 12 GO");
 }
 String androidVersion = getTranscendDictKeyByZh("ANDROID_VERSION", tonesRr.getAndroidVerNo(), ipmDictMap,errorMsg);
 mSpaceAppData.put("androidVerNo", androidVersion);
 反馈人数
 String numberOfFeedback = tonesRr.getNumberOfFeedback() == null?"":tonesRr.getNumberOfFeedback().toString();
 mSpaceAppData.put("numberOfFeedback", numberOfFeedback);
 jira编号
 mSpaceAppData.put("jiraCode", tonesRr.getJiraCode());
 优先级
 String tonesPriority = getTonesValue("priority",tonesRr.getPriority(),tonesRrDictMap,errorMsg);
 String priority = getTranscendDictKeyByZh("PRORITY", tonesPriority, transcendDictMap,errorMsg);
 mSpaceAppData.put("priority", priority);
 提报人
 String reportedBy = getJobNum(tonesRr.getReportedBy());
 if (StringUtils.isNoneBlank(reportedBy)){
 mSpaceAppData.put("reportedBy", reportedBy);
 提报人部门
 ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(reportedBy);
 if(apmUser != null && com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(apmUser.getDepts()) && StringUtils.isNotBlank(apmUser.getDepts().get(0).getName())){
 mSpaceAppData.put("deP", apmUser.getDepts().get(0).getName());
 }
 }
 原始需求提出人
 mSpaceAppData.put("originalProposer",tonesRr.getOriginalProposer());
 生命周期
 if (StringUtils.isNotEmpty(tonesRr.getLifeCycleCode())){
 String lifeCycleCode = tonesRrDictDescMap.get("life_cycle_code").get(tonesRr.getLifeCycleCode());
 mSpaceAppData.put("lifeCycleCode",lifeCycleCode);
 if (Lists.newArrayList("COMPLETED","INPLANING","DOMAINVALUEASSESSMENT","VERIFIED","BUG_REJECTED").contains(lifeCycleCode)){
 mSpaceAppData.put("operationalFilteringEvaluation","tongguo");
 }else if (Lists.newArrayList("INVALID").contains(lifeCycleCode)){
 mSpaceAppData.put("operationalFilteringEvaluation","butongguo");
 }
 }
 需求分类，都是多选
 if (StringUtils.isNoneBlank(tonesRr.getClassificationOfDemandTypes())){
 List<String> tonesValue = getTonesValueMulti("classification_of_demand_types",tonesRr.getClassificationOfDemandTypes(),tonesRrDictMap,errorMsg);
 List<String> sourcechannel = getTranscendDictKeyMultiByZh("CLASSTYPES", tonesValue, transcendDictMap,errorMsg);
 mSpaceAppData.put("classificationOfDemandTypes",sourcechannel);
 }
 需求分类子项，都是多选
 if (StringUtils.isNoneBlank(tonesRr.getRequirementTypeClassificationSubItems())){
 List<String> tonesValue = getTonesValueMulti("requirement_type_classification_sub_items",tonesRr.getRequirementTypeClassificationSubItems(),tonesRrDictMap,errorMsg);
 List<String> sourcechannel = getTranscendDictKeyMultiByZh("REQUIREMENTTYPECLASSIFICATIONSUBITEMS", tonesValue, transcendDictMap,errorMsg);
 mSpaceAppData.put("requirementTypeClassificationSubItems",sourcechannel);
 }
 新功能子项
 if (StringUtils.isNoneBlank(tonesRr.getNewFeatureSubItems())){
 List<String> tonesValue = getTonesValueMulti("new_feature_sub_items",tonesRr.getNewFeatureSubItems(),tonesRrDictMap,errorMsg);
 List<String> sourcechannel = getTranscendDictKeyMultiByZh("NEWFEATURESUBITEMS", tonesValue, transcendDictMap,errorMsg);
 mSpaceAppData.put("newFeatureSubItems",sourcechannel);
 }
 生命周期模板
 mSpaceAppData.put("lcTemplBid",syncTonesRRProperties.getLcTemplBid());
 mSpaceAppData.put("lcTemplVersion",syncTonesRRProperties.getLcTemplVersion());
 是否历史数据
 mSpaceAppData.put("historicalDataIdentification","1");
 分发人
 mSpaceAppData.put("distributor",getJobNum(tonesRr.getFenFaRen()));
 分发时间
 mSpaceAppData.put("distributionTime",tonesRr.getFenFaShiJian());
 价值评估（评估结果）
 mSpaceAppData.put("valueAssessment",getTonesValue("value_assessment",tonesRr.getValueAssessment(),tonesRrDictMap,errorMsg));
 价值评估时间（评估时间）
 mSpaceAppData.put("valueAssessmentTime",tonesRr.getValueAssessmentTime());
 需求预估排期(计划完成时间)
 mSpaceAppData.put("estimatedDemandScheduling",tonesRr.getEstimatedDemandScheduling());

 评估不通过原因(评估拒绝原因）查的transcend字典：RESONFORVALUENOTPASSING
 if (StringUtils.isNoneBlank(tonesRr.getReasonForFailureInEvaluation())){
 String tonesValue = getTonesValue("reason_for_failure_in_evaluation",tonesRr.getReasonForFailureInEvaluation(),tonesRrDictMap, errorMsg);
 String reasonForFailureInEvaluation = getTranscendDictKeyByZh("RESONFORVALUENOTPASSING", tonesValue, transcendDictMap,errorMsg);
 mSpaceAppData.put("reasonForFailureInEvaluation",reasonForFailureInEvaluation);
 }
 评估不通过原因说明（评估拒绝原因说明）：文本
 mSpaceAppData.put("explanationOfReasonsForFailureLnEvaluation",tonesRr.getExplanationOfReasonsForFailureLnEvaluation());
 预估实现信息备注：预计导入项目+“;”+预计发布的OS版本号+“;”+预计发布独立应用版本号
 mSpaceAppData.put("estimatedimplementationinformationremarks",getInfoRemarks(tonesRr));
 关联IR回传整机版本号(验证环境-已导入的整机版本号)
 mSpaceAppData.put("irreturnsthecompletemachineversionnumber",tonesRr.getZhengjibanbenhao());
 IR回传项目(验证环境-导入项目)
 mSpaceAppData.put("irbackhaulproject",tonesRr.getDaoruxiangmu());
 是否通过（验证结果）PASSEDORNOT
 if (StringUtils.isNotEmpty(tonesRr.getPassedOrNot()) && !"-".equals(tonesRr.getPassedOrNot())){
 if ("通过".equals(tonesRr.getPassedOrNot())){
 mSpaceAppData.put("passedOrNot","Verificationpassed");
 }else if (("不通过".equals(tonesRr.getPassedOrNot()))){
 mSpaceAppData.put("passedOrNot","Verificationfailed");
 }else {
 errorMsg.add(String.format("属性：【%s】没有找到对应的key:[%s]","passedOrNot",tonesRr.getPassedOrNot()));
 }
 }
 流程参数workItemType
 mSpaceAppData.put("workItemType",syncTonesRRProperties.getWorkItemType());
 创建人
 String createdBy = getJobNum(tonesRr.getCreatedBy());
 if (RESIGNED_JOB_NUM.containsKey(createdBy)){
 createdBy = RESIGNED_JOB_NUM.get(createdBy);
 }
 mSpaceAppData.put("createdBy",createdBy);
 评估人
 mSpaceAppData.put("assessor",getJobNumList(tonesRr.getValueAssessmentPeople()));
 价值评估人
 mSpaceAppData.put("valueAssessmentPeople",getSingleJobNum(tonesRr.getValueAssessmentPeople()));
 验证人
 mSpaceAppData.put("verifier",getJobNum(tonesRr.getVerifier()));
 验证时间
 mSpaceAppData.put("verificationTime",tonesRr.getVerifyTime());
 领域“productArea”
 一、小模块对应领域信息，已经补充，见表格；
 其中，有两个小模块Phantom和Camon，它不是唯一对应一种领域，需要手工处理。
 我已经把包含Phantom和Camon小模块的所有需求，导出来了。（如下）
 1、当小模块中，除了这两个模块外，还有其他模块时，则使用其他模块对应的领域，忽略Phantom和Camon小模块。
 2、当小模块中只有Phantom和Camon的需求（我标记黄色背景）时，我对每一条需求都手动做了领域对应（在D列黑色背景的）。
 当小模块是「退回评估人」时，需进一步判断，若状态为「评估中」或「延后评估」时，实际上这条需求就是已退回了，
 需要按「已退回」状态处理。此时，领域信息可空着，不用赋值；（当小模块是「退回评估人」时，若状态非以上两种，则不需要特殊处理。
 本身是退回则为退回，本身是拒绝则为拒绝；这两种情况时，可不解析领域。）
 if (StringUtils.isNotEmpty(tonesRr.getXiaoMoKuai())){
 List<String> list = Arrays.asList(tonesRr.getXiaoMoKuai().split("[,，]"));
 List<String> domianBidList = new ArrayList<>();
 如果小模块是「退回分发人」且需求状态是「评估中」或「延后评估」，则也认为需求属于「已退回」状态。按已退回处理。
 if (list.contains("退回分发人") && ("DOMAINVALUEASSESSMENT".equals(mSpaceAppData.getLifeCycleCode()))){
 mSpaceAppData.put("lifeCycleCode","INVALID");
 }
 boolean isPhantom = list.contains("Phantom") || list.contains("Camon");
 for (String xiaoMoKuai : list) {
 xiaoMoKuai = xiaoMoKuai.trim();
 if ("Phantom".equals(xiaoMoKuai) || "Camon".equals(xiaoMoKuai)){
 continue;
 }
 if (!moduleDomainMap.containsKey(xiaoMoKuai)) {
 notFindModule.add(xiaoMoKuai);
 }else {
 TonesRrModuleDomain tonesRrModuleDomain = moduleDomainMap.get(xiaoMoKuai);
 String domainItemName = tonesRrModuleDomain.getDomainItemName();
 if (!domainNameMap.containsKey(domainItemName)){
 notFindDomain.add(domainItemName);
 }else if (!domianBidList.contains(domainNameMap.get(domainItemName).getBid())){
 domainBidNameMap.put(domainNameMap.get(domainItemName).getBid(),domainItemName);
 domianBidList.add(domainNameMap.get(domainItemName).getBid());
 }
 }
 }
 if (CollectionUtils.isEmpty(domianBidList) && isPhantom && DOMAIN_MAP.containsKey(tonesRr.getTonesId())){
 String domainItemName = DOMAIN_MAP.get(tonesRr.getTonesId());
 if (!domainNameMap.containsKey(domainItemName)){
 notFindDomain.add(domainItemName);
 }else if (!domianBidList.contains(domainNameMap.get(domainItemName).getBid())){
 domainBidNameMap.put(domainNameMap.get(domainItemName).getBid(),domainItemName);
 domianBidList.add(domainNameMap.get(domainItemName).getBid());
 }
 }
 if (domianBidList.size() == 1){
 mSpaceAppData.put("productArea",domianBidList.get(0));
 } else if (domianBidList.size() > 1){
 mSpaceAppData.put("productArea",domianBidList.get(0));
 domianBidList.remove(0);
 addDomainMap.put(bid, domianBidList);
 }
 }
 （SRE说明）运营备注，都是文本
 mSpaceAppData.put("sreDescription",tonesRr.getYunYingBeiZhu());
 问题说明（验证不通过原因）
 mSpaceAppData.put("problemDescription",tonesRr.getProblemDescription());
 不通过原因类型（最近一次驳回原因）查的transcend字典：REASONTYPEFORFAILURE
 mSpaceAppData.put("reasonTypeFOrFailure",getTranscendDictKeyByZh("REASONTYPEFORFAILURE",tonesRr.getReasonTypeFOrFailure(), transcendDictMap,errorMsg));
 申诉评估不通过原因说明（退回原因补充说明）
 mSpaceAppData.put("explanationOfReasonsForFailureToPass",tonesRr.getExplanationOfReasonsForFailureToPass());
 targetMObjects.add(mSpaceAppData);
 }
 log.info("不存在的小模块：【{}】",JSON.toJSONString(notFindModule));
 log.info("不存在的子领域：【{}】",JSON.toJSONString(notFindDomain));
 log.info("不存在的字典：【{}】",JSON.toJSONString(errorMsg));
 login("黄远虎(18649100)");
 IUserContext<IUser> user = TranscendUserContextHolder.getUser();
 ThreadPoolExecutor executor = new ThreadPoolExecutor(
 15,
 60,
 120,
 TimeUnit.SECONDS,
 new ArrayBlockingQueue<>(2048),
 Executors.defaultThreadFactory(),
 new ThreadPoolExecutor.CallerRunsPolicy());
 List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
 for (MSpaceAppData targetMObject : targetMObjects) {
 completableFutures.add(CompletableFuture.runAsync(() -> {
 TranscendUserContextHolder.setUser(user);
 MSpaceAppData add = iBaseApmSpaceAppDataDrivenService.addTonesData(syncTonesRRProperties.getRrSpaceAppBid(), targetMObject);
 if (addDomainMap.containsKey(add.getBid())){
 List<String> domainBids = addDomainMap.get(add.getBid());
 if (CollectionUtils.isNotEmpty(domainBids)){
 SelectAo selectAo = new SelectAo();
 List<List<String>> selectedList = new ArrayList<>();
 List<SelectVo> nameList = new ArrayList<>();
 for (String domainBid : domainBids) {
 List<String> bidList = new ArrayList<>();
 bidList.add(domainBid);
 selectedList.add(bidList);
 SelectVo selectVo = new SelectVo();
 selectVo.setName(domainBidNameMap.get(domainBid));
 if (domainNameMap.containsKey(domainBidNameMap.get(domainBid))){
 Object productManager = domainNameMap.get(domainBidNameMap.get(domainBid)).get("productManager");
 if (!ObjectUtils.isEmpty(productManager) && productManager instanceof java.util.List){
 selectVo.setDomainLeader(((List<?>)productManager).get(0).toString());
 }
 }
 selectVo.setBid(domainBid);
 selectVo.setDisabled(false);
 selectVo.setModelCode("A42");
 selectVo.setSpaceAppBid(syncTonesRRProperties.getProductSpaceBid());
 nameList.add(selectVo);
 }
 selectAo.setSelectedList(selectedList);
 selectAo.setNameList(nameList);
 demandManagementService.selectDomain(syncTonesRRProperties.getSpaceBid(),syncTonesRRProperties.getRrSpaceAppBid(),add.getBid(),1,selectAo,null,null);
 }
 }
 }, executor));
 }
 CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
 log.error("多线程同步Tones需求异常", ex);
 throw new TranscendBizException("多线程同步Tones需求异常");
 }).join();
 return true;
 }

 private Map<String, Map<String, String>> toMap(Object ipmDictVoList) {
 Map<String, Object> data = JSON.parseObject(JSONObject.parseObject(JSON.toJSONString(ipmDictVoList)).getString("data"), Map.class);
 Map<String, Map<String, String>> result = new HashMap<>();
 data.forEach((k,v)->{
 Map<String, String> objectObjectHashMap = new HashMap<>();
 for (Object object : JSONObject.parseArray(JSON.toJSONString(v))) {
 JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(object));
 objectObjectHashMap.put(jsonObject.getString("chValue"),jsonObject.getString("key"));
 }
 result.put(k,objectObjectHashMap);
 });
 return result;
 }

 private Object getTonesDict(){
 String url = "https:api-tones.transsion.com/service-ipm-tones/ipm-tones/project/info/getCommonCheckBox";
 HttpPost httpPost = new HttpPost(url);
 RequestConfig config = RequestConfig.custom().setSocketTimeout(600000).setConnectTimeout(600000).setConnectionRequestTimeout(600000).build();
 CloseableHttpClient httpClient = null;
 CloseableHttpResponse response = null;
 HttpEntity entity = null;
 String responseContent = null;
 try {
 httpClient = HttpClients.createDefault();
 httpPost.setConfig(config);
 httpPost.setHeader("p-rtoken","u_MjE3MWl1cTcwaWVwamlyaTlwaGFeNDQ1NjU1ODIxNDc1NTUyNjAyMTgxMjQ1");
 httpPost.setHeader("p-auth","r_MTI3MWVyZWhoNjY1Y3M0OWQzbXJeNDQ1NjU1ODIxNzMwMzUyNjAyMTgxMjQ1");
 HashMap<Object, Object> map = new HashMap<>();
 map.put("enumKey","software_version");
 StringEntity stringEntity = new StringEntity(JSON.toJSONString(map), StandardCharsets.UTF_8.name());
 stringEntity.setContentType("application/json;charset=UTF-8");
 httpPost.setEntity(stringEntity);
 long execStart = System.currentTimeMillis();
 response = httpClient.execute(httpPost);
 long execEnd = System.currentTimeMillis();
 log.info("=================执行post请求耗时：" + (execEnd - execStart) + "ms");
 long getStart = System.currentTimeMillis();
 entity = response.getEntity();
 responseContent = EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
 long getEnd = System.currentTimeMillis();
 log.info("=================获取响应结果耗时：" + (getEnd - getStart) + "ms");
 } catch (Exception var22) {
 log.error("error", var22);
 } finally {
 try {
 if (response != null) {
 response.close();
 }

 if (httpClient != null) {
 httpClient.close();
 }
 } catch (IOException var21) {
 log.error("error", var21);
 }

 }

 return responseContent;
 }

 private String getTonesValue(String attr, String key, Map<String, Map<String, String>> tonesRrDictMap, Set<String> errorMsg){
 if (StringUtils.isEmpty(key)){
 return null;
 }
 Map<String, String> map = tonesRrDictMap.get(attr);
 if (!map.containsKey(key)) {
 errorMsg.add(String.format("Tones属性：%s没有找到对应的key:%s",attr,key));
 }
 return map.get(key);
 }

 private List<String> getTonesValueMulti(String attr, String key, Map<String, Map<String, String>> tonesRrDictMap,Set<String> errorMsg){
 if (StringUtils.isEmpty(key)){
 return null;
 }
 List<String> keyList = Arrays.asList(key.split(","));
 Map<String, String> map = tonesRrDictMap.get(attr);
 List<String> result = new ArrayList<>();
 for (String keyItem : keyList) {
 if (!map.containsKey(keyItem)) {
 errorMsg.add(String.format("Tones属性：%s没有找到对应的key:%s",attr,key));
 }else {
 result.add(map.get(keyItem));
 }
 }
 return result;
 }
 private String getTranscendDictKeyByZh(String code, String name, Map<String, Map<String, String>> transcendRrDictMap,Set<String> errorMsg){
 if (StringUtils.isEmpty(name)){
 return null;
 }
 Map<String, String> map = transcendRrDictMap.get(code);
 if (!map.containsKey(name)) {
 errorMsg.add(String.format(ERROR_MESSAGE,code,name));
 }
 return map.get(name);
 }

 private List<String> getTranscendDictKeyMultiByZh(String code, List<String> nameList, Map<String, Map<String, String>> transcendRrDictMap,Set<String> errorMsg){
 if (CollectionUtils.isEmpty(nameList)){
 return null;
 }
 Map<String, String> map = transcendRrDictMap.get(code);
 List<String> result = new ArrayList<>();
 for (String name : nameList) {
 name = name.trim();
 if (!map.containsKey(name)) {
 errorMsg.add(String.format(ERROR_MESSAGE,code,name));
 }else {
 result.add(map.get(name));
 }
 }
 return result;
 }

 private List<String> getSourceCountry(String code, List<String> nameList, Map<String, Map<String, String>> transcendRrDictMap,Set<String> errorMsg){
 if (CollectionUtils.isEmpty(nameList)){
 return null;
 }
 Map<String, String> map = transcendRrDictMap.get(code);
 List<String> result = new ArrayList<>();
 for (String name : nameList) {
 name = name.trim();
 if (COUNTRY_CODE_MAP.containsKey(name)) {
 name = COUNTRY_CODE_MAP.get(name);
 }
 if (!map.containsKey(name)) {
 errorMsg.add(String.format(ERROR_MESSAGE,code,name));
 }else {
 result.add(map.get(name));
 }
 }
 return result;
 }

 private String getJobNum(String user){
 if (StringUtils.isEmpty(user)){
 return "";
 }
 List<String> strList = new ArrayList<>();
 Matcher matcher = JOBNUM_PATTERN.matcher(user);
 while(matcher.find()){
 strList.add(matcher.group());
 }
 if (strList.isEmpty()){
 log.info("工号错误：【{}】",user);
 return "";
 }
 return strList.get(0);
 }

 private List<String> getJobNumList(String user){
 if (StringUtils.isEmpty(user)) {
 return new ArrayList<>();
 }
 String[] userList = user.split(",");
 List<String> strList = new ArrayList<>();
 for (String s : userList) {
 Matcher matcher = JOBNUM_PATTERN.matcher(s);
 if (matcher.find()) {
 strList.add(matcher.group());
 }
 }
 return strList;
 }

 private String getSingleJobNum(String user){
 if (StringUtils.isEmpty(user)) {
 return null;
 }
 String[] userList = user.split(",");
 List<String> strList = new ArrayList<>();
 for (String s : userList) {
 Matcher matcher = JOBNUM_PATTERN.matcher(s);
 if (matcher.find()) {
 return matcher.group();
 }
 }
 return null;
 }

 private void login(String modifier) throws Exception {
 String userName = "IPMPublicAccount";
 String password = "IPMPublicAccount";
 RSAKeyPairDTO rsaKeyPair = iUacLoginService.getRsaKeyPair().getData();
 String pwd = RSAUtils.encryptByPublicKey(password, rsaKeyPair.getPublicKey(), StandardCharsets.UTF_8.name());
 LoginRequest loginRequest = LoginRequest.builder().username(userName).pwd(
 pwd).appId(credentialsProperties.getAppKey()).verifyKey(rsaKeyPair.getVerifyKey()).build();
 LoginUserDTO loginUserDTO = iUacLoginService.loginByAccount(loginRequest).getData();
 IHeaderContext headerContext = getHeaderContext(loginUserDTO);
 RequestContextHolder2.setContext(headerContext);
 IUserContext<IUser> userContext = getUserContext(loginUserDTO.getUtoken(), modifier);
 TranscendUserContextHolder.setUser(userContext);
 }

 @NotNull private IHeaderContext getHeaderContext(LoginUserDTO loginUserDTO) {
 Map<String, String> headerMap = Maps.newHashMap();
 headerMap.put(SysGlobalConst.HTTP_HEADER_X_APP_ID, credentialsProperties.getAppKey());
 headerMap.put(SysGlobalConst.HTTP_HEADER_X_AUTH, loginUserDTO.getRtoken());
 headerMap.put(SysGlobalConst.HTTP_HEADER_X_RTOKEN, loginUserDTO.getUtoken());
 headerMap.put(SysGlobalConst.HTTP_HEADER_X_EMP_NO, loginUserDTO.getEmployeeNo());
 headerMap.put(SysGlobalConst.HTTP_HEADER_X_SYS_CODE, sysCode);
 return new DefaultHeaderContext(headerMap);
 }

 @NotNull private IUserContext<IUser> getUserContext(String uToken, String modifier) {
 UserLoginDto emp = new UserLoginDto();
 String name = StringUtils.substringBetween(modifier, "", "(");
 String employeeNo = StringUtils.substringBetween(modifier, "(", ")");
 emp.setRealName(name);
 emp.setEmployeeNo(employeeNo);
 return new UserContextDto<>(uToken, emp);
 }

 查询所有子领域名称对应bid
 private Map<String, MObject> getDomainNameMap(){
 com.transcend.plm.datadriven.api.model.QueryWrapper qo = new com.transcend.plm.datadriven.api.model.QueryWrapper();
 qo.eq("deleteFlag", 0);
 QueryCondition queryCondition = new QueryCondition();
 List<com.transcend.plm.datadriven.api.model.QueryWrapper> queryWrappers = com.transcend.plm.datadriven.api.model.QueryWrapper.buildSqlQo(qo);
 QueryWrapper qo2 = new QueryWrapper();
 qo2.eq("spaceBid", syncTonesRRProperties.getSpaceBid());
 QueryWrapper qo3 = new QueryWrapper();
 领域维护应用BID
 qo3.in("spaceAppBid", syncTonesRRProperties.getProductSpaceBid());
 queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo2));
 queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo3));
 queryCondition.setQueries(queryWrappers);
 BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
 pageQo.setParam(queryCondition);
 pageQo.setCurrent(1);
 pageQo.setSize(1000);
 PagedResult<MObject> page = objectModelCrudI.page("A42",pageQo, true);
 Map<String, MObject> objectObjectHashMap = new HashMap<>();
 for (MObject datum : page.getData()) {
 objectObjectHashMap.put(datum.getName(), datum);
 }
 return objectObjectHashMap;
 }

 */
/** 预估实现信息备注
 * @description:
 * @author: haijun.ren
 * @date: 2024/7/26 14:14
 * @param: null
 * @return:
 ***


private String getInfoRemarks(TonesRr tonesRr){
StringBuilder estimatedimplementationinformationremarks = new StringBuilder();
if (StringUtils.isNotEmpty(tonesRr.getYujidaoyuxiangmu())) {
estimatedimplementationinformationremarks.append(tonesRr.getYujidaoyuxiangmu()).append(";");
}
if (StringUtils.isNotEmpty(tonesRr.getYujifabuosbanben())) {
estimatedimplementationinformationremarks.append(tonesRr.getYujifabuosbanben()).append(";");
}
if (StringUtils.isNotEmpty(tonesRr.getYujifabubanbenhao())) {
estimatedimplementationinformationremarks.append(tonesRr.getYujifabubanbenhao()).append(";");
}
if (estimatedimplementationinformationremarks.length() > 0) {
estimatedimplementationinformationremarks = estimatedimplementationinformationremarks.deleteCharAt(estimatedimplementationinformationremarks.length()-1);
}
return estimatedimplementationinformationremarks.toString();
}
public void initFiles(JSONObject params){
List<TonesRrFile> list;
if (params.containsKey("tonesIds")){
List tonesIds = (List)params.get("tonesIds");
list = tonesRrFileService.list(Wrappers.<TonesRrFile>lambdaQuery().in(TonesRrFile::getTonesId, tonesIds));
}else if (params.containsKey("startTonesId") && params.containsKey("endTonesId")){
String startTonesId = params.getString("startTonesId");
String endTonesId = params.getString("endTonesId");
list = tonesRrFileService.list(Wrappers.<TonesRrFile>lambdaQuery().between(TonesRrFile::getTonesId, startTonesId, endTonesId));
}else {
list = tonesRrFileService.list();
}
if(CollectionUtils.isEmpty(list)){
return;
}
Map<String,List<TonesRrFile>> map = list.stream().collect(Collectors.groupingBy(TonesRrFile::getTonesId));
ThreadPoolExecutor executor = new ThreadPoolExecutor(
15,
60,
120,
TimeUnit.SECONDS,
new ArrayBlockingQueue<>(2048),
Executors.defaultThreadFactory(),
new ThreadPoolExecutor.CallerRunsPolicy());
List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();

for(Map.Entry<String,List<TonesRrFile>> entry:map.entrySet()) {
completableFutures.add(CompletableFuture.runAsync(() -> {
List<FileVO> fileVOS = new ArrayList<>();
List<TonesRrFile> files = entry.getValue();
if (CollectionUtils.isNotEmpty(files)) {
for (TonesRrFile file : files) {
if(StringUtils.isEmpty(file.getFileName()) || file.getFileName().endsWith(".webp")){
continue;
}
FileVO fileVO = new FileVO();
fileVO.setUrl("https:api-tones-hk.transsion.com/tones-component/tones-component/file/getFiles?id="+ URLEncoder.encode(file.getFileName()));
fileVO.setName(file.getName());
fileVOS.add(fileVO);
}
TonesRr tonesRr = new TonesRr();
tonesRr.setTonesId(entry.getKey());
List<FileVO> result = null;
Boolean fileResult = false;
try {
if (CollectionUtils.isNotEmpty(fileVOS)) {
获取文件是否是否需要登录认证
boolean isAuth = fileVOS.stream().anyMatch(v->{
String filePre = v.getUrl().split("\\.")[v.getUrl().split("\\.").length-1];
return !AUTH_FILE_TYPE.contains(filePre);
});
if (isAuth) {
result = getAuthFile(fileVOS);
} else {
result = FileUtils.uploadFile(fileVOS);
}
}
fileResult = true;
}catch (Exception e){
log.info("上传文件失败,{}",entry.getKey());
}finally {
tonesRrService.update(Wrappers.<TonesRr>lambdaUpdate().set(TonesRr::getFiles,result).set(TonesRr::getFileResult,fileResult).eq(TonesRr::getTonesId,entry.getKey()));
}
}
}, executor));
}
CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
log.error("多线程同步Tones需求异常", ex);
throw new TranscendBizException("多线程同步Tones需求异常");
}).join();
}

public void initDescFiles(){
final List<TonesRr> list = tonesRrService.list(Wrappers.<TonesRr>lambdaQuery().like(TonesRr::getDemandDesc,"<img"));
if(CollectionUtils.isEmpty(list)){
return;
}
ThreadPoolExecutor executor = new ThreadPoolExecutor(
15,
60,
120,
TimeUnit.SECONDS,
new ArrayBlockingQueue<>(2048),
Executors.defaultThreadFactory(),
new ThreadPoolExecutor.CallerRunsPolicy());
List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
for(TonesRr rr:list) {
completableFutures.add(CompletableFuture.runAsync(() -> {
List<FileVO> fileVOS =parseContent(rr.getDemandDesc());
Map<String,String> nameUrl = fileVOS.stream().collect(Collectors.toMap(FileVO::getName, FileVO::getUrl));
try {
List<FileVO> result = FileUtils.uploadFile(fileVOS);
result.forEach(fileVO -> {
String url = nameUrl.get(fileVO.getName());
rr.setDemandDesc(rr.getDemandDesc().replace(url,fileVO.getUrl()));
});
rr.setDescResult("success");
}catch (Exception e){
rr.setDescResult("error");
log.info("上传文件失败,{}",rr.getTonesId());
}finally {
tonesRrService.updateById(rr);
}
}, executor));
}
CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
log.error("多线程同步Tones需求异常", ex);
throw new TranscendBizException("多线程同步Tones需求异常");
}).join();
}
private List<FileVO> parseContent(String content){
List<FileVO> fileDTOList = new ArrayList<>();
Pattern pattern = Pattern.compile("<img.*?src\\s*=\\s*['\"](.*?)['\"][^>]*>");
Matcher matcher = pattern.matcher(content);
while (matcher.find()) {
FileVO fileDTO = new FileVO();
fileDTO.setUrl(matcher.group(1));
fileDTO.setName(fileDTO.getUrl().substring(fileDTO.getUrl().lastIndexOf("=")+1));
fileDTOList.add(fileDTO);
}
return fileDTOList;
}

private LocalDateTime asLocalDate(Date date) {
if (date == null) {
return null;
}
return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
}

 */
/**
 * @description: 获取品牌
 * @author: haijun.ren
 * @date: 2024/7/31 18:30
 * @param: null
 * @return:
 ***


private String getBrand(String code, String name, Map<String, Map<String, String>> transcendRrDictMap,Set<String> errorMsg){
if (StringUtils.isEmpty(name)){
return "company_all_brand";
}
switch (name){
case "Syinix":
case "其他":
name = "Infinix";
}
Map<String, String> map = transcendRrDictMap.get(code);
CaseInsensitiveMap<String, String> insensitiveMap = new CaseInsensitiveMap<>(map);
if (!insensitiveMap.containsKey(name)) {
errorMsg.add(String.format(ERROR_MESSAGE,code,name));
}
return insensitiveMap.get(name);
}

 */
    /**
     * 获取产品线
     *
     * @description:
     * @author: haijun.ren
     * @date: 2024/7/31 18:31
     * @param: null
     * @return: **
     * <p>
     * <p>
     * private String getProductLine(String code, String name, Map<String, Map<String, String>> transcendRrDictMap,Set<String> errorMsg){
     * if (StringUtils.isEmpty(name)){
     * return "company_all_series";
     * }
     * name = name.trim();
     * Map<String, String> map = transcendRrDictMap.get(code);
     * CaseInsensitiveMap<String, String> insensitiveMap = new CaseInsensitiveMap<>(map);
     * String[] split = name.split("[,/]");
     * String productLineName = split[0];
     * switch (productLineName){
     * case "ASP系列":
     * case "A系列":
     * productLineName = "A";
     * break;
     * case "ALL":
     * case "ITEL":
     * case "HiOS":
     * case "XPAD":
     * case "Basic":
     * case "MEGAPAD":
     * productLineName = "我司全系产品线";
     * break;
     * case "Power":
     * case "Pova":
     * productLineName = "POVA";
     * break;
     * case "S665LN":
     * productLineName = "S";
     * break;
     * }
     * if (!insensitiveMap.containsKey(productLineName)) {
     * errorMsg.add(String.format(ERROR_MESSAGE,code,productLineName));
     * }
     * return insensitiveMap.get(productLineName);
     * }
     * <p>
     * private List<MObject> getRrList(){
     * com.transcend.plm.datadriven.api.model.QueryWrapper qo = new com.transcend.plm.datadriven.api.model.QueryWrapper();
     * qo.eq("deleteFlag", 0);
     * QueryCondition queryCondition = new QueryCondition();
     * List<com.transcend.plm.datadriven.api.model.QueryWrapper> queryWrappers = com.transcend.plm.datadriven.api.model.QueryWrapper.buildSqlQo(qo);
     * QueryWrapper qo2 = new QueryWrapper();
     * qo2.eq("spaceBid", syncTonesRRProperties.getSpaceBid());
     * QueryWrapper qo3 = new QueryWrapper();
     * 领域维护应用BID
     * qo3.in("spaceAppBid", syncTonesRRProperties.getRrSpaceAppBid());
     * queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo2));
     * queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo3));
     * queryCondition.setQueries(queryWrappers);
     * BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
     * pageQo.setParam(queryCondition);
     * pageQo.setCurrent(1);
     * pageQo.setSize(1);
     * PagedResult<MObject> page = objectModelCrudI.page("A5E",pageQo, true);
     * if (page == null || page.getData().isEmpty()) {
     * return new ArrayList<>();
     * }
     * long total = page.getTotal();
     * int pageSize = 10000;
     * int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
     * List<MObject> result = new ArrayList<>();
     * for (int i = 1; i <= totalPage; i++) {
     * pageQo.setCurrent(i);
     * pageQo.setSize(pageSize);
     * page = objectModelCrudI.page("A5E",pageQo, true);
     * List<MObject> mObjectList = page.getData();
     * if (CollectionUtils.isNotEmpty(mObjectList)) {
     * result.addAll(mObjectList);
     * }
     * }
     * return result;
     * }
     * <p>
     * private  List<FileVO> getAuthFile(List<FileVO> fileVo){
     * IpmProjectFeignClient ipmProjectFeignClient = SpringBeanHelper.getBean(IpmProjectFeignClient.class);
     * if(CollectionUtils.isEmpty(fileVo)) {
     * return null;
     * }
     * MultipartFile[] multipartFiles = new MultipartFile[fileVo.size()];
     * for (int i = 0; i < fileVo.size(); i++) {
     * FileVO file= fileVo.get(i);
     * String url = file.getUrl();
     * HttpRequest get = HttpUtil.createGet(url);
     * 设置请求头，按照业务需求设置即可
     * get.header("p-rtoken", syncTonesRRProperties.getProdUtoken());
     * get.header("p-auth", syncTonesRRProperties.getProdRtoken());
     * 发出请求，返回数据
     * HttpResponse execute = get.execute();
     * byte[] bytes = execute.bodyBytes();
     * multipartFiles[i] =  new MockMultipartFile(file.getName(),file.getName(),"multipart/form-data",bytes);
     * }
     * 发起请求
     * log.info("上传文件开始------------------入参:{}", JSON.toJSONString(JSON.toJSONString(fileVo)));
     * ResponseVO<List<FileVO>> response =  ipmProjectFeignClient.uploadFiles(multipartFiles);
     * if("200".equals(response.getCode())) {
     * return response.getData();
     * }else {
     * log.info("上传文件失败，响应信息:{}", JSON.toJSONString(response));
     * throw new PlmBizException(response.getMessage());
     * }
     * }
     * }
     */


    public Boolean syncData(JSONObject params) throws Exception {
        //查询导入数据
        List<TonesIr> irTonesList = irService.list();
        List<TonesSr> srTonesList = srService.list();
        List<TonesAr> arTonesList = arService.list();
        for (TonesIr ir : irTonesList) {
            ir.setOldCode(ir.getOldCode().trim());
        }
        for (TonesSr sr : srTonesList) {
            sr.setOldCode(sr.getOldCode().trim());
            sr.setIrOldCode(sr.getIrOldCode().trim());

        }
        for (TonesAr ar : arTonesList) {
            ar.setOldCode(ar.getOldCode().trim());
            ar.setSrOldCode(ar.getSrOldCode().trim());
        }
        //领域名称
        Map<String, MObject> domainNameMap = getDomainNameMap();
        //模块名称
        Map<String, MObject> moduleNameMap = getModuleNameMap();
        //责任田名称
        Map<String, MObject> dutyFieldMap = getDutyFieldMap();
        //研发模块名称
        Map<String, MObject> developModuleMap = getDevelopModuleMap();

        //组装ir,sr,ar,ir-sr关系,sr-ar关系实例数据
        List<MSpaceAppData> irDataList = new ArrayList<>();
        List<MSpaceAppData> srDataList = new ArrayList<>();
        List<MSpaceAppData> arDataList = new ArrayList<>();
        List<MObject> irSrDataList = Lists.newArrayList();
        List<MObject> srArDataList = Lists.newArrayList();
        //ir,sr原始编码与实例Bid映射
        Map<String, String> irCodeBidMap = new HashMap<>(64);
        Map<String, String> srCodeBidMap = new HashMap<>(64);
        //ir-sr,sr-ar关系映射
        List<Pair<String, String>> irSrBidMap = new ArrayList<>();
        List<Pair<String, String>> srArBidMap = new ArrayList<>();
        //ir,sr原始编码与transcend编码映射
        Map<String, String> irCodingMap = new HashMap<>(64);
        Map<String, String> srCodingMap = new HashMap<>(64);
        getIr(irCodeBidMap, irCodingMap);
        //sr当前节点审批人
        Map<String, Set<String>> srCurrentApproverMap = new HashMap<>();
        //生成IR编码
        createRrCode(irTonesList);
        buildIrDataList(irTonesList, irDataList, irCodeBidMap, domainNameMap, moduleNameMap, irCodingMap);
        buildSrDataList(srTonesList, srDataList, srCodeBidMap, irCodeBidMap, irSrBidMap, domainNameMap, irCodingMap, srCodingMap, dutyFieldMap, moduleNameMap, srCurrentApproverMap);
        buildArDataList(arTonesList, arDataList, srCodeBidMap, srArBidMap, domainNameMap, srCodingMap, dutyFieldMap, developModuleMap, moduleNameMap, srCurrentApproverMap);
        Long tenantId = SsoHelper.getTenantId();
        buildIrSrDataList(irSrBidMap, irSrDataList, tenantId);
        buildSrArDataList(srArBidMap, srArDataList, tenantId);
        for (MSpaceAppData mSpaceAppData : irDataList) {
            MSpaceAppData add = iBaseApmSpaceAppDataDrivenService.addTonesData(irProperties.getIrSpaceAppBid(), mSpaceAppData);
        }
        for (MSpaceAppData mSpaceAppData : srDataList) {
            MSpaceAppData add = iBaseApmSpaceAppDataDrivenService.addTonesData(irProperties.getSrSpaceAppBid(), mSpaceAppData);
        }
        for (MSpaceAppData mSpaceAppData : arDataList) {
            MSpaceAppData add = iBaseApmSpaceAppDataDrivenService.addTonesData(irProperties.getArSpaceAppBid(), mSpaceAppData);
        }
        appDataService.addBatch(irProperties.getIrSrRelationModelCode(), irSrDataList);
        appDataService.addBatch(irProperties.getSrArRelationModelCode(), srArDataList);
        //设置Sr当前节点审批人
        setCurrenUser(srDataList, srCurrentApproverMap);
        return true;
    }

    private void setCurrenUser(List<MSpaceAppData> srDataList, Map<String, Set<String>> srCurrentApproverMap) {
        Map<String, List<String>> userMap = new HashMap<>();
        Map<String, String> nameMap = new HashMap<>();
        for (MSpaceAppData mSpaceAppData : srDataList) {
            if (!ObjectUtils.isEmpty(srCurrentApproverMap.get(mSpaceAppData.getBid()))) {
                userMap.put(mSpaceAppData.getBid(), new ArrayList<>(srCurrentApproverMap.get(mSpaceAppData.getBid())));
                nameMap.put(mSpaceAppData.getBid(), mSpaceAppData.getName());
            }
        }
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listActiveNodesByInstanceAndNode(new ArrayList<>(userMap.keySet()), irProperties.getSrTempNodeBid());
        for (ApmFlowInstanceNode apmFlowInstanceNode : apmFlowInstanceNodes) {
            String roleBid = apmFlowInstanceNode.getNodeRoleBids().get(0);
            List<String> users = userMap.get(apmFlowInstanceNode.getInstanceBid());
            if (CollectionUtils.isNotEmpty(users)) {
                List<ApmUser> userVoList = users.stream().map(v -> ApmUser.builder().empNo(v).build()).collect(Collectors.toList());
                ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
                apmRoleUserAO.setUserList(userVoList);
                apmRoleUserAO.setRoleBid(roleBid);
                ApmFlowQo apmFlowQo = new ApmFlowQo();
                apmFlowQo.setSpaceBid(irProperties.getSpaceBid());
                apmFlowQo.setSpaceAppBid(irProperties.getSrSpaceAppBid());
                apmFlowQo.setRoleUserList(Lists.newArrayList(apmRoleUserAO));
                runtimeService.updateRoleUser(apmFlowInstanceNode.getInstanceBid(), apmFlowQo);
                notify2(apmFlowInstanceNode.getInstanceBid(), users, nameMap.get(apmFlowInstanceNode.getInstanceBid()));
            }
        }
    }

    public void addRoleUser(){
        List<TonesSr> srTonesList = srService.list();
         Map<String, Set<String>> roleUserMap = new HashMap<>();
        srTonesList.forEach(tonesSr -> {
            if (StringUtils.isNotBlank(tonesSr.getOldCode()) && !StrUtil.isAllEmpty(tonesSr.getTitle(),tonesSr.getProductRequirementDescription())){
                Set<String> users = new HashSet<>();
                if (StringUtils.isNotBlank(tonesSr.getTitle())){
                    users.addAll(Arrays.asList(tonesSr.getTitle().trim().split(",")));
                }
                if (StringUtils.isNotBlank(tonesSr.getProductRequirementDescription())){
                    users.addAll(Arrays.asList(tonesSr.getProductRequirementDescription().trim().split(",")));
                }
                if (!users.isEmpty()){
                    if (roleUserMap.containsKey(tonesSr.getOldCode())){
                        roleUserMap.get(tonesSr.getOldCode()).addAll(users);
                    } else {
                        roleUserMap.put(tonesSr.getOldCode(), users);
                    }
                }

            }
        });
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in("tones_id", roleUserMap.keySet());
        QueryCondition queryCondition = new QueryCondition();
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
        queryCondition.setQueries(queryWrappers);
        List<MObject> list = objectModelCrudI.list("A5F", queryCondition);
        Map<String, String> irBidCodeMap = new HashMap<>();
        for (MObject datum : list) {
            if (StrUtil.isAllNotBlank(datum.getBid()) && !ObjectUtils.isEmpty( datum.get("tonesId"))){
                irBidCodeMap.put(datum.getBid(), datum.get("tonesId").toString());
            }
        }
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listActiveNodesByInstanceAndNode(new ArrayList<>(irBidCodeMap.keySet()), irProperties.getSrTempNodeBid());

        List<String> nodeRoleBids = apmFlowInstanceNodes.stream().filter(v->CollectionUtils.isNotEmpty(v.getNodeRoleBids())).map(v -> v.getNodeRoleBids().get(0)).collect(Collectors.toList());
        List<ApmFlowInstanceRoleUser>  roleUserList = apmFlowInstanceRoleUserService.list(Wrappers.<ApmFlowInstanceRoleUser>lambdaQuery().in(ApmFlowInstanceRoleUser::getInstanceBid, irBidCodeMap.keySet())
                .in(ApmFlowInstanceRoleUser::getRoleBid, nodeRoleBids));
        Map<String, List<ApmFlowInstanceRoleUser>> roleGroup = roleUserList.stream().collect(Collectors.groupingBy(ApmFlowInstanceRoleUser::getInstanceBid));
        roleGroup.forEach((instanceBid, roleUsers) -> {
            Set<String> userNoSet = roleUsers.stream().map(ApmFlowInstanceRoleUser::getUserNo).collect(Collectors.toSet());
            String roleBid = roleUsers.get(0).getRoleBid();
            if (irBidCodeMap.containsKey(instanceBid) && CollectionUtils.isNotEmpty(roleUserMap.get(irBidCodeMap.get(instanceBid)))) {
                userNoSet.addAll(roleUserMap.get(irBidCodeMap.get(instanceBid)));
                List<ApmUser> userVoList = userNoSet.stream().map(v -> ApmUser.builder().empNo(v).build()).collect(Collectors.toList());
                ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
                apmRoleUserAO.setUserList(userVoList);
                apmRoleUserAO.setRoleBid(roleBid);
                ApmFlowQo apmFlowQo = new ApmFlowQo();
                apmFlowQo.setSpaceBid(irProperties.getSpaceBid());
                apmFlowQo.setSpaceAppBid(irProperties.getSrSpaceAppBid());
                apmFlowQo.setRoleUserList(Lists.newArrayList(apmRoleUserAO));
                runtimeService.updateRoleUser(instanceBid, apmFlowQo);
            }
        });
    }

    private void buildSrArDataList(List<Pair<String, String>> srArBidMap, List<MObject> srArataList, Long tenantId) {
        srArBidMap.forEach((pair) -> {
            MObject relationObject = new MObject();
            relationObject.put(TranscendModelBaseFields.BID, com.transcend.plm.datadriven.common.util.SnowflakeIdWorker.nextIdStr());
            relationObject.put(RelationEnum.DATA_BID.getCode(), com.transcend.plm.datadriven.common.util.SnowflakeIdWorker.nextIdStr());
            relationObject.put(RelationEnum.SOURCE_BID.getCode(), pair.getKey());
            relationObject.put(RelationEnum.SOURCE_DATA_BID.getCode(), pair.getKey());
            relationObject.put(RelationEnum.TARGET_BID.getCode(), pair.getValue());
            relationObject.put(RelationEnum.TARGET_DATA_BID.getCode(), pair.getValue());
            relationObject.put(RelationEnum.DRAFT.getCode(), Boolean.FALSE);
            relationObject.put(RelationEnum.MODEL_CODE.getCode(), irProperties.getSrArRelationModelCode());
            relationObject.put(BaseDataEnum.ENABLE_FLAG.getCode(), Boolean.TRUE);;
            relationObject.put(RelationEnum.SPACE_BID.getCode(), irProperties.getSpaceBid());
            relationObject.put(RelationEnum.SPACE_APP_BID.getCode(), 0);
            relationObject.setTenantId(tenantId);
            srArataList.add(relationObject);
        });
    }

    private void buildIrSrDataList(List<Pair<String, String>>  irSrBidMap, List<MObject> irSrDataList, Long tenantId) {
        irSrBidMap.forEach((pair) -> {
            MObject relationObject = new MObject();
            relationObject.put(TranscendModelBaseFields.BID, com.transcend.plm.datadriven.common.util.SnowflakeIdWorker.nextIdStr());
            relationObject.put(RelationEnum.DATA_BID.getCode(), com.transcend.plm.datadriven.common.util.SnowflakeIdWorker.nextIdStr());
            relationObject.put(RelationEnum.SOURCE_BID.getCode(), pair.getKey());
            relationObject.put(RelationEnum.SOURCE_DATA_BID.getCode(), pair.getKey());
            relationObject.put(RelationEnum.TARGET_BID.getCode(), pair.getValue());
            relationObject.put(RelationEnum.TARGET_DATA_BID.getCode(), pair.getValue());
            relationObject.put(RelationEnum.DRAFT.getCode(), Boolean.FALSE);
            relationObject.put(BaseDataEnum.ENABLE_FLAG.getCode(), Boolean.TRUE);
            relationObject.put(RelationEnum.MODEL_CODE.getCode(), irProperties.getIrSrRelationModelCode());
            relationObject.put(RelationEnum.SPACE_BID.getCode(), irProperties.getSpaceBid());
            relationObject.put(RelationEnum.SPACE_APP_BID.getCode(), 0);
            relationObject.setTenantId(tenantId);
            irSrDataList.add(relationObject);
        });
    }

    private void buildArDataList(List<TonesAr> arTonesList, List<MSpaceAppData> arDataList, Map<String, String> srCodeBidMap, List<Pair<String, String>> srArBidMap,
                                 Map<String, MObject> domainNameMap, Map<String, String> srCodingMap, Map<String, MObject> dutyFieldMap, Map<String, MObject> developModuleMap,
                                 Map<String, MObject> moduleNameMap, Map<String, Set<String>> srCurrentApproverMap) {
        for (TonesAr tonesAr : arTonesList) {
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            String bid = SnowflakeIdWorker.nextIdStr();
            mSpaceAppData.setBid(bid);
            mSpaceAppData.put("dataBid", bid);
            mSpaceAppData.put("tonesId", tonesAr.getOldCode());
            mSpaceAppData.put("historicalDataIdentification", "1");
            mSpaceAppData.put("workItemType",irProperties.getArWorkItemType());
            mSpaceAppData.put("lcTemplBid",irProperties.getArLcTemplBid());
            mSpaceAppData.put("lcTemplVersion",irProperties.getArLcTemplVersion());
            //编码
            String srCoding = srCodingMap.get(tonesAr.getSrOldCode());
            String arCoding = srCoding.replace("SR", "AR") +  tonesAr.getOldCode().substring( tonesAr.getOldCode().length() - 4);
            mSpaceAppData.put("requirementcoding",arCoding);
            //标题
            mSpaceAppData.setName(tonesAr.getTitle());
            //需求描述
            mSpaceAppData.put("productRequirementDescription", tonesAr.getProductRequirementDescription());
            //子领域
            if (StringUtils.isNotBlank(tonesAr.getChildDomain()) && domainNameMap.containsKey(tonesAr.getChildDomain())) {
                mSpaceAppData.put("productArea", domainNameMap.get(tonesAr.getChildDomain()).getBid());
            }
            if (StringUtils.isNotBlank(tonesAr.getModule()) && moduleNameMap.containsKey(tonesAr.getModule())) {
                mSpaceAppData.put("module", moduleNameMap.get(tonesAr.getModule()).getBid());
            }
            //创建人
            String createdBy = getJobNum(tonesAr.getCreatedBy());
            if (StringUtils.isBlank(createdBy)) {
                createdBy = "18651509";
            }
            mSpaceAppData.put("createdBy", createdBy);
            //创建时间
            mSpaceAppData.setCreatedTime(asLocalDate(tonesAr.getCreatedTime()));
           /* //责任人
            if (StringUtils.isNotBlank(tonesAr.getPersonResponsible())) {
            }*/
            if (StringUtils.isNotBlank(tonesAr.getPersonResponsible()) && srCodeBidMap.containsKey(tonesAr.getSrOldCode())) {
                String srBid = srCodeBidMap.get(tonesAr.getSrOldCode());
                mSpaceAppData.put("atResponsiblePerson", Arrays.asList(tonesAr.getPersonResponsible().split(",")));
                if (srCurrentApproverMap.containsKey(srBid)) {
                    srCurrentApproverMap.get(srBid).addAll(Arrays.asList(tonesAr.getPersonResponsible().split(",")));
                }else {
                    srCurrentApproverMap.put(srBid, new HashSet<>(Arrays.asList(tonesAr.getPersonResponsible().split(","))));
                }
                //mSpaceAppData.put("personResponsible", Arrays.asList(tonesAr.getPersonResponsible().split(",")));
            }

            //责任田
            if (StringUtils.isNotBlank(tonesAr.getResponsibilityField()) && dutyFieldMap.containsKey(tonesAr.getResponsibilityField())) {
                MObject mObject = dutyFieldMap.get(tonesAr.getResponsibilityField());
                mSpaceAppData.put("responsibilityField",mObject.getBid());
                mSpaceAppData.put("personResponsible",mObject.get("personResponsible"));
                mSpaceAppData.put("deputy",mObject.get("deputy"));
            }
            //研发模块
            if (StringUtils.isNotBlank(tonesAr.getRdModule()) && developModuleMap.containsKey(tonesAr.getRdModule())) {
                MObject mObject = developModuleMap.get(tonesAr.getRdModule());
                mSpaceAppData.put("rAndDModuleInformation",mObject.getBid());
                mSpaceAppData.put("informationOfRAndDModuleResponsiblePerson",mObject.get("informationOfRAndDModuleResponsiblePerson"));
            }
            //预估研发时间
            mSpaceAppData.put("estimatedrdworkload", tonesAr.getEstimatedrdworkload());
            arDataList.add(mSpaceAppData);
            if (StringUtils.isNotBlank(tonesAr.getSrOldCode())) {
                srArBidMap.add(new Pair<>(srCodeBidMap.get(tonesAr.getSrOldCode()), bid));
            }
        }
    }

    private void buildSrDataList(List<TonesSr> srTonesList, List<MSpaceAppData> srDataList, Map<String, String> srCodeBidMap,
                                 Map<String, String> irCodeBidMap, List<Pair<String, String>> irSrBidMap, Map<String, MObject> domainNameMap,
                                 Map<String, String> irCodingMap, Map<String, String> srCodingMap, Map<String, MObject> dutyFieldMap,
                                 Map<String, MObject> moduleNameMap, Map<String, Set<String>> srCurrentApproverMap) {
        for (TonesSr tonesSr : srTonesList) {
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            String bid = SnowflakeIdWorker.nextIdStr();
            mSpaceAppData.setBid(bid);
            mSpaceAppData.put("dataBid", bid);
            mSpaceAppData.put("tonesId", tonesSr.getOldCode());
            mSpaceAppData.put("historicalDataIdentification", "1");
            mSpaceAppData.put("workItemType",irProperties.getSrWorkItemType());
            mSpaceAppData.put("lcTemplBid",irProperties.getSrLcTemplBid());
            mSpaceAppData.put("lcTemplVersion",irProperties.getSrLcTemplVersion());
            //编码
            String irCoding = irCodingMap.get(tonesSr.getIrOldCode());
            String srCoding = irCoding.replace("IR", "SR") +  tonesSr.getOldCode().substring( tonesSr.getOldCode().length() - 4);
            srCodingMap.put(tonesSr.getOldCode(), srCoding);
            mSpaceAppData.put("requirementcoding",srCoding);
            //标题
            mSpaceAppData.setName(tonesSr.getTitle());
            //需求来源
            mSpaceAppData.put("productRequirementDescription", tonesSr.getProductRequirementDescription());
            //项目差异化说明
            mSpaceAppData.put("projectDifferentiationExplanation", tonesSr.getProjectDifferentiationExplanation());
            //领域
            //List<String> domainNames = new ArrayList<>();
            if (StringUtils.isNotBlank(tonesSr.getChildDomain()) && domainNameMap.containsKey(tonesSr.getChildDomain())) {
                //domainNames.add(tonesSr.getChildDomain());
                mSpaceAppData.put("productArea", domainNameMap.get(tonesSr.getChildDomain()).getBid());
            }
           /*if (!domainNames.isEmpty()) {
                SelectAo selectAo = new SelectAo();
                List<List<String>> selectedList = new ArrayList<>();
                List<SelectVo> nameList = new ArrayList<>();
                for (String domainName : domainNames) {
                    MObject mObject = domainNameMap.get(domainName);
                    if (mObject == null) {
                        continue;
                    }
                    List<String> bidList = new ArrayList<>();
                    bidList.add(mObject.getBid());
                    selectedList.add(bidList);
                    SelectVo selectVo = new SelectVo();
                    selectVo.setName(domainName);
                    Object productManager = mObject.get("productManager");
                    if (!ObjectUtils.isEmpty(productManager) && productManager instanceof java.util.List) {
                        selectVo.setDomainLeader(((List<?>) productManager).get(0).toString());
                    }
                    selectVo.setBid(mObject.getBid());
                    selectVo.setDisabled(false);
                    selectVo.setModelCode("A42");
                    selectVo.setSpaceAppBid(irProperties.getProductSpaceBid());
                    nameList.add(selectVo);
                }
                selectAo.setSelectedList(selectedList);
                selectAo.setNameList(nameList);
                demandManagementService.selectDomain(irProperties.getSpaceBid(), irProperties.getIrSpaceAppBid(), bid, 2, selectAo, null, null);
            }*/
            //模块
            if (StringUtils.isNotBlank(tonesSr.getMoudule()) && moduleNameMap.containsKey(tonesSr.getMoudule())) {
                mSpaceAppData.put("module", moduleNameMap.get(tonesSr.getMoudule()).getBid());
            }
            //创建人
            String createdBy = getJobNum(tonesSr.getCreatedBy());
            if (StringUtils.isBlank(createdBy)) {
                createdBy = "18651509";
            }
            mSpaceAppData.put("createdBy", createdBy);
            //创建时间
            mSpaceAppData.setCreatedTime(asLocalDate(tonesSr.getCreatedTime()));
            //责任人
            if (StringUtils.isNotBlank(tonesSr.getPersonResponsible())) {
                if (srCurrentApproverMap.containsKey(bid)) {
                    srCurrentApproverMap.get(bid).addAll(Arrays.asList(tonesSr.getPersonResponsible().split(",")));
                }else {
                    srCurrentApproverMap.put(bid, new HashSet<>(Arrays.asList(tonesSr.getPersonResponsible().split(","))));
                }
                mSpaceAppData.put("currPersonResponsible", Arrays.asList(tonesSr.getPersonResponsible().split(",")));
            }
            //责任田,SR责任田是多选
            if (StringUtils.isNotBlank(tonesSr.getResponsibilityField())) {
                List<String> bids = new ArrayList<>();
                List<Object> personResponsibleList = new ArrayList<>();
                List<Object> deputyList = new ArrayList<>();
                for (String name : tonesSr.getResponsibilityField().split(",")) {
                    if (dutyFieldMap.containsKey(name.trim())) {
                        MObject mObject = dutyFieldMap.get(name.trim());
                        bids.add(mObject.getBid());
                        if (!ObjectUtils.isEmpty(mObject.get("personResponsible"))){
                            personResponsibleList.add(mObject.get("personResponsible"));
                        }
                        if (!ObjectUtils.isEmpty(mObject.get("deputy"))){
                            deputyList.add(mObject.get("deputy"));
                        }
                    }
                }
                mSpaceAppData.put("responsibilityField",bids);
                mSpaceAppData.put("personResponsible",personResponsibleList);
                mSpaceAppData.put("deputy",deputyList);
            }
            //预估研发时间
            mSpaceAppData.put("estimatedrdworkload", tonesSr.getEstimatedTestingWorkload());
            srDataList.add(mSpaceAppData);
            if (StringUtils.isNotBlank(tonesSr.getIrOldCode())) {
                irSrBidMap.add(new Pair<>(irCodeBidMap.get(tonesSr.getIrOldCode()), bid));
            }
            srCodeBidMap.put(tonesSr.getOldCode(), bid);
        }


    }

    private void buildIrDataList(List<TonesIr> irTonesList, List<MSpaceAppData> irDataList, Map<String, String> irCodeBidMap, Map<String, MObject> domainNameMap, Map<String, MObject> moduleNameMap ,Map<String, String> irCodingMap) {
        for (TonesIr tonesIr : irTonesList) {
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            String bid = SnowflakeIdWorker.nextIdStr();
            mSpaceAppData.setBid(bid);
            mSpaceAppData.put("dataBid", bid);
            mSpaceAppData.put("tonesId", tonesIr.getOldCode());
            mSpaceAppData.put("historicalDataIdentification", "1");
            mSpaceAppData.put("workItemType",irProperties.getIrWorkItemType());
            mSpaceAppData.put("lcTemplBid",irProperties.getIrLcTemplBid());
            mSpaceAppData.put("lcTemplVersion",irProperties.getIrLcTemplVersion());
            mSpaceAppData.put("targetOSversion",irProperties.getIrOsVersion());
            //编码
            mSpaceAppData.put("requirementcoding",tonesIr.getRequirementcoding());
            irCodingMap.put(tonesIr.getOldCode(), tonesIr.getRequirementcoding());
            //标题
            mSpaceAppData.setName(tonesIr.getTitle());
            mSpaceAppData.put("requirementDescription", tonesIr.getRequirementDescription());
            //需求来源
            if (StringUtils.isNotBlank(tonesIr.getProductLine())) {
                mSpaceAppData.put("demandOrigin", Collections.singleton("soft"));
            }
            //来源产品线
            if (StringUtils.isNotBlank(tonesIr.getProductLine())) {
                mSpaceAppData.put("productLine", JSONObject.toJSONString(Collections.singleton(tonesIr.getProductLine())));
            }
            //关键价值
            mSpaceAppData.put("valueDirection", tonesIr.getValueDirection());
            //优先级
            mSpaceAppData.put("demandPriority", tonesIr.getDemandPriority());
            //体验主张
            mSpaceAppData.put("experienceProposition", tonesIr.getExperienceProposition());
            //领域
            List<String> domainNames = new ArrayList<>();
            /*if (StringUtils.isNotBlank(tonesIr.getProductArea()) && domainNameMap.containsKey(tonesIr.getProductArea())) {
                domainNames.add(tonesIr.getProductArea());
            }*/
            if (StringUtils.isNotBlank(tonesIr.getChildProductArea()) && domainNameMap.containsKey(tonesIr.getChildProductArea())) {
                domainNames.add(tonesIr.getChildProductArea());
                String module = tonesIr.getModule().split(",")[0];
                if (!moduleNameMap.containsKey(module)){
                    throw new RuntimeException("模块不存在:{"+module+"}");
                }
                domainNames.add(module);
                mSpaceAppData.put("domainList",domainNameMap.get(tonesIr.getChildProductArea()).getBid()+"-"+moduleNameMap.get(module).getBid());

            }else {
                throw new RuntimeException("领域不存在:{"+tonesIr.getChildProductArea()+"}");
            }
            if (!domainNames.isEmpty()) {
                SelectAo selectAo = new SelectAo();
                List<List<String>> selectedList = new ArrayList<>();
                List<SelectVo> nameList = new ArrayList<>();
                String domainName = domainNames.get(0);
                MObject mObject = domainNameMap.get(domainName);
                if (mObject == null) {
                    continue;
                }
                List<String> bidList = new ArrayList<>();
                bidList.add(mObject.getBid());

                SelectVo selectVo = new SelectVo();
                selectVo.setName(domainName);
                Object productManager = mObject.get("productManager");
                if (!ObjectUtils.isEmpty(productManager) && productManager instanceof java.util.List) {
                    selectVo.setDomainLeader(((List<?>) productManager).get(0).toString());
                }
                selectVo.setBid(mObject.getBid());
                selectVo.setDisabled(false);
                selectVo.setModelCode(TranscendModel.DOMAIN.getCode());
                selectVo.setSpaceAppBid(irProperties.getProductSpaceBid());

                String moduleName = domainNames.get(1);
                MObject moduleMobject = moduleNameMap.get(moduleName);
                if (moduleMobject == null) {
                    continue;
                }
                bidList.add(moduleMobject.getBid());

                selectedList.add(bidList);
                SelectVo child = new SelectVo();
                child.setName(moduleName);
                Object domainResponsibleObj = moduleMobject.get(DemandManagementEnum.MODULE_RESPONSIBLE.getCode());
                // 责任人多个取第一个
                if(domainResponsibleObj != null){
                    if(domainResponsibleObj instanceof List){
                        List<String> values = JSON.parseArray(JSON.toJSONString(domainResponsibleObj), String.class);
                        if(CollectionUtils.isNotEmpty(values)){
                            child.setDomainLeader(values.get(0));
                        }
                    }else if(domainResponsibleObj instanceof String){
                        String person = domainResponsibleObj.toString();
                        child.setDomainLeader( person);
                    }
                }
                child.setBid(moduleMobject.getBid());
                child.setDisabled(false);
                child.setModelCode(TranscendModel.MODULE.getCode());
                child.setSpaceAppBid(irProperties.getModuleSpaceBid());
                selectVo.setChildren(Collections.singletonList(child));
                nameList.add(selectVo);
                selectAo.setSelectedList(selectedList);
                selectAo.setNameList(nameList);
                demandManagementService.selectDomain(irProperties.getSpaceBid(), irProperties.getIrSpaceAppBid(), bid, 2, selectAo, null, null);
            }
            //创建人
            String createdBy = getJobNum(tonesIr.getCreatedBy());
            if (StringUtils.isBlank(createdBy)) {
                createdBy = "18651509";
            }
            mSpaceAppData.put("createdBy", createdBy);
            //创建时间
            mSpaceAppData.setCreatedTime(asLocalDate(tonesIr.getCreatedTime()));
            //责任人
            if (StringUtils.isNotBlank(tonesIr.getPersonResponsible())) {
                mSpaceAppData.put("personResponsible", Arrays.asList(tonesIr.getPersonResponsible().split(",")));
            }
            //附件
            if (StringUtils.isNotBlank(tonesIr.getFiles())) {
                List<Map<String, Object>> files = new ArrayList<>();
                for (String url : tonesIr.getFiles().split(",")) {
                    Map<String, Object> file = new HashMap<>();
                    file.put("name", url);
                    file.put("status", "success");
                    file.put("type", "link");
                    file.put("url", url);
                    file.put("uid", Long.toString(System.currentTimeMillis()));
                    files.add(file);
                }
                mSpaceAppData.put("files", files);
            }
            irDataList.add(mSpaceAppData);
            irCodeBidMap.put(tonesIr.getOldCode(), bid);
        }
    }

    private String getJobNum(String user) {
        if (StringUtils.isEmpty(user)) {
            return "";
        }
        user = user.replace(" ", "");
        List<String> strList = new ArrayList<>();
        Matcher matcher = JOBNUM_PATTERN.matcher(user);
        while (matcher.find()) {
            strList.add(matcher.group());
        }
        if (strList.isEmpty()) {
            strList.add(user);
        }
        return strList.get(0);
    }

    private LocalDateTime asLocalDate(Date date) {
        if (date == null) {
            return LocalDateTime.now();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    //查询所有子领域名称对应bid
    private Map<String, MObject> getDomainNameMap(){
        com.transcend.plm.datadriven.api.model.QueryWrapper qo = new com.transcend.plm.datadriven.api.model.QueryWrapper();
        qo.eq("deleteFlag", 0);
        QueryCondition queryCondition = new QueryCondition();
        List<com.transcend.plm.datadriven.api.model.QueryWrapper> queryWrappers = com.transcend.plm.datadriven.api.model.QueryWrapper.buildSqlQo(qo);
        QueryWrapper qo2 = new QueryWrapper();
        qo2.eq("spaceBid", irProperties.getSpaceBid());
        QueryWrapper qo3 = new QueryWrapper();
        //领域维护应用BID
        qo3.in("spaceAppBid", irProperties.getProductSpaceBid());
        queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo2));
        queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo3));
        queryCondition.setQueries(queryWrappers);
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        pageQo.setParam(queryCondition);
        pageQo.setCurrent(1);
        pageQo.setSize(1000);
        PagedResult<MObject> page = objectModelCrudI.page(TranscendModel.DOMAIN.getCode(),pageQo, true);
        Map<String, MObject> objectObjectHashMap = new HashMap<>();
        for (MObject datum : page.getData()) {
            objectObjectHashMap.put(datum.getName(), datum);
        }
        return objectObjectHashMap;
    }

    //查询所有业务模块
    private Map<String, MObject> getModuleNameMap(){
        com.transcend.plm.datadriven.api.model.QueryWrapper qo = new com.transcend.plm.datadriven.api.model.QueryWrapper();
        qo.eq("deleteFlag", 0);
        QueryCondition queryCondition = new QueryCondition();
        List<com.transcend.plm.datadriven.api.model.QueryWrapper> queryWrappers = com.transcend.plm.datadriven.api.model.QueryWrapper.buildSqlQo(qo);
        QueryWrapper qo2 = new QueryWrapper();
        qo2.eq("spaceBid", irProperties.getSpaceBid());
        QueryWrapper qo3 = new QueryWrapper();
        //领域维护应用BID
        qo3.in("spaceAppBid", irProperties.getModuleSpaceBid());
        queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo2));
        queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers,QueryWrapper.buildSqlQo(qo3));
        queryCondition.setQueries(queryWrappers);
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        pageQo.setParam(queryCondition);
        pageQo.setCurrent(1);
        pageQo.setSize(1000);
        PagedResult<MObject> page = objectModelCrudI.page(TranscendModel.MODULE.getCode(),pageQo, true);
        Map<String, MObject> objectObjectHashMap = new HashMap<>();
        for (MObject datum : page.getData()) {
            objectObjectHashMap.put(datum.getName(), datum);
        }
        return objectObjectHashMap;
    }

    //查询所有责任田信息
    private Map<String, MObject> getDutyFieldMap(){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        QueryCondition queryCondition = new QueryCondition();
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
        queryCondition.setQueries(queryWrappers);
        List<MObject> list = objectModelCrudI.list("A6L", queryCondition);
        Map<String, MObject> objectObjectHashMap = new HashMap<>();
        for (MObject datum : list) {
            objectObjectHashMap.put(datum.getName(), datum);
        }
        return objectObjectHashMap;
    }

    //查询IR信息
    private void getIr(Map<String, String> irCodeBidMap, Map<String, String> irCodingMap){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("historical_data_identification", 1);
        QueryCondition queryCondition = new QueryCondition();
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
        queryCondition.setQueries(queryWrappers);
        List<MObject> list = objectModelCrudI.list("A5G", queryCondition);
        Map<String, String> result = new HashMap<>();
        for (MObject datum : list) {
            if (StrUtil.isAllNotBlank(datum.getBid()) && !ObjectUtils.isEmpty( datum.get("tonesId"))){
                irCodeBidMap.put(datum.get("tonesId").toString(), datum.getBid());
                irCodingMap.put(datum.get("tonesId").toString(), datum.get("requirementcoding").toString());
            }
        }
    }

    //查询所有研发模块信息
    private Map<String, MObject> getDevelopModuleMap(){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        QueryCondition queryCondition = new QueryCondition();
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(queryWrapper);
        queryCondition.setQueries(queryWrappers);
        List<MObject> list = objectModelCrudI.list("A6M", queryCondition);
        Map<String, MObject> objectObjectHashMap = new HashMap<>();
        for (MObject datum : list) {
            objectObjectHashMap.put(datum.getName(), datum);
        }
        return objectObjectHashMap;
    }

        public void createRrCode(List<TonesIr> dataList) {
        Map<String, Integer> existCodeMap = new HashMap<>();
        for (TonesIr tonesIr : dataList) {
            Date createdTime = tonesIr.getCreatedTime() == null ? new Date() : tonesIr.getCreatedTime();
            StringBuilder resultCode = new StringBuilder("IR");
            resultCode.append(DASH);
            String currentDate = DateUtil.format(createdTime, "yyMMdd");
            resultCode.append(currentDate);
            Integer currentSerialNumber = 0;
            if (existCodeMap.containsKey(currentDate)) {
                currentSerialNumber = existCodeMap.get(currentDate);
            }
            currentSerialNumber++;
            existCodeMap.put(currentDate, currentSerialNumber);
            if (currentSerialNumber < NumberConstant.TEN) {
                resultCode.append("00").append(currentSerialNumber);
            } else if (currentSerialNumber < NumberConstant.HUNDRED) {
                resultCode.append("0").append(currentSerialNumber);
            } else {
                resultCode.append(currentSerialNumber);
            }
            tonesIr.setRequirementcoding(resultCode.toString());
        }
    }
    public void notify2(String instanceId, List<String> empNos, String name) {
        String url = String.format(apmWebUrl + apmInstanceWebPathTemplate, irProperties.getSpaceBid(), irProperties.getSrSpaceAppBid(), instanceId);
        String title = "您有一条需求需要填写研发工时，请前往【Transcend】工作台准确填写SR/AR的研发工时";
        PushCenterFeishuBuilder feishuBuilder = PushCenterFeishuBuilder.builder().title(title, "red")
                .image(title, apmMsgPictureUrl)
                .dividingLine()
                .content("您有一个新的任务需要处理，请前往【Transcend】工作台处理\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25")
                .dividingLine()
                .action(Lists.newArrayList(Button.builder().text(Text.builder().content("前往处理\uD83D\uDE80").build()).url(url).style("primary").build()))
                .url(url)
                .receivers(empNos);
        NotifyEventBus.EVENT_BUS.post(feishuBuilder);
    }




}
