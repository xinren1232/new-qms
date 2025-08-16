package com.transcend.plm.datadriven.apm.space.service.impl;

import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.MObjectCheckDto;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.apm.space.service.ICrossSpaceService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.platform.feign.model.dto.PlatFormUserDTO;
import com.transcend.plm.datadriven.platform.service.IPlatformService;
import com.transsion.framework.uac.model.dto.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 跨空间服务实现类
 *
 * @author unknown
 */
@Slf4j
@Service
public class CrossSpaceServiceImpl implements ICrossSpaceService {
    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private IAppDataService appDataService;

    @Resource
    private IPlatformService platformService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;


    @Override
    public List<MObjectTree> selectTree(String spaceBid, String modelCode, RelationMObject relationMObject) {
        List<QueryWrapper> wrappers = QueryConveterTool.convert(relationMObject.getModelMixQo()).getQueries();
        QueryCondition queryCondition = QueryCondition.of().setQueries(wrappers);
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getApmSpaceAppBySpaceBidAndModelCode(spaceBid, modelCode);
        if (apmSpaceApp == null) {
            return new ArrayList<>();
        }
        List<MObject> list = appDataService.list(spaceBid, apmSpaceApp.getBid(), queryCondition);
        return objectModelCrudI.listTree(list, relationMObject, Boolean.TRUE);
    }

    @Override
    public List<MObject> relationSelectListExpand(String spaceBid, String modelCode, String source, MObjectCheckDto mObjectCheckDto) {
        if ("inner".equals(source)) {
            List<QueryWrapper> wrappers = QueryConveterTool.convert(mObjectCheckDto.getModelMixQo()).getQueries();
            QueryCondition queryCondition = QueryCondition.of().setQueries(wrappers);
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getApmSpaceAppBySpaceBidAndModelCode(spaceBid, modelCode);
            if (apmSpaceApp == null) {
                return new ArrayList<>();
            }
            // 选取 IR数据，关注人 + 保密人可以选 条件：源和目标都相同，TODO
            List<MObject> list = appDataService.list(spaceBid, apmSpaceApp.getBid(), queryCondition);
            List selectList = objectModelCrudI.relationSelectList(list, mObjectCheckDto);
            // 处理停留时长
            handleTime(selectList, modelCode);
            return selectList;
        } else if ("outer".equals(source)) {
            switch (mObjectCheckDto.getRelationModelCode()) {
                case "A3H": {
                    ModelFilterQo queryCondition = mObjectCheckDto.getModelMixQo().getQueries().stream()
                            .filter(query -> "name".equals(query.getProperty()))
                            .findFirst()
                            .orElseThrow(() -> new PlmBizException("查询条件中必须包含name属性"));
                    String conditionValue = (String) queryCondition.getValue();
                    PageDTO<PlatFormUserDTO> userData = platformService.queryPlatformUser(conditionValue);
                    return userData.getDataList().stream().map(user -> {
                        MObject mObject = new MObject();
                        mObject.setModelCode("A3H");
                        mObject.setBid(com.transcend.framework.common.util.SnowflakeIdWorker.nextIdStr());
                        mObject.put("name", user.getEmployeeName());
                        mObject.put("perno", user.getEmployeeNo());
                        return mObject;
                    }).collect(Collectors.toList());
                }
                default: {
                }
            }
        }
        return null;
    }

    private void handleTime(List<MObject> list, String modelCode) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (MObject data : list) {
                Object time = data.get(ObjectEnum.REACH_TIME.getCode());
                if (time != null) {
                    String timeStr = time.toString();
                    LocalDateTime dateTime = null;
                    DateTimeFormatter formatterWithSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    DateTimeFormatter formatterWithoutSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

                    try {
                        dateTime = LocalDateTime.parse(timeStr, formatterWithSeconds);
                    } catch (DateTimeParseException e) {
                        try {
                            dateTime = LocalDateTime.parse(timeStr, formatterWithoutSeconds);
                        } catch (DateTimeParseException ex) {
                            log.error("时间转换异常", ex);
                            continue;
                        }
                    }

                    if (dateTime != null) {
                        Date date = java.sql.Timestamp.valueOf(dateTime);
                        Date nowDate = new Date();
                        long diffMillis = Math.abs(nowDate.getTime() - date.getTime());
                        if ("A03".equals(modelCode)) {
                            // 计算小时数
                            long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
                            data.put(CommonConst.STAY_DURATION, hours + "小时");
                        } else {
                            // 计算天和剩余小时
                            long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
                            long remainingMillis = diffMillis - TimeUnit.DAYS.toMillis(days);
                            long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis);
                            StringBuilder sb = new StringBuilder();
                            if (days > 0) {
                                sb.append(days).append("天");
                            }
                            sb.append(hours).append("小时");
                            data.put(CommonConst.STAY_DURATION, sb.toString());
                        }
                    }
                }
            }
        }
    }
}
