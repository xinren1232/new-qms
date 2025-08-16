package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * 对象版本模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
public class MVersionObject extends MObject {

    @ApiModelProperty("数据BID")
    private String dataBid;

    @ApiModelProperty("版本")
    private Integer version;

    @ApiModelProperty("修订版本")
    private String revision;

    @ApiModelProperty("检出人")
    private String checkoutBy;

    @ApiModelProperty("检出时间")
    private Date checkoutTime;

    @ApiModelProperty("检入描述")
    private String checkinDescription;

    /**
     * @return {@link String }
     */
    public String getCheckoutBy() {
        return (String) this.get(VersionObjectEnum.CHECKOUT_BY.getCode());
    }

    /**
     * @param checkoutBy
     */
    public void setCheckoutBy(String checkoutBy) {
        this.put(VersionObjectEnum.CHECKOUT_BY.getCode(), checkoutBy);
    }

    /**
     * @return {@link String }
     */
    public String getCheckinDescription() {
        return (String) this.get(VersionObjectEnum.CHECKIN_DESCRIPTION.getCode());
    }

    /**
     * @param checkinDescription
     */
    public void setCheckinDescription(String checkinDescription) {
        this.put(VersionObjectEnum.CHECKIN_DESCRIPTION.getCode(), checkinDescription);
    }

    /**
     * @return {@link String }
     */
    public String getDataBid() {
        return (String) this.get(VersionObjectEnum.DATA_BID.getCode());
    }

    /**
     * @param dataBid
     */
    public void setDataBid(String dataBid) {
        this.put(VersionObjectEnum.DATA_BID.getCode(), dataBid);
    }

    /**
     * @return {@link Integer }
     */
    public Integer getVersion() {
        return this.get(VersionObjectEnum.VERSION.getCode()) == null? null: Integer.parseInt(this.get(VersionObjectEnum.VERSION.getCode()).toString());
    }

    /**
     * @param version
     */
    public void setVersion(Integer version) {
        this.put(VersionObjectEnum.VERSION.getCode(), version);
    }

    /**
     * @return {@link String }
     */
    public String getRevision() {
        return (String) this.get(VersionObjectEnum.REVISION.getCode());
    }

    /**
     * @param revision
     */
    public void setRevision(String revision) {
        this.put(VersionObjectEnum.REVISION.getCode(), revision);
    }

    /**
     * @return {@link Date }
     */
    public Date getCheckoutTime() {
        return (Date) this.get(VersionObjectEnum.CHECKOUT_TIME.getCode());
    }

    /**
     * @param checkoutTime
     */
    public void setCheckoutTime(Date checkoutTime) {
        this.put(VersionObjectEnum.CHECKOUT_TIME.getCode(), checkoutTime);
    }
}
