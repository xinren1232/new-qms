package com.transcend.plm.datadriven.apm.space.utils;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class RelationUtils {

    @NotNull
    public static MObject collectRelationPo(String relationModelCode, String sourceBid, String sourceDataBid, String targetBid, String targetDataBid, String spaceBid, Long tenantId) {
        MObject relMObject = new MObject();
        relMObject.setBid(targetBid);
        relMObject.setCreatedBy(SsoHelper.getJobNumber());
        relMObject.setUpdatedBy(SsoHelper.getJobNumber());
        // 补充空间和空间应用bid
        relMObject.put(SpaceAppDataEnum.SPACE_BID.getCode(), spaceBid);
        relMObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), "0");
        relMObject.put(RelationEnum.SOURCE_BID.getCode(), sourceBid);
        relMObject.put(RelationEnum.TARGET_BID.getCode(), targetBid);
        relMObject.put(RelationEnum.SOURCE_DATA_BID.getCode(), sourceDataBid);
        relMObject.put(RelationEnum.TARGET_DATA_BID.getCode(), targetDataBid);
        relMObject.put("draft", false);
        relMObject.put(TranscendModelBaseFields.BID, SnowflakeIdWorker.nextIdStr());
        relMObject.put(TranscendModelBaseFields.DATA_BID, SnowflakeIdWorker.nextIdStr());
        relMObject.setModelCode(relationModelCode);
        relMObject.put(TranscendModelBaseFields.DATA_BID, SnowflakeIdWorker.nextIdStr());
        relMObject.setUpdatedTime(LocalDateTime.now());
        relMObject.setCreatedTime(LocalDateTime.now());
        relMObject.setEnableFlag(true);
        relMObject.setDeleteFlag(false);
        relMObject.setTenantId(tenantId);
        return relMObject;
    }
}
