package com.transcend.plm.datadriven.apm.space.model;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MVersionObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 对象模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/24 17:44
 * @since 1.0
 */
@ApiModel("空间应用对象实例模型")
@Data
public class MSpaceAppData extends MObject {

    @ApiModelProperty("空间bid")
    private String spaceBid;

    @ApiModelProperty("空间应用bid")
    private String spaceAppBid;
    /**
     * 是否异步检查流程
     */
    private Boolean syanCheckFlow = true;

    public void setSyanCheckFlow(Boolean syanCheckFlow) {
        this.syanCheckFlow = syanCheckFlow;
    }

    public Boolean isSyanCheckFlow() {
        return syanCheckFlow;
    }

    public String getSpaceBid() {
        return (String) this.get(SpaceAppDataEnum.SPACE_BID.getCode());
    }

    public MSpaceAppData setSpaceBid(String modelCode) {
        this.put(SpaceAppDataEnum.SPACE_BID.getCode(), modelCode);
        return this;
    }

    public String getSpaceAppBid() {
        return (String) this.get(SpaceAppDataEnum.SPACE_APP_BID.getCode());
    }

    public MSpaceAppData setSpaceAppBid(String modelCode) {
        this.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), modelCode);
        return this;
    }

    /**
     * 从MObject实例生成MSpaceAppData实例
     */
    public static MSpaceAppData buildFrom(MObject mObject, String spaceBid, String spaceAppBid) {
        MSpaceAppData mSpaceAppData = new MSpaceAppData();
        mSpaceAppData.putAll(mObject);
        mSpaceAppData.setSpaceBid(spaceBid);
        mSpaceAppData.setSpaceAppBid(spaceAppBid);
        return mSpaceAppData;
    }

    public static MSpaceAppData buildFrom(MVersionObject mVersionObject, String spaceAppBid) {
        MSpaceAppData mSpaceAppData = new MSpaceAppData();
        mSpaceAppData.putAll(mVersionObject);
        mSpaceAppData.setSpaceAppBid(spaceAppBid);
        return mSpaceAppData;
    }
}
