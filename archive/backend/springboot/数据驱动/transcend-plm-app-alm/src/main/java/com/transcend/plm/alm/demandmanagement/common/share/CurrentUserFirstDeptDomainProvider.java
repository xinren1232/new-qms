package com.transcend.plm.alm.demandmanagement.common.share;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.vo.ThreeDeptVO;
import com.transcend.plm.datadriven.apm.common.share.CurrentUserDeptProvider;
import com.transcend.plm.datadriven.common.share.ContentProvider;
import com.transcend.plm.datadriven.common.share.RequestShareContext;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 当前用户一级部门邻域关联提供者
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 15:18
 */
@Component
public class CurrentUserFirstDeptDomainProvider implements ContentProvider<String> {

    @Resource
    private ObjectModelStandardI<MObject> objectModelCrudI;
    @Resource
    @Lazy
    private RequestShareContext context;

    @Override
    public String getContent() {
        ThreeDeptVO dept = context.getContent(CurrentUserDeptProvider.class);
        return Optional.ofNullable(dept).map(ThreeDeptVO::getFirstDeptId).map(id -> {
            QueryWrapper wrapper = new QueryWrapper().like("firstLevelDepartment", id);
            List<MObject> list = objectModelCrudI.list(TranscendModel.RM_RESPONSIBLE_PERSON.getCode(),
                    QueryWrapper.buildSqlQo(wrapper));
            if (list != null && !list.isEmpty()) {
                return new TranscendObjectWrapper(list.get(0)).getBid();
            }
            return null;
        }).orElse(null);

    }
}
