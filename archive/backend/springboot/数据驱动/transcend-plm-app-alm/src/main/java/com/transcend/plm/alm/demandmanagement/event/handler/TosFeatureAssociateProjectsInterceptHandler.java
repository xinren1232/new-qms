package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractAddEventHandler;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Tos特性关联项目拦截处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/12 16:00
 */
@Log4j2
public class TosFeatureAssociateProjectsInterceptHandler {


    @Component
    @AllArgsConstructor
    public static class AddIntercept extends AbstractAddEventHandler {
        private final ObjectModelStandardI<MObject> objectModelStandardI;

        @Override
        public AddEventHandlerParam preHandle(AddEventHandlerParam param) {
            TosFeatureAssociateProjectsInterceptHandler.repeatAssociateVerify(objectModelStandardI, param.getMSpaceAppData());
            return super.preHandle(param);
        }


        @Override
        public boolean isMatch(AddEventHandlerParam param) {
            return TosFeatureAssociateProjectsInterceptHandler.isMatch(param.getApmSpaceApp());
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    @Component
    @AllArgsConstructor
    public static class UpdateIntercept extends AbstractUpdateEventHandler {
        private final ObjectModelStandardI<MObject> objectModelStandardI;

        @Override
        public UpdateEventHandlerParam preHandle(UpdateEventHandlerParam param) {
            MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
            if (mSpaceAppData == null) {
                return param;
            }
            mSpaceAppData.setBid(param.getBid());
            TosFeatureAssociateProjectsInterceptHandler.repeatAssociateVerify(objectModelStandardI, mSpaceAppData);
            return super.preHandle(param);
        }

        @Override
        public boolean isMatch(UpdateEventHandlerParam param) {
            return TosFeatureAssociateProjectsInterceptHandler.isMatch(param.getApmSpaceApp());
        }

        @Override
        public int getOrder() {
            return 0;
        }


    }

    @NotNull
    private static Boolean isMatch(ApmSpaceApp spaceApp) {
        return Optional.ofNullable(spaceApp)
                .map(ApmSpaceApp::getModelCode)
                .map(modelCode -> TranscendModel.matchCode(modelCode, TranscendModel.TOS_VERSION_FEATURE_TREE))
                .orElse(false);
    }

    /**
     * 重复关联校验
     *
     * @param objectModelStandardI 对象模型
     * @param appData              应用数据
     */
    private static void repeatAssociateVerify(ObjectModelStandardI<MObject> objectModelStandardI, MSpaceAppData appData) {
        if (appData == null) {
            return;
        }
        TranscendObjectWrapper wrapper = new TranscendObjectWrapper(appData);
        String fieldName = "associateTOSProject";
        String projectBid = wrapper.getStr(fieldName);
        if (projectBid == null) {
            return;
        }

        QueryWrapper queryWrapper = new QueryWrapper().ne(BaseDataEnum.BID.getCode(), wrapper.getBid())
                .and().eq(fieldName, projectBid);

        int count = objectModelStandardI.count(
                TranscendModel.TOS_VERSION_FEATURE_TREE.getCode(), QueryWrapper.buildSqlQo(queryWrapper));
        Assert.isFalse(count > 0, "该项目已有关联的tOS版本特性，请勿重复关联");
    }

}
