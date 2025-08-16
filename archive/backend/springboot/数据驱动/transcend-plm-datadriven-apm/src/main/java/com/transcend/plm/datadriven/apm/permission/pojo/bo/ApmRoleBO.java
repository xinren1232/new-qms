package com.transcend.plm.datadriven.apm.permission.pojo.bo;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 角色BO
 * @createTime 2023-09-20 15:01:00
 */
@Data
public class ApmRoleBO implements ApmIdentity {
    private Integer id;

    /**
     *
     */
    private String bid;

    /**
     * 父级bid
     */
    private String pbid;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 域id
     */
    private String sphereBid;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 更新人
     */
    private String updatedBy;

    List<ApmRoleBO> children;
    List<ApmIdentity> relatedApmIdentityList;

    @Override
    public List<ApmUser> getApmUserList() {
        LinkedList<ApmUser> apmUserList = Lists.newLinkedList();
        for (ApmIdentity apmIdentity : relatedApmIdentityList) {
            apmUserList.addAll(apmIdentity.getApmUserList());
        }
        if (CollectionUtils.isEmpty(children)) {
            return Lists.newArrayList(apmUserList);
        }
        for (ApmRoleBO child : children) {
            apmUserList.addAll(child.getApmUserList());
        }
        return Lists.newArrayList(apmUserList);
    }
}
