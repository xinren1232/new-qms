package com.transcend.plm.datadriven.apm.aspect;

import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.log.context.ValueChangeContext;
import com.transcend.plm.datadriven.apm.log.model.dto.GenericLogAddParam;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigDrivenService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.domain.object.version.VersionObjectManageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author Qiu Yuhao
 * @Date 2024/1/12 15:33
 * @Describe 用于对象检出检入日志记录切面
 */
@Aspect
@Component
@Slf4j
public class CheckObjectLogAspect {

    @Autowired
    private OperationLogEsService operationLogEsService;

    @Autowired
    private ApmSpaceAppService apmSpaceAppService;

    @Autowired
    private VersionObjectManageService versionObjectManageService;

    @Autowired
    private ObjectModelStandardI objectModelStandardI;

    @Resource
    private ValueChangeContext valueChangeContext;

    @Resource
    private IApmSpaceAppConfigDrivenService iApmSpaceAppConfigDrivenService;

    private ThreadLocal<Map<String, Object>>  threadLocal;

    @PostConstruct
    public void init() {
        threadLocal = new ThreadLocal<>();
    }


    @Before("execution(* com.transcend.plm.datadriven.apm.space.service.impl.ApmSpaceAppVersionDataDrivenServiceImpl.checkIn(..))")
    public void beforeCheckIn(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        ApmSpaceApp app = apmSpaceAppService.getByBid(String.valueOf(args[0]));
        MVersionObject mVersionObject = (MVersionObject) args[1];
        // 查询历史数据，将历史数据保存到ThreadLocal中
        MVersionObject oldMvObject = versionObjectManageService.getByDataBid(app.getModelCode(), mVersionObject.getDataBid());
        threadLocal.set(Maps.newHashMap(oldMvObject));
    }


    @After("execution(* com.transcend.plm.datadriven.apm.space.service.impl.ApmSpaceAppVersionDataDrivenServiceImpl.checkOut(..))")
    public void afterCheckOut(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String spaceAppBid = String.valueOf(args[0]);
        ApmSpaceApp app = apmSpaceAppService.getByBid(spaceAppBid);
        String instanceBid = String.valueOf(args[1]);
        MObject obj = objectModelStandardI.getByBid(app.getModelCode(), instanceBid);
        String dataBid = String.valueOf(obj.get(TranscendModelBaseFields.DATA_BID));
        String name = String.valueOf(obj.get(TranscendModelBaseFields.NAME));
        GenericLogAddParam logParam = GenericLogAddParam.builder().spaceBid(app.getSpaceBid())
                .modelCode(app.getModelCode())
                .instanceBid(dataBid)
                .logMsg("对实例数据 [ " + name + " ] 执行了检出操作")
                .build();
        operationLogEsService.genericSave(logParam);
    }


    @AfterReturning(pointcut = "execution(* com.transcend.plm.datadriven.apm.space.service.impl.ApmSpaceAppVersionDataDrivenServiceImpl.cancelCheckOut(..))",
                    returning = "mSpaceAppData")
    public void afterChancelCheckOut(JoinPoint joinPoint, MSpaceAppData mSpaceAppData) {
        Object[] args = joinPoint.getArgs();
        String spaceAppBid = String.valueOf(args[0]);
        ApmSpaceApp app = apmSpaceAppService.getByBid(spaceAppBid);
        String dataBid = String.valueOf(mSpaceAppData.get(TranscendModelBaseFields.DATA_BID));
        String name = String.valueOf(mSpaceAppData.getName());
        GenericLogAddParam logParam = GenericLogAddParam.builder().spaceBid(app.getSpaceBid())
                .type("log")
                .modelCode(app.getModelCode())
                .instanceBid(dataBid)
                .logMsg("对实例数据 [ " + name + " ] 执行了取消检出操作")
                .build();
        operationLogEsService.genericSave(logParam);
    }

    @After("execution(* com.transcend.plm.datadriven.apm.space.service.impl.ApmSpaceAppVersionDataDrivenServiceImpl.checkIn(..))")
    public void afterCheckIn(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            String spaceAppBid = String.valueOf(args[0]);
            // 新数据
            MVersionObject mVersionObject = (MVersionObject) args[1];
            // 老数据
            Map<String, Object> oldMvObject = threadLocal.get();
            // 查应用视图信息
            List<CfgViewMetaDto> cfgViewMetaList = iApmSpaceAppConfigDrivenService.baseViewGetMeteModels(String.valueOf(args[0]));
            List<String> changeList = cfgViewMetaList.stream()
                    .filter(data -> !Objects.equals(oldMvObject.get(data.getName()), mVersionObject.get(data.getName())))
                    .map(data -> {
                        OperationLogCfgViewMetaDto operationLogCfgViewMetaDto = new OperationLogCfgViewMetaDto();
                        BeanUtils.copyProperties(data, operationLogCfgViewMetaDto);
                        String oldValue = Optional.ofNullable(oldMvObject.get(data.getName())).map(Object::toString).orElse("");
                        String newValue = Optional.ofNullable(mVersionObject.get(data.getName())).map(Object::toString).orElse("");
                        return valueChangeContext.getServiceName(operationLogCfgViewMetaDto).getChangeValue(spaceAppBid, oldValue, newValue, operationLogCfgViewMetaDto);
                    }).collect(Collectors.toList());
            ApmSpaceApp app = apmSpaceAppService.getByBid(String.valueOf(args[0]));
            GenericLogAddParam logParam = GenericLogAddParam.builder().spaceBid(app.getSpaceBid())
                    .instanceBid(mVersionObject.getDataBid())
                    .logMsg("对实例数据[ " + oldMvObject.get(TranscendModelBaseFields.NAME) + " ]执行了检入操作，" + String.join("，", changeList))
                    .build();
            operationLogEsService.genericSave(logParam);
        } catch (Exception e) {
            log.error("保存检入操作日志失败，错误信息为：", e);
        } finally {
            threadLocal.remove();
        }
    }
}
