package com.transcend.plm.datadriven.infrastructure.draft.repository;

import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.infrastructure.draft.po.DraftPO;
import com.transcend.plm.datadriven.infrastructure.draft.repository.mapper.DraftMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Repository
public class DraftRepository {

    @Resource
    private DraftMapper draftMapper;

    /**
     * @param draftDataPO
     * @return boolean
     */
    public boolean saveDraftData(DraftPO draftDataPO) {
        int res = draftMapper.saveDraftData(SsoHelper.getTenantCode(), draftDataPO);
        return res > 0;
    }

    /**
     * @param dataBid
     * @return boolean
     */
    public boolean deleteDraftByDataBid(String dataBid) {
        int res = draftMapper.deleteDraftByDataBid(SsoHelper.getTenantCode(), dataBid);
        return res > 0;
    }

    public boolean deleteDraftByBid(String bid, String tenantId) {
        int res = draftMapper.deleteDraftByBid(bid, tenantId);
        return res > 0;
    }

    public List<DraftPO> listDraftData(DraftPO draftDataDTO) {
        return draftMapper.listDraftData(
                SsoHelper.getTenantCode(),
                draftDataDTO);
    }

    /**
     * @param dataBid
     * @return {@link DraftPO }
     */
    public DraftPO getByDataBid(String dataBid) {
        return draftMapper.getByDataBid(SsoHelper.getTenantCode(), dataBid);
    }

    /**
     * 创建一个方法，根据bid查询草稿数据
     */
    public DraftPO getByBid(String bid) {
        return draftMapper.getByBid(SsoHelper.getTenantCode(),bid);
    }

    public List<DraftPO> getByDataBids(List<String> dataBids) {
        return draftMapper.getByDataBids(SsoHelper.getTenantCode(), dataBids);
    }

    public List<DraftPO> getByBids(List<String> bids) {
        return draftMapper.getByBids(SsoHelper.getTenantCode(), bids);
    }

}
