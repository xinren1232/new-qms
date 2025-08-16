package com.transcend.plm.datadriven.notify.mapstruct;

import com.transcend.plm.datadriven.notify.domain.NotifyConfigOperate;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigOperateVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface NotifyConfigOperateConverter {
    NotifyConfigOperateConverter INSTANCE = Mappers.getMapper(NotifyConfigOperateConverter.class);

    /**
     * po 转 vo
     *
     * @param po {@link NotifyConfigOperate }
     * @return {@link NotifyConfigOperateVo }
     */
    NotifyConfigOperateVo po2Vo(NotifyConfigOperate po);

}
