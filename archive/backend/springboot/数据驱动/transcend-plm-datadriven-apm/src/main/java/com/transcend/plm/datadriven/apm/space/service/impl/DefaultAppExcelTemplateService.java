package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryItemDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.configcenter.api.model.view.dto.*;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.constants.SpaceConstant;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.service.IPlatformUserWrapper;
import com.transcend.plm.datadriven.apm.space.model.ApmSpaceAppDataDrivenOperationFilterBo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmStateVO;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description Excel模板管理
 * @createTime 2023-12-05 14:01:00
 */
@Slf4j
@Service
public class DefaultAppExcelTemplateService extends AbstractAppExcelTemplateService {
    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;


    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Resource
    private DictionaryFeignClient dictionaryFeignClient;
    @Resource
    private IAppDataService appDataService;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private IPlatformUserWrapper platformUserWrapper;
    ThreadLocal<Map<String, String>> lifeStateMap = new ThreadLocal<>();
    ThreadLocal<Map<String, Map<String, String>>> dicMap = new ThreadLocal<>();
    public static final String TEMPLATE_TYPE_TREE = "tree";
    public static final String TEMPLATE_TYPE_OBJECT = "object";
    public static final String SEQUENCE = "sequence";
    public static final String USER = "user";
    public static final String DATE = "date";
    public static final String DATETIME = "datetime";
    public static final String SLIDER = "slider";

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    /**
     * 初始化一个map，且放入数据
     */
    private static final Map<String, String> HEADER_COMMENT_MAP = MapUtil.builder(USER, "温馨提示：只能输入工号或者姓名(工号)，括号为英文格式")
            .put(DATE, "温馨提示：请输入正确的日期格式(yyyy/mm/dd或yyyy-mm-dd或yyyy年mm月dd日")
            .put(DATETIME, "温馨提示：请输入正确的日期时间格式(yyyy/mm/dd hh:mm:ss或yyyy-mm-dd hh:mm:ss或yyyy年mm月dd日 hh时mm分ss秒")
            .put(SLIDER, "请输入正确的进度(0-100)")
            .put(SEQUENCE, "输入正整数或者以小数点(.)连接的序列号 开始为 1")
            .build();
    private static final Set<String> DICT_TYPE_SET = Sets.newHashSet("select", "radio", "checkbox");

    private static final String RELATION = "relation";


    private static final String MODEL_CODE_LABEL = "A04";

    private static final String LABEL_SELECT = "label-select";

    @Override
    public ByteArrayOutputStream handleExcelOutputStream(String spaceAppBid, String type, List<ViewProperty> viewProperties) throws IOException {
        ByteArrayOutputStream outputStream;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            CellStyle headerStyle = getHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            Cell firstHeaderCell = createCell(sheet, headerStyle, headerRow, 0, "*序号");
            fillComment(sheet, 0, firstHeaderCell, HEADER_COMMENT_MAP.get(SEQUENCE));
            for (int i = 0; i < viewProperties.size(); i++) {
                int column = i + 1;
                ViewProperty viewProperty = viewProperties.get(i);
                ViewOptions options = viewProperty.getField().getOptions();
                String label = options.getLabel();
                if (Boolean.TRUE.equals((options.isRequired()))) {
                    label = "*" + label;
                }
                Cell cell = createCell(sheet, headerStyle, headerRow, column, label);
                //给cell添加注释
                String viewFieldType = viewProperty.getField().getType();
                if (HEADER_COMMENT_MAP.containsKey(viewFieldType)) {
                    fillComment(sheet, column, cell, HEADER_COMMENT_MAP.get(viewFieldType));
                }
                if (ObjectEnum.LIFE_CYCLE_CODE.getCode().equals(options.getName())) {
                    List<String> stateList = getStateList(spaceAppBid);
                    addValidation(workbook, sheet, column, stateList);
                } else if (DICT_TYPE_SET.contains(viewFieldType) && StringUtils.isNotBlank(options.getRemoteDictType())) {
                    List<String> dictValues = getDictValues(options.getRemoteDictType());
                    addValidation(workbook, sheet, column, dictValues);
                }
            }
            fillDemoData(workbook, sheet, type);
            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
        }
        return outputStream;
    }

    @Override
    public List<MSpaceAppData> importExcel(String spaceAppBid, MultipartFile file, String spaceBid) {
        //校验file是不是excel文件
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName) || (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx"))) {
            throw new TranscendBizException("文件格式不正确");
        }
        //用easyExcel读取excel数据，放入list当中
        List<ViewProperty> viewPropertyList = getSpaceAppViewConfig(spaceAppBid);
        Map<String, String> headerTypeMap = Maps.newHashMap();
        Map<String, String> headerLabelNameMap = Maps.newHashMap();
        Map<String, Boolean> requiredMap = Maps.newHashMap();
        Map<String, Map<String, String>> headerDictMap = Maps.newHashMap();
        Map<String, Map<String, String>> relationMap = Maps.newHashMap();
        initConfigData(viewPropertyList, headerTypeMap, headerLabelNameMap, headerDictMap, requiredMap, relationMap, spaceBid);
        List<MSpaceAppData> mSpaceAppDataList = Lists.newArrayList();
        // 创建 Workbook
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // 获取第一个 Sheet
            Sheet sheet = workbook.getSheetAt(0);
            // 读取表头
            Row headerRow = sheet.getRow(0);
            List<String> headerTypeList = Lists.newArrayList();
            List<String> headerList = Lists.newArrayList();
            for (Cell cell : headerRow) {
                String headerName = cell.getRichStringCellValue().getString();
                //去掉headerName开头的*
                if (headerName.startsWith("*")) {
                    headerName = headerName.substring(1);
                }
                String headerType = headerTypeMap.get(headerName) != null ? headerTypeMap.get(headerName) : "none";
                headerTypeList.add(headerType);
                headerList.add(headerName);
            }
            // 读取数据
            Map<String, String> stateNameAndCodeMap = getStateNameAndCodeMap(spaceAppBid);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null) {
                    continue;
                }
                MSpaceAppData mSpaceAppData = getMSpaceAppData(headerLabelNameMap, headerDictMap, headerTypeList, headerList, stateNameAndCodeMap, dataRow, requiredMap, relationMap);
                if (MapUtil.isNotEmpty(mSpaceAppData)) {
                    if (mSpaceAppData.size() == 1 && mSpaceAppData.containsKey("SEQUENCE")) {
                        mSpaceAppData.put("errMsg", "序号不能为空");
                    }
                    mSpaceAppDataList.add(mSpaceAppData);
                }
            }
        } catch (IOException ex) {
            throw new TranscendBizException("读取excel错误", ex);
        }
        return mSpaceAppDataList;
    }

    /**
     * 导入并保存数据
     *
     * @param spaceAppBid
     * @param file
     * @param spaceBid
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importExcelAndSave(String spaceAppBid, MultipartFile file, String spaceBid) {
        //校验file是不是excel文件
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName) || (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx"))) {
            throw new TranscendBizException("文件格式不正确");
        }
        //用easyExcel读取excel数据，放入list当中
        List<ViewProperty> viewPropertyList = getSpaceAppViewConfig(spaceAppBid);
        Map<String, String> headerTypeMap = Maps.newHashMap();
        Map<String, String> headerLabelNameMap = Maps.newHashMap();
        Map<String, Boolean> requiredMap = Maps.newHashMap();
        Map<String, Map<String, String>> headerDictMap = Maps.newHashMap();
        Map<String, Map<String, String>> relationMap = Maps.newHashMap();
        Map<String, String> relationCodeMap = Maps.newHashMap();
        //是否多选
        Map<String, Boolean> multiMap = Maps.newHashMap();
        initConfigDataSpecial(viewPropertyList, headerTypeMap, headerLabelNameMap, headerDictMap, requiredMap, relationMap, spaceBid, relationCodeMap, multiMap);
        List<MSpaceAppData> mSpaceAppDataList = Lists.newArrayList();
        Map<String, List<MObject>> relationMObjectMap = Maps.newHashMap();
        // 创建 Workbook
        String modelCode = apmSpaceAppService.getByBid(spaceAppBid).getModelCode();
        List<String> requiredFieldList = Lists.newArrayList();
        Map<String, String> headerFieldMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry : headerLabelNameMap.entrySet()) {
            if (requiredMap.containsKey(entry.getKey()) && requiredMap.get(entry.getKey())) {
                requiredFieldList.add(entry.getValue());
                headerFieldMap.put(entry.getValue(), entry.getKey());
            }
        }
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // 获取第一个 Sheet
            Sheet sheet = workbook.getSheetAt(0);
            // 读取表头
            Row headerRow = sheet.getRow(0);
            List<String> headerTypeList = Lists.newArrayList();
            List<String> headerList = Lists.newArrayList();
            for (Cell cell : headerRow) {
                String headerName = cell.getRichStringCellValue().getString();
                //去掉headerName开头的*
                if (headerName.startsWith("*")) {
                    headerName = headerName.substring(1);
                }
                String headerType = headerTypeMap.get(headerName) != null ? headerTypeMap.get(headerName) : "none";
                headerTypeList.add(headerType);
                headerList.add(headerName);
            }
            // 读取数据
            ApmLifeCycleStateVO lifeCycleState = apmSpaceAppConfigManageService.getLifeCycleState(spaceAppBid);
            if (lifeCycleState == null) {
                throw new TranscendBizException("生命周期模板不存在");
            }
            List<ApmStateVO> apmStateVOList = lifeCycleState.getApmStateVOList();
            if (CollectionUtils.isEmpty(apmStateVOList)) {
                throw new TranscendBizException("生命周期模板未配置状态");
            }
            Map<String, String> stateNameAndCodeMap = apmStateVOList.stream().collect(Collectors.toMap(ApmStateVO::getName, ApmStateVO::getLifeCycleCode));
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null) {
                    continue;
                }
                boolean allCellEmpty = true;
                for (int j = 0; j < dataRow.getLastCellNum(); j++) {
                    Cell cell = dataRow.getCell(j);
                    if (cell != null && StringUtils.isNotBlank(cell + "")) {
                        allCellEmpty = false;
                    }
                }
                if (allCellEmpty) {
                    continue;
                }
                String bid = SnowflakeIdWorker.nextIdStr();
                MSpaceAppData mSpaceAppData = getMObject(headerLabelNameMap, headerDictMap, headerTypeList, headerList, stateNameAndCodeMap, dataRow, requiredMap, relationMap, relationCodeMap, bid, relationMObjectMap, spaceBid, multiMap);
                if (MapUtil.isNotEmpty(mSpaceAppData)) {
                    //校验必填项
                    for (String required : requiredFieldList) {
                        if (mSpaceAppData.get(required) == null) {
                            throw new TranscendBizException("必填项[" + headerFieldMap.get(required) + "]不能为空");
                        }
                    }
                    mSpaceAppData.setBid(bid);
                    mSpaceAppData.put(TranscendModelBaseFields.DATA_BID, bid);
                    mSpaceAppData.put(TranscendModelBaseFields.SPACE_APP_BID, spaceAppBid);
                    mSpaceAppData.put(TranscendModelBaseFields.SPACE_BID, spaceBid);
                    mSpaceAppData.setModelCode(modelCode);
                    mSpaceAppData.put(TranscendModelBaseFields.LC_MODEL_CODE, mSpaceAppData.getLifeCycleCode() + ":" + mSpaceAppData.getModelCode());
                    mSpaceAppData.setLcTemplBid(lifeCycleState.getLcTemplBid());
                    mSpaceAppData.setLcTemplVersion(lifeCycleState.getLcTemplVersion());
                    if (mSpaceAppData.get(ObjectEnum.LIFE_CYCLE_CODE.getCode()) == null || StringUtils.isEmpty(mSpaceAppData.getLifeCycleCode())) {
                        mSpaceAppData.put(ObjectEnum.LIFE_CYCLE_CODE.getCode(), lifeCycleState.getInitState());
                    }
                    //添加到达时间
                    mSpaceAppData.put(ObjectEnum.REACH_TIME.getCode(), new Date());
                    mSpaceAppDataList.add(mSpaceAppData);
                }
            }
        } catch (IOException ex) {
            throw new TranscendBizException("读取excel错误", ex);
        }
        if (CollectionUtils.isNotEmpty(mSpaceAppDataList)) {
            //处理层级关联数据
            handlerParentBid(mSpaceAppDataList);
            ApmSpaceAppDataDrivenOperationFilterBo filterBo = new ApmSpaceAppDataDrivenOperationFilterBo() ;
            iBaseApmSpaceAppDataDrivenService.addBatch(spaceBid, spaceAppBid, mSpaceAppDataList, filterBo);
            //appDataService.addBatch(modelCode, mSpaceAppDataList);
            if (relationMObjectMap.size() > 0) {
                //保存关系数据
                for (Map.Entry<String, List<MObject>> entry : relationMObjectMap.entrySet()) {
                    appDataService.addBatch(entry.getKey(), entry.getValue());
                }
            }
        }
        return true;
    }

    static void handlerParentBid(List<MSpaceAppData> mSpaceAppDataList) {
        Map<String, String> sequenceBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MSpaceAppData appData : mSpaceAppDataList) {
            sequenceBidMap.put(appData.get("sequence") + "", appData.getBid());
        }
        for (MSpaceAppData appData : mSpaceAppDataList) {
            String sequence = appData.get("sequence") + "";
            int lastedIndexOf = sequence.lastIndexOf(".");
            if (lastedIndexOf == -1) {
                continue;
            }
            String parentBid = sequenceBidMap.get(sequence.substring(0, lastedIndexOf));
            appData.put(TranscendModelBaseFields.PARENT_BID, parentBid);
        }
    }

    @Override
    public ByteArrayOutputStream handleExcelData(ByteArrayOutputStream excelTemplate, List<MObject> mObjects, String spaceAppBid, String type) throws IOException {
        ByteArrayOutputStream outputStream;
        if (CollectionUtils.isEmpty(mObjects)) {
            byte[] bytes = excelTemplate.toByteArray();
            ByteArrayOutputStream copy = new ByteArrayOutputStream();
            copy.write(bytes);
            return copy;
        }
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelTemplate.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            fillObjectData(sheet, mObjects, spaceAppBid, type);
            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
        }
        return outputStream;
    }

    private static void clearColumn(Sheet sheet, int columnIndex, int startRowNum) {
        // 遍历每一行，清空指定列的内容
        for (Row row : sheet) {
            if (startRowNum > row.getRowNum()) {
                continue;
            }
            Cell cell = row.getCell(columnIndex);
            if (cell != null) {
                cell.setCellValue("");
            }
        }
    }

    private void fillObjectData(Sheet sheet, List<MObject> mObjects, String spaceAppBid, String type) {
        clearColumn(sheet, 0, 1);
        try {
            List<ViewProperty> viewProperties = getSpaceAppViewConfig(spaceAppBid);
            Map<String, Map<String, String>> dictNameAndCodeMap = Maps.newHashMap();
            for (ViewProperty viewProperty : viewProperties) {
                ViewOptions options = viewProperty.getField().getOptions();
                if (ObjectEnum.LIFE_CYCLE_CODE.getCode().equals(options.getName())) {
                    lifeStateMap.set(getStateCodeAndNameMap(spaceAppBid));
                } else if (DICT_TYPE_SET.contains(viewProperty.getField().getType()) && StringUtils.isNotBlank(options.getRemoteDictType())) {
                    Map<String, String> dictCodeAndNameMap = getDictCodeAndNameMap(options.getRemoteDictType());
                    dictNameAndCodeMap.put(options.getName(), dictCodeAndNameMap);
                }
            }
            dicMap.set(dictNameAndCodeMap);
            if (TEMPLATE_TYPE_TREE.equals(type)) {
                fillTreeData(sheet, mObjects, viewProperties, "", 1);
            } else if (TEMPLATE_TYPE_OBJECT.equals(type)) {
                fillObjectData(sheet, mObjects, viewProperties);
            }
        } finally {
            lifeStateMap.remove();
            dicMap.remove();
        }
    }

    private int fillTreeData(Sheet sheet, List<? extends MObject> mObjects, List<ViewProperty> viewProperties, String parentSequence, int currentRowNum) {
        for (int i = 0; i < mObjects.size(); i++) {
            Row row = sheet.getRow(currentRowNum++);
            if (row == null) {
                row = sheet.createRow(currentRowNum - 1);
            }
            MObject mObject = mObjects.get(i);
            MObjectTree mObjectTree;
            if (mObject instanceof MObjectTree) {
                mObjectTree = (MObjectTree) mObject;
            } else {
                throw new TranscendBizException("数据类型不匹配，无法导出树形结构");
            }
            String sequence = StringUtils.isNotBlank(parentSequence) ? parentSequence + "." + (i + 1) : (i + 1) + "";
            Cell firstCell = row.getCell(0);
            if (firstCell == null) {
                firstCell = row.createCell(0);
            }
            firstCell.setCellValue(sequence);
            setRowValue(viewProperties, row, mObject);
            List<MObjectTree> children = mObjectTree.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                currentRowNum = fillTreeData(sheet, children, viewProperties, sequence, currentRowNum);
            }
        }
        return currentRowNum;
    }

    private void setRowValue(List<ViewProperty> viewProperties, Row row, MObject mObject) {
        for (int i = 0; i < viewProperties.size(); i++) {
            Cell cell = row.getCell(i + 1);
            if (cell == null) {
                cell = row.createCell(i + 1);
            }
            ViewProperty viewProperty = viewProperties.get(i);
            ViewOptions options = viewProperty.getField().getOptions();
            Object value = mObject.get(options.getName());
            if (ObjectEnum.LIFE_CYCLE_CODE.getCode().equals(options.getName())) {
                value = lifeStateMap.get().get(value);
            } else if (DICT_TYPE_SET.contains(viewProperty.getField().getType()) && StringUtils.isNotBlank(options.getRemoteDictType())) {
                Map<String, String> dictCodeAndNameMap = dicMap.get().get(options.getName());
                value = dictCodeAndNameMap.get(value);
            } else if (USER.equals(viewProperty.getField().getType())) {
                ApmUser userBOByEmpNO = platformUserWrapper.getUserBOByEmpNO((String) value);
                if (userBOByEmpNO != null) {
                    value = userBOByEmpNO.getName() + "(" + userBOByEmpNO.getEmpNo() + ")";
                }
            }
            setCellValue(cell, value);
        }
    }

    private void fillObjectData(Sheet sheet, List<MObject> mObjects, List<ViewProperty> spaceAppViewConfig) {
        for (int i = 0; i < mObjects.size(); i++) {
            Row row = sheet.getRow(i + 1);
            MObject mObject = mObjects.get(i);
            row.getCell(0).setCellValue(i + 1);
            setRowValue(spaceAppViewConfig, row, mObject);
        }
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            cell.setCellValue(((LocalDate) value).format(formatter));
        } else if (value instanceof LocalDateTime) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            cell.setCellValue(((LocalDateTime) value).format(formatter));
        } else if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            cell.setCellValue(sdf.format((Date) value));
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        } else if (value instanceof Short) {
            cell.setCellValue((Short) value);
        } else if (value instanceof Byte) {
            cell.setCellValue((Byte) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            // 如果有其他特殊类型，可以在这里进行处理
            cell.setCellValue(value.toString());
        }
    }

    private Map<String, String> getRelationResourceMap(SourceModelCodeInfo sourceModelCodeInfo, String spaceBid, RelationInfo relationInfo) {
        Map<String, String> relationResourceMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        String modelCode = "";
        if (sourceModelCodeInfo != null) {
            modelCode = sourceModelCodeInfo.getModelCode();
        } else if (relationInfo != null) {
            modelCode = relationInfo.getSourceModelCode();
        }
        if (StringUtils.isEmpty(spaceBid) || StringUtils.isEmpty(modelCode)) {
            return relationResourceMap;
        }
        QueryWrapper qo = new QueryWrapper(Boolean.FALSE);
        qo.eq("spaceBid", spaceBid).and().eq("deleteFlag", 0);
        List<MObject> mObjectList = objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(qo));
        if (CollectionUtils.isNotEmpty(mObjectList)) {
            for (MObject mObject : mObjectList) {
                relationResourceMap.put(mObject.getName(), mObject.getBid());
            }
        }
        return relationResourceMap;
    }

    private Map<String, String> getInstanceNameMap(String modelCode, String spaceBid) {
        Map<String, String> relationResourceMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if (StringUtils.isEmpty(spaceBid) || StringUtils.isEmpty(modelCode)) {
            return relationResourceMap;
        }
        QueryWrapper qo = new QueryWrapper(Boolean.FALSE);
        qo.eq("spaceBid", spaceBid).and().eq("deleteFlag", 0);
        List<MObject> mObjectList = objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(qo));
        if (CollectionUtils.isNotEmpty(mObjectList)) {
            for (MObject mObject : mObjectList) {
                relationResourceMap.put(mObject.getName(), mObject.getBid());
            }
        }
        return relationResourceMap;
    }

    private void initConfigData(List<ViewProperty> viewPropertyList, Map<String, String> headerTypeMap, Map<String, String> headerLabelNameMap, Map<String, Map<String, String>> headerDictMap, Map<String, Boolean> requiredMap, Map<String, Map<String, String>> relationMap, String spaceBid) {
        headerTypeMap.put("序号", SEQUENCE);
        headerLabelNameMap.put("序号", SEQUENCE);
        for (ViewProperty viewProperty : viewPropertyList) {
            String name = viewProperty.getField().getOptions().getName();
            String label = viewProperty.getField().getOptions().getLabel();
            if (ObjectEnum.LIFE_CYCLE_CODE.getCode().equals(name)) {
                headerTypeMap.put(label, ObjectEnum.LIFE_CYCLE_CODE.getCode());
            } else if (DICT_TYPE_SET.contains(viewProperty.getField().getType()) && StringUtils.isNotBlank(viewProperty.getField().getOptions().getRemoteDictType())) {
                headerDictMap.put(label, getDictNameAndCodeMap(viewProperty.getField().getOptions().getRemoteDictType()));
                headerTypeMap.put(label, viewProperty.getField().getType());
            } else if (RELATION.equals(viewProperty.getField().getType())) {
                relationMap.put(label, getRelationResourceMap(viewProperty.getField().getOptions().getSourceModelCodeInfo(), spaceBid, viewProperty.getField().getOptions().getRelationInfo()));
                headerTypeMap.put(label, viewProperty.getField().getType());
            } else {
                headerTypeMap.put(label, viewProperty.getField().getType());
            }
            headerLabelNameMap.put(label, name);
            requiredMap.put(label, viewProperty.getField().getOptions().isRequired());
        }
    }

    /**
     * 导入特殊处理
     */
    private void initConfigDataSpecial(List<ViewProperty> viewPropertyList, Map<String, String> headerTypeMap, Map<String, String> headerLabelNameMap, Map<String, Map<String, String>> headerDictMap, Map<String, Boolean> requiredMap, Map<String, Map<String, String>> relationMap, String spaceBid, Map<String, String> relationCodeMap, Map<String, Boolean> multiMap) {
        headerTypeMap.put("序号", SEQUENCE);
        headerLabelNameMap.put("序号", SEQUENCE);
        for (ViewProperty viewProperty : viewPropertyList) {
            String name = viewProperty.getField().getOptions().getName();
            String label = viewProperty.getField().getOptions().getLabel();
            if (ObjectEnum.LIFE_CYCLE_CODE.getCode().equals(name)) {
                headerTypeMap.put(label, ObjectEnum.LIFE_CYCLE_CODE.getCode());
            } else if (DICT_TYPE_SET.contains(viewProperty.getField().getType()) && StringUtils.isNotBlank(viewProperty.getField().getOptions().getRemoteDictType())) {
                headerDictMap.put(label, getDictNameAndCodeMap(viewProperty.getField().getOptions().getRemoteDictType()));
                headerTypeMap.put(label, viewProperty.getField().getType());
            } else if (RELATION.equals(viewProperty.getField().getType())) {
                relationMap.put(label, getRelationResourceMap(viewProperty.getField().getOptions().getSourceModelCodeInfo(), spaceBid, viewProperty.getField().getOptions().getRelationInfo()));
                headerTypeMap.put(label, viewProperty.getField().getType());
                relationCodeMap.put(label, viewProperty.getField().getOptions().getRelationInfo().getModelCode());
            } else if (LABEL_SELECT.equals(viewProperty.getField().getType())) {
                headerTypeMap.put(label, viewProperty.getField().getType());
                relationMap.put(label, getInstanceNameMap(MODEL_CODE_LABEL, spaceBid));
                relationCodeMap.put(label, MODEL_CODE_LABEL);
            }else {
                headerTypeMap.put(label, viewProperty.getField().getType());
            }
            ViewField field = viewProperty.getField();
            if (field != null && field.getOptions() != null) {
                multiMap.put(label, field.getOptions().isMultiple());
            } else {
                multiMap.put(label, false);
            }
            headerLabelNameMap.put(label, name);
            requiredMap.put(label, viewProperty.getField().getOptions().isRequired());
        }
    }

    private MSpaceAppData getMSpaceAppData(Map<String, String> headerLabelNameMap, Map<String, Map<String, String>> headerDictMap, List<String> headerTypeList, List<String> headerList, Map<String, String> stateNameAndCodeMap, Row dataRow, Map<String, Boolean> requiredMap, Map<String, Map<String, String>> relationMap) {
        MSpaceAppData mSpaceAppData = new MSpaceAppData();
        String errMsg = "";
        for (int j = 0; j < dataRow.getLastCellNum(); j++) {
            Cell cell = dataRow.getCell(j);
            if (cell == null) {
                continue;
            }
            String headerType = headerTypeList.get(j);
            String headerName = headerList.get(j);
            String cellValue = getCellValue(cell);
            if (Boolean.TRUE.equals(requiredMap.get(headerName)) && StringUtils.isBlank(cellValue) && mSpaceAppData.get(SEQUENCE) != null) {
                errMsg = headerName + "不能为空;";
                continue;
            }
            if (StringUtils.isBlank(cellValue)) {
                continue;
            }
            if (headerLabelNameMap.get(headerName) == null) {
                log.error("excel表头名称不正确,{}", headerName);
                errMsg = "表头名称不正确[" + headerName + "];";
                continue;
            }
            if (ObjectEnum.LIFE_CYCLE_CODE.getCode().equals(headerType)) {
                String stateCode = stateNameAndCodeMap.get(cellValue);
                if (StringUtils.isBlank(stateCode)) {
                    errMsg = headerName + "状态值不存在[" + cellValue + "];";
                }
                cellValue = stateCode;
            } else if (DICT_TYPE_SET.contains(headerType)) {
                Map<String, String> dictNameAndCodeMap = headerDictMap.get(headerName);
                String dictCode = dictNameAndCodeMap.get(cellValue);
                if (StringUtils.isBlank(dictCode)) {
                    errMsg = headerName + "字典值不存在[" + cellValue + "];";
                }
                cellValue = dictCode;
                //如果是日期类型，需要转换成LocalDate
            } else if (RELATION.equals(headerType)) {
                Map<String, String> relMap = relationMap.get(headerName);
                String bid = relMap.get(cellValue);
                cellValue = bid;
            } else if (DATE.equals(headerType)) {
                //校验cellValue是否是日期格式
                if (cellValue.startsWith("reserved-")) {
                    // 从字符串中解析出日期的序列号
                    String[] parts = cellValue.split("-|x");
                    int dateSerial = Integer.parseInt(parts[1]);

                    // 将日期的序列号转换为 LocalDate 对象
                    LocalDate date = LocalDate.of(1900, 1, 1).plusDays(dateSerial - 2);

                    cellValue = date.toString();
                }
                if (StringUtils.isNotBlank(cellValue)) {
                    try {
                        LocalDate.parse(cellValue);
                    } catch (Exception e) {
                        errMsg = headerName + "日期格式不正确[" + cellValue + "];";
                    }
                }
            } else if (USER.equals(headerType)) {
                //校验cellValue是否是人名或者工号
                if (StringUtils.isNumeric(cellValue) || (cellValue.contains("(") && cellValue.contains(")"))) {
                    cellValue = extractContentInBrackets(cellValue);
                    ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(cellValue);
                    if (apmUser == null) {
                        errMsg = headerName + "工号不存在[" + cellValue + "];";
                    }
                } else {
                    errMsg = headerName + "工号格式不正确[" + cellValue + "];";
                }

            } else if (SLIDER.equals(headerType)) {
                //校验cellValue是否是进度
                try {
                    int slider = Integer.parseInt(cellValue);
                    if (slider < 0 || slider > 100) {
                        errMsg = headerName + "进度值不正确[" + cellValue + "];";
                    }
                } catch (Exception e) {
                    errMsg = headerName + "进度值不正确[" + cellValue + "];";
                }

            }
            if (StringUtils.isNotBlank(cellValue)) {
                mSpaceAppData.put(headerLabelNameMap.get(headerName), cellValue);
            }
        }
        if (StringUtils.isNotBlank(errMsg)) {
            mSpaceAppData.put("errMsg", errMsg);
        }
        return mSpaceAppData;
    }

    private MSpaceAppData getMObject(Map<String, String> headerLabelNameMap, Map<String, Map<String, String>> headerDictMap, List<String> headerTypeList, List<String> headerList, Map<String, String> stateNameAndCodeMap, Row dataRow, Map<String, Boolean> requiredMap, Map<String, Map<String, String>> relationMap, Map<String, String> relationCodeMap, String sourceBid, Map<String, List<MObject>> relationMObjectMap, String spaceBid, Map<String, Boolean> multiMap) {
        MSpaceAppData mSpaceAppData = new MSpaceAppData();
        String errMsg = "";
        Long tenantId = SsoHelper.getTenantId();
        boolean isEmpty = true;
        for (int j = 0; j < dataRow.getLastCellNum(); j++) {
            Cell cell = dataRow.getCell(j);
            if (j >= headerList.size()) {
                continue;
            }
            String headerName = headerList.get(j);
            if (cell == null) {
                if (Boolean.TRUE.equals(requiredMap.get(headerName))) {
                    errMsg = headerName + "不能为空;";
                    throw new BusinessException(errMsg);
                }
                continue;
            }
            isEmpty = false;
            String headerType = headerTypeList.get(j);

            String cellValue = getCellValue(cell);
            if (Boolean.TRUE.equals(requiredMap.get(headerName)) && StringUtils.isBlank(cellValue)) {
                errMsg = headerName + "不能为空;";
                throw new BusinessException(errMsg);
            }
            if (StringUtils.isBlank(cellValue)) {
                continue;
            }
            if (headerLabelNameMap.get(headerName) == null) {
                log.error("excel表头名称不正确,{}", headerName);
                errMsg = "表头名称不正确[" + headerName + "];";
                throw new BusinessException(errMsg);
            }
            if (ObjectEnum.LIFE_CYCLE_CODE.getCode().equals(headerType)) {
                String stateCode = stateNameAndCodeMap.get(cellValue);
                if (StringUtils.isBlank(stateCode)) {
                    errMsg = headerName + "状态值不存在[" + cellValue + "];";
                    throw new BusinessException(errMsg);
                }
                cellValue = stateCode;
            } else if (DICT_TYPE_SET.contains(headerType)) {
                Map<String, String> dictNameAndCodeMap = headerDictMap.get(headerName);
                if (dictNameAndCodeMap != null) {
                    String dictCode = dictNameAndCodeMap.get(cellValue);
                    if (StringUtils.isNotEmpty(dictCode)) {
                        cellValue = dictCode;
                    }
                }
                //如果是日期类型，需要转换成LocalDate
            } else if (RELATION.equals(headerType)) {
                Map<String, String> relMap = relationMap.get(headerName);
                String relationCode = relationCodeMap.get(headerName);
                List<MObject> relationMObjectList = relationMObjectMap.get(relationCode);
                if (relationMObjectList == null) {
                    relationMObjectList = new ArrayList<>();
                }
                if (Boolean.TRUE.equals(multiMap.get(headerName))) {
                    List<String> cellValues = new ArrayList<>();
                    for (String cellValueItem : cellValue.split(",")) {
                        String bid = relMap.get(cellValueItem);
                        cellValueItem = bid;
                        cellValues.add(cellValueItem);
                        MObject relationMObject = new MObject();
                        relationMObject.setBid(SnowflakeIdWorker.nextIdStr());
                        relationMObject.setModelCode(relationCode);
                        relationMObject.put("sourceBid", bid);
                        relationMObject.put("targetBid", sourceBid);
                        relationMObject.put("sourceDataBid", bid);
                        relationMObject.put("targetDataBid", sourceBid);
                        relationMObject.put("spaceBid", spaceBid);
                        relationMObject.put("tenantId", tenantId);
                        relationMObject.put("draft", "0");
                        relationMObject.put("dataBid", relationMObject.getBid());
                        relationMObjectList.add(relationMObject);
                    }
                    if (cellValues.size() > 1) {
                        cellValue = String.join(",", cellValues);
                    }
                } else {
                    String bid = relMap.get(cellValue);
                    cellValue = bid;
                    MObject relationMObject = new MObject();
                    relationMObject.setBid(SnowflakeIdWorker.nextIdStr());
                    relationMObject.setModelCode(relationCode);
                    relationMObject.put("sourceBid", bid);
                    relationMObject.put("targetBid", sourceBid);
                    relationMObject.put("sourceDataBid", bid);
                    relationMObject.put("targetDataBid", sourceBid);
                    relationMObject.put("spaceBid", spaceBid);
                    relationMObject.put("tenantId", tenantId);
                    relationMObject.put("draft", "0");
                    relationMObject.put("dataBid", relationMObject.getBid());
                    relationMObjectList.add(relationMObject);
                }
                relationMObjectMap.put(relationCode, relationMObjectList);
            }  else if (LABEL_SELECT.equals(headerType)) {
                Map<String, String> relMap = relationMap.get(headerName);
                String relationCode = relationCodeMap.get(headerName);
                List<MObject> relationMObjectList = relationMObjectMap.get(relationCode);
                if (relationMObjectList == null) {
                    relationMObjectList = new ArrayList<>();
                }
                if (Boolean.TRUE.equals(multiMap.get(headerName))) {
                    List<String> cellValues = new ArrayList<>();
                    for (String cellValueItem : cellValue.split(",")) {
                        String bid = relMap.get(cellValueItem);
                        if (StringUtils.isBlank(bid)) {
                            bid = SnowflakeIdWorker.nextIdStr();
                            MObject relationMObject = new MObject();
                            relationMObject.setBid(bid);
                            relationMObject.setName(cellValueItem);
                            relationMObject.setModelCode(relationCode);
                            relationMObject.setLcModelCode("none");
                            relationMObject.setLifeCycleCode("none");
                            relationMObject.setLcTemplVersion("none");
                            relationMObject.setLcTemplBid("none");
                            relationMObject.put("spaceBid", spaceBid);
                            relationMObject.put("tenantId", tenantId);
                            relationMObject.put("draft", "0");
                            relationMObject.put("dataBid", bid);
                            relationMObjectList.add(relationMObject);
                        }
                        cellValues.add(bid);
                    }
                    if (cellValues.size() > 1) {
                        cellValue = String.join(",", cellValues);
                    }
                } else {
                    String bid = relMap.get(cellValue);
                    if (StringUtils.isBlank(bid)) {
                        bid = SnowflakeIdWorker.nextIdStr();
                        MObject relationMObject = new MObject();
                        relationMObject.setBid(bid);
                        relationMObject.setName(cellValue);
                        relationMObject.setModelCode(relationCode);
                        relationMObject.setLcModelCode("none");
                        relationMObject.setLifeCycleCode("none");
                        relationMObject.setLcTemplVersion("none");
                        relationMObject.setLcTemplBid("none");
                        relationMObject.put("spaceBid", spaceBid);
                        relationMObject.put("tenantId", tenantId);
                        relationMObject.put("draft", "0");
                        relationMObject.put("dataBid", bid);
                        relationMObjectList.add(relationMObject);
                    }
                    cellValue = bid;
                }
                relationMObjectMap.put(relationCode, relationMObjectList);
            } else if (DATE.equals(headerType)) {
                //校验cellValue是否是日期格式
                if (cellValue.startsWith("reserved-")) {
                    // 从字符串中解析出日期的序列号
                    String[] parts = cellValue.split("-|x");
                    int dateSerial = Integer.parseInt(parts[1]);

                    // 将日期的序列号转换为 LocalDate 对象
                    LocalDate date = LocalDate.of(1900, 1, 1).plusDays(dateSerial - 2);

                    cellValue = date.toString();
                }
                if (StringUtils.isNotBlank(cellValue)) {
                    try {
                        LocalDate.parse(cellValue);
                    } catch (Exception e) {
                        errMsg = headerName + "日期格式不正确[" + cellValue + "];";
                        throw new BusinessException(errMsg);
                    }
                }
            } else if (USER.equals(headerType)) {
                //校验cellValue是否是人名或者工号


            } else if (SLIDER.equals(headerType)) {
                //校验cellValue是否是进度
                try {
                    int slider = Integer.parseInt(cellValue);
                    if (slider < 0 || slider > 100) {
                        errMsg = headerName + "进度值不正确[" + cellValue + "];";
                        throw new BusinessException(errMsg);
                    }
                } catch (Exception e) {
                    errMsg = headerName + "进度值不正确[" + cellValue + "];";
                    throw new BusinessException(errMsg);
                }
            }
            if (StringUtils.isNotBlank(cellValue)) {
                if (multiMap.get(headerName) != null && multiMap.get(headerName)) {
                    String[] cellValues = cellValue.split(",");
                    mSpaceAppData.put(headerLabelNameMap.get(headerName), JSON.toJSON(cellValues));
                } else {
                    mSpaceAppData.put(headerLabelNameMap.get(headerName), cellValue);
                }
            }
        }
        if (StringUtils.isNotBlank(errMsg)) {
            mSpaceAppData.put("errMsg", errMsg);
            throw new BusinessException(errMsg);
        }
        if (isEmpty) {
            return null;
        }
        mSpaceAppData.put("tenantId", tenantId);
        return mSpaceAppData;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是日期类型，转化为 LocalDateTime
                    return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                } else {
                    // 如果是数值类型，防止长数字出现科学计数法
                    DataFormatter formatter = new DataFormatter();
                    return formatter.formatCellValue(cell);
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                // 根据公式计算的结果类型，进行处理
                CellType cachedFormulaResultType = cell.getCachedFormulaResultType();
                switch (cachedFormulaResultType) {
                    case NUMERIC:
                        return String.valueOf(cell.getNumericCellValue());
                    case STRING:
                        return cell.getStringCellValue();
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    default:
                        return "";
                }

            default:
                return "";
        }
    }

    private void fillDemoData(Workbook workbook, Sheet sheet, String type) {
        String[] demoData = {"1"};
        if (TEMPLATE_TYPE_TREE.equals(type)) {
            demoData = new String[]{"1", "1.1", "1.1.1", "2", "2.1", "2.2", "2.2.1", "2.3"};
        }
        // 创建一个数据格式对象，并定义一个文本格式
        DataFormat format = workbook.createDataFormat();
        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setDataFormat(format.getFormat("@"));
        for (int i = 0; i < demoData.length; i++) {
            Row row = sheet.createRow(i + 1);
            Cell cell = row.createCell(0);
            cell.setCellStyle(textStyle);
            cell.setCellValue(demoData[i]);
        }
    }

    private List<String> getStateList(String spaceAppBid) {
        List<ApmStateVO> apmStateVOList = getLifeStateConfig(spaceAppBid);
        return apmStateVOList.stream().map(ApmStateVO::getName).collect(Collectors.toList());
    }

    private List<ApmStateVO> getLifeStateConfig(String spaceAppBid) {
        ApmLifeCycleStateVO lifeCycleState = apmSpaceAppConfigManageService.getLifeCycleState(spaceAppBid);
        if (lifeCycleState == null) {
            throw new TranscendBizException("生命周期模板不存在");
        }
        List<ApmStateVO> apmStateVOList = lifeCycleState.getApmStateVOList();
        if (CollectionUtils.isEmpty(apmStateVOList)) {
            throw new TranscendBizException("生命周期模板未配置状态");
        }
        return apmStateVOList;
    }

    /**
     * 获取生命周期状态code和name的map
     *
     * @param spaceAppBid
     * @return
     */
    private Map<String, String> getStateCodeAndNameMap(String spaceAppBid) {
        List<ApmStateVO> apmStateVOList = getLifeStateConfig(spaceAppBid);
        return apmStateVOList.stream().collect(Collectors.toMap(ApmStateVO::getLifeCycleCode, ApmStateVO::getName));
    }

    private Map<String, String> getStateNameAndCodeMap(String spaceAppBid) {
        List<ApmStateVO> apmStateVOList = getLifeStateConfig(spaceAppBid);
        return apmStateVOList.stream().collect(Collectors.toMap(ApmStateVO::getName, ApmStateVO::getLifeCycleCode));
    }

    private static void addValidation(Workbook wb, Sheet sheet, int column, List<String> dictValues) {
        if (CollectionUtils.isNotEmpty(dictValues)) {
            //设置下拉框
            //解决下拉框数据过大问题
            DataValidationHelper helper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint;
            if (dictValues.size() > 20) {
                String refers = refers(wb, dictValues);
                //5 将刚才设置的sheet引用到你的下拉列表中
                constraint = helper.createFormulaListConstraint(refers);
            }else {
                constraint = helper.createExplicitListConstraint(dictValues.toArray(new String[0]));
            }
            CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, column, column);
            DataValidation validation = helper.createValidation(constraint, addressList);
            // 处理显示错误信息
            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("Invalid entry", "You must select an option from the list.");
            sheet.addValidationData(validation);
        }
    }

    @NotNull
    private static String refers(Workbook wb, List<String> dictValues) {
        String hiddenName = com.transcend.plm.datadriven.common.util.SnowflakeIdWorker.nextIdStr();
        Sheet hidden= wb.createSheet(hiddenName);
        String excelLine = "A";
        //2.循环赋值
        for (int i = 0, length = dictValues.size(); i < length; i++) {
            // 3:表示你开始的行数  3表示 你开始的列数
            Row row = hidden.getRow(i);
            if (row == null) {
                row = hidden.createRow(i);
            }
            row.createCell(0).setCellValue(dictValues.get(i));
        }
        return "=" + hiddenName + "!$" + excelLine +
                "$1:$" + excelLine + "$" + (dictValues.size() + 1);
    }

    private static void fillComment(Sheet sheet, int i, Cell cell, String commentStr) {
        // 创建一个 Drawing 对象，用于添加注释
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        // 创建一个 ClientAnchor 对象，用于定位注释
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, i, 0, i + 1, 5);
        // 创建一个 Comment 对象并设置文本
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString(commentStr));
        cell.setCellComment(comment);
    }

    private static Cell createCell(Sheet sheet, CellStyle headerStyle, Row headerRow, int column, String label) {
        Cell cell = headerRow.createCell(column);
        cell.setCellValue(label);
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(column, label.getBytes().length * 256 + 4 * 256);
        return cell;
    }

    private static CellStyle getHeaderStyle(Workbook workbook) {
        // 创建一个 CellStyle 对象，用于设置 header 样式
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置水平居中
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        // 设置垂直居中
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置边框
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());

        // 创建一个 Font 对象，用于设置 header 字体
        Font headerFont = workbook.createFont();
        headerFont.setFontName("宋体");
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        // 将字体应用于样式
        headerStyle.setFont(headerFont);

        // 将字体应用于样式
        headerStyle.setFont(headerFont);
        return headerStyle;
    }

    @Override
    public List<ViewProperty> getSpaceAppViewConfig(String spaceAppBid) {
        CfgViewVo cfgViewVo = apmSpaceAppConfigManageService.baseViewGet(spaceAppBid);
        if (cfgViewVo == null) {
            throw new TranscendBizException("空间应用视图配置不存在");
        }
        Object propertiesList = cfgViewVo.getContent().get("propertiesList");
        if (propertiesList == null) {
            throw new TranscendBizException("空间应用视图未配置属性");
        }
        List<ViewProperty> viewProperties = JSON.parseArray(JSON.toJSONString(propertiesList), ViewProperty.class);
        if (CollectionUtils.isNotEmpty(viewProperties)) {
            //去掉禁用的属性
            for (int i = viewProperties.size() - 1; i >= 0; i--) {
                ViewProperty viewProperty = viewProperties.get(i);
                ViewField viewField = viewProperty.getField();
                if (viewField != null && viewField.getOptions() != null && viewField.getOptions().isDisabled()) {
                    viewProperties.remove(i);
                }
            }
        }
        return viewProperties;
    }

    /**
     * 获取字典值
     */
    public List<String> getDictValues(String remoteDictType) {
        CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
        qo.setCodes(Lists.newArrayList(remoteDictType));
        List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
        if (CollectionUtils.isEmpty(cfgDictionaryVos)) {
            return Lists.newArrayList();
        }
        List<CfgDictionaryItemVo> dictionaryItems = cfgDictionaryVos.get(0).getDictionaryItems();
        if (CollectionUtils.isEmpty(dictionaryItems)) {
            return Lists.newArrayList();
        }
        return dictionaryItems.stream().map(item -> (String) item.get("zh")).collect(Collectors.toList());
    }

    public Map<String, String> getDictNameAndCodeMap(String remoteDictType) {
        CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
        qo.setCodes(Lists.newArrayList(remoteDictType));
        List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
        if (CollectionUtils.isEmpty(cfgDictionaryVos)) {
            return Maps.newHashMap();
        }
        List<CfgDictionaryItemVo> dictionaryItems = cfgDictionaryVos.get(0).getDictionaryItems();
        if (CollectionUtils.isEmpty(dictionaryItems)) {
            return Maps.newHashMap();
        }
        return dictionaryItems.stream().collect(Collectors.toMap(item -> (String) item.get("zh"), CfgDictionaryItemDto::getKeyCode, (k1, k2) -> k1));
    }

    /**
     * 获取字典code和name的map
     *
     * @param remoteDictType
     * @return key:code value:name
     */
    private Map<String, String> getDictCodeAndNameMap(String remoteDictType) {
        CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
        qo.setCodes(Lists.newArrayList(remoteDictType));
        List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
        if (CollectionUtils.isEmpty(cfgDictionaryVos)) {
            return Maps.newHashMap();
        }
        List<CfgDictionaryItemVo> dictionaryItems = cfgDictionaryVos.get(0).getDictionaryItems();
        if (CollectionUtils.isEmpty(dictionaryItems)) {
            return Maps.newHashMap();
        }
        return dictionaryItems.stream().collect(Collectors.toMap(CfgDictionaryItemDto::getKeyCode, item -> (String) item.get("zh")));
    }

    public static String extractContentInBrackets(String input) {
        // 定义正则表达式，匹配括号内的内容
        String str = "\\(([^)]+)\\)";
        Pattern pattern = Pattern.compile(str);

        // 使用正则表达式进行匹配
        Matcher matcher = pattern.matcher(input);

        // 查找匹配项
        if (matcher.find()) {
            // 返回括号内的内容
            return matcher.group(1);
        } else {
            // 如果没有找到匹配项，返回空字符串或者其他你希望返回的默认值
            return input;
        }
    }

    /**
     * 获取字典code和英文name的map
     *
     * @param remoteDictType
     * @return key:code value:name
     */
    public Map<String, String> getDictCodeAndEnNameMap(String remoteDictType) {
        CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
        qo.setCodes(Lists.newArrayList(remoteDictType));
        List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
        if (CollectionUtils.isEmpty(cfgDictionaryVos)) {
            return Maps.newHashMap();
        }
        List<CfgDictionaryItemVo> dictionaryItems = cfgDictionaryVos.get(0).getDictionaryItems();
        if (CollectionUtils.isEmpty(dictionaryItems)) {
            return Maps.newHashMap();
        }
        return dictionaryItems.stream().filter(
                cfgDictionaryItemVo -> cfgDictionaryItemVo.getEnableFlag() != null && CommonConst.ENABLE_FLAG_ENABLE.equals(cfgDictionaryItemVo.getEnableFlag())
        ).collect(Collectors.toMap(CfgDictionaryItemDto::getKeyCode, item -> (String) item.get("en"), (k1, k2) -> k1));
    }
}
