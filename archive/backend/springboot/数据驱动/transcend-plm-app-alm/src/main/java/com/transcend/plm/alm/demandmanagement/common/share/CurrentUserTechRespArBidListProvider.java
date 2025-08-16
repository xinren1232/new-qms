package com.transcend.plm.alm.demandmanagement.common.share;

import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.common.share.ContentProvider;
import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 当前登录登录用户技术负责的AR提供者
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/7/1 15:25
 */
@Component
@AllArgsConstructor
public class CurrentUserTechRespArBidListProvider implements ContentProvider<List<String>> {

    private final QueryMapper specialQueryMapper;

    @Override
    public List<String> getContent() {
        return specialQueryMapper.query(SsoHelper.getJobNumber());
    }

    @Mapper
    public interface QueryMapper {

        /**
         * 查询当前登录用户技术负责的AR
         *
         * @param jobNumber 工号
         * @return AR编号列表
         */
        @Select("select distinct rel.target_bid from transcend_model_sr sr " +
                "right join transcend_model_a0e rel on sr.bid = rel.source_bid " +
                "where sr.delete_flag = 0 and rel.delete_flag = 0 and sr.person_responsible like CONCAT('%', #{jobNumber}, '%')")
        List<String> query(@Param("jobNumber") String jobNumber);
    }

}
