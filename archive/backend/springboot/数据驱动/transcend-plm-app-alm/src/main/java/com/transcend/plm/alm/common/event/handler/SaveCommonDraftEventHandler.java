package com.transcend.plm.alm.common.event.handler;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.google.common.collect.Lists;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.configcenter.api.feign.LifeCycleFeignClient;
import com.transcend.plm.configcenter.api.model.lifecycle.dto.TemplateDto;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.CfgLifeCycleTemplateNodeVo;
import com.transcend.plm.datadriven.api.model.BatchUpdateBO;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.enums.CommonEnum;
import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractSaveCommonDraftEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmStateVO;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.exception.BusinessException;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description: (用一句话描述该文件做什么)
 * 保存草稿数据事件处理器
 * @author sgx
 * date 2024/6/21 19:08
 * @version V1.0
 */

@Component
public class SaveCommonDraftEventHandler extends AbstractSaveCommonDraftEventHandler {
    @Resource
    private LifeCycleFeignClient lifeCycleFeignClient;

    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    @Resource
    private DemandManagementProperties demandManagementProperties;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Override
    public AddEventHandlerParam preHandle(AddEventHandlerParam param) {
        // 查询应用的流程是否有草稿状态
        String spaceAppBid = param.getApmSpaceApp().getBid();
        ApmLifeCycleStateVO apmLifeCycleStateVO = apmSpaceAppConfigManageService.getLifeCycleState(spaceAppBid);
        List<ApmStateVO> apmStateVOList = null;
        if (apmLifeCycleStateVO != null) {
            apmStateVOList = apmLifeCycleStateVO.getApmStateVOList().stream().filter(nodeVo -> CommonEnum.DRAFT.getCode().equals(nodeVo.getLifeCycleCode())).collect(Collectors.toList());
        }
        // 校验生命周期是否有草稿状态
        String modelCode = param.getApmSpaceApp().getModelCode();
        TemplateDto templateDto = new TemplateDto();
        templateDto.setModelCode(modelCode);
        List<CfgLifeCycleTemplateNodeVo> nodeVos = lifeCycleFeignClient.getTemplateNodesSorted(templateDto)
                .getCheckExceptionData();
        Assert.notNull(nodeVos, "对象生命周期没有设置，请先设置生命周期");
        List<CfgLifeCycleTemplateNodeVo> drafs = nodeVos.stream().filter(nodeVo -> CommonEnum.DRAFT.getCode().equals(nodeVo.getLifeCycleCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(drafs) && CollectionUtils.isEmpty(apmStateVOList)   ) {
            throw new BusinessException("对象生命周期没有草稿状态，请先设置草稿状态");
        }
        return super.preHandle(param);
    }

    @Override
    public MSpaceAppData postHandle(AddEventHandlerParam param, MSpaceAppData result) {
        String spaceBid = param.getSpaceBid();
        String spaceAppBid = param.getApmSpaceApp().getBid();
        // 提交时已有BID
        String newBid = param.getMSpaceAppData().getBid();
        // RR 和IR 时 更新领域组件
        if (TranscendModel.RR.getCode().equals(param.getApmSpaceApp().getModelCode()) || TranscendModel.IR.getCode().equals(param.getApmSpaceApp().getModelCode())) {
            Set<String> domainBids = Sets.newHashSet();
            Set<String> domainUsers = Sets.newHashSet();
            Set<String> muduleBids = Sets.newHashSet();
            Set<String> productUsers = Sets.newHashSet();
            AtomicReference<String> leaderDomain = new AtomicReference<>();
            //领域组件
            String domainComponentModelCode = TranscendModel.DOMAIN_COMPONENT.getCode();
            //领域组件的数据
            List<MObject> domainComponentList = queryRelData(spaceBid, newBid);
            List<BatchUpdateBO<MSpaceAppData>> batchUpdateBoList = Lists.newArrayList();
            domainComponentList.forEach(m -> {
                MSpaceAppData mSpaceAppData = MSpaceAppData.buildFrom(m, spaceBid, spaceAppBid);
                if (TranscendModel.DOMAIN.getCode().equals((String) m.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()))) {
                    // 领域
                    domainBids.add((String) m.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                    domainUsers.add((String) m.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
                    Integer isLeadDomain = (Integer) m.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode());
                    if (Integer.valueOf(1).equals(isLeadDomain)) {
                        leaderDomain.set((String) m.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                    }

                } else if (TranscendModel.MODULE.getCode().equals((String) m.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()))) {
                    // 模块
                    muduleBids.add((String) m.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                    productUsers.add((String) m.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
                }
                mSpaceAppData.put(DemandManagementEnum.RR_BID.getCode(), newBid);
                QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                queryWrapper.setCondition("=");
                queryWrapper.setValue(m.getBid());
                BatchUpdateBO<MSpaceAppData> batchUpdateBO = new BatchUpdateBO<>();
                batchUpdateBO.setBaseData(mSpaceAppData);
                batchUpdateBO.setWrappers(Collections.singletonList(queryWrapper));
                batchUpdateBoList.add(batchUpdateBO);
            });
            if (CollectionUtils.isNotEmpty(batchUpdateBoList)) {
                objectModelCrudI.batchUpdateByQueryWrapper(domainComponentModelCode, batchUpdateBoList, false);
            }
            MSpaceAppData appData = new MSpaceAppData();
            appData.setBid(newBid);
            //领域IDS
            appData.put(DemandManagementEnum.PRODUCT_AREA.getCode(), domainBids);
            // 主导领域
            appData.put(DemandManagementEnum.ONWENR.getCode(), leaderDomain.get());
            // 领域负责人
            appData.put(DemandManagementEnum.PRODUCT_MANAGER.getCode(), domainUsers);
            // 模块IDS
            appData.put(DemandManagementEnum.BELONG_MODULE.getCode(), muduleBids);
            // 产品负责人
            appData.put(DemandManagementEnum.PERSON_RESPONSIBLE.getCode(), productUsers);
            try {
                iBaseApmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid,newBid,appData);
            } catch (Exception e) {
                // 不处理异常
            }
        }
        return super.postHandle(param, result);
    }



    private List<MObject> queryRelData(String spaceBid, String rrBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SPACE_BID.getColumn(), spaceBid).and().eq(DemandManagementEnum.RR_BID.getColumn(), rrBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), queryWrappers);
    }


    @Override
    public boolean isMatch(AddEventHandlerParam param) {
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
