package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.collection.CollUtil;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.constant.NumberConstant;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.*;

/**
 * @Describe 需求管理生成SR编码事件处理器
 * @Author yuhao.qiu
 * @Date 2024/6/21
 */
@Slf4j
@Component
public class GenerateCodeRelAddEventHandler extends AbstractRelationAddEventHandler {

    private static final String IR_OBJ_BID = "1253649025684303872";

    @Autowired
    private ApmSpaceAppService apmSpaceAppService;

    @Autowired
    private ObjectModelStandardI objectModelStandardI;

    @Autowired
    private DemandManagementProperties demandManagementProperties;


    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        // 生成SR需求编码 规则 SR-YYMMDDXXX-YYY 其中YYMMDDXXX是IR的编码，YYY是SR的序号 示例：SR-210621001-001
        String resultCode = generateCode(param);
        List<MSpaceAppData> targetMObjects = param.getAddExpandAo().getTargetMObjects();
        // 因为SR实例只能通过新增的方式添加 所以只有一个 直接取第一个
        MSpaceAppData appData = targetMObjects.get(0);
        appData.put(REQUIREMENT_CODING, resultCode);
        return super.preHandle(param);
    }

    /**
     * 生成编码
     *
     * @param param 事件参数
     * @return 编码
     */
    private String generateCode(RelationAddEventHandlerParam param) {
        StringBuilder resultCodeBuilder = new StringBuilder(SR_PREFIX);
        resultCodeBuilder.append(DASH);
        // 1. 通过IR的bid获取IR的编码
        String sourceSpaceAppBid = param.getAddExpandAo().getSourceSpaceAppBid();
        ApmSpaceApp sourceApp = apmSpaceAppService.getByBid(sourceSpaceAppBid);
        if (sourceApp == null) {
            throw new TranscendBizException("IR应用不存在");
        }
        String sourceModelCode = sourceApp.getModelCode();
        MObject sourceIrIns = objectModelStandardI.getByBid(sourceModelCode, param.getAddExpandAo().getSourceBid());
        if (sourceIrIns == null) {
            throw new TranscendBizException("IR实例不存在");
        }
        String irCode = String.valueOf(sourceIrIns.get(REQUIREMENT_CODING));
        if (StringUtils.isBlank(irCode)) {
            throw new TranscendBizException("IR编码为空");
        }
        // 2. 切分IR编码 获取YYMMDDXXX部分
        resultCodeBuilder.append(irCode.split(DASH)[1]);
        resultCodeBuilder.append(DASH);
        // 3. 查询当前IR-SR关系下面是否有存在已添加的SR
        RelationMObject relationMObject = RelationMObject.builder()
                .relationModelCode(param.getAddExpandAo().getRelationModelCode())
                .sourceBid(param.getAddExpandAo().getSourceBid()).build();
        List<MObject> irAndSrRel = objectModelStandardI.listOnlyRelationMObjects(relationMObject);
        if (CollUtil.isEmpty(irAndSrRel)) {
            // 如果为空
            resultCodeBuilder.append("001");
        } else {
            // 如果不为空
            // 新增进去的SR编码为关系数量+1
            resultCodeBuilder.append(handleSerialNumber(irAndSrRel.size() + 1));
        }
        return resultCodeBuilder.toString();
    }

    /**
     * 处理尾部编号
     * @param currentSerialNumber 自增序列值
     * @return 尾部编号
     */
    public String handleSerialNumber(int currentSerialNumber) {
        if (currentSerialNumber < NumberConstant.TEN) {
            return "00" + currentSerialNumber;
        } else if (currentSerialNumber < NumberConstant.HUNDRED) {
            return "0" + currentSerialNumber;
        } else {
            return String.valueOf(currentSerialNumber);
        }
    }

    /**
     * 如果关系新增时且IR实例关联需要新建SR对象实例，则返回true
     *
     * @param param 入参
     * @return boolean
     */
    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        String objBid = param.getObjBid();
        return IR_OBJ_BID.equals(objBid) &&
                param.getAddExpandAo().getRelationModelCode().equals(TranscendModel.RELATION_IR_SR.getCode());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
