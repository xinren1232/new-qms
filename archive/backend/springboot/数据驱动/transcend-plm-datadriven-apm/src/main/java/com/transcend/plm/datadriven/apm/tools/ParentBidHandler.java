package com.transcend.plm.datadriven.apm.tools;

import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户加工数据，将parentBid没有匹配上的数据，parentBid设置为0
 *
 * @author unknown
 */
public class ParentBidHandler {


    public static List<MSpaceAppData> handleMSpaceAppData(List<MSpaceAppData> mSpaceAppDataList) {
        if (CollectionUtils.isNotEmpty(mSpaceAppDataList)) {
            Map<String, String> bidMap = mSpaceAppDataList.stream().collect(Collectors.toMap(MSpaceAppData::getBid, MSpaceAppData::getBid, (k1, k2) -> k1));
            for (MSpaceAppData mSpaceAppData : mSpaceAppDataList) {
                if (!bidMap.containsKey(mSpaceAppData.get(TranscendModelBaseFields.PARENT_BID) + "")) {
                    mSpaceAppData.put(TranscendModelBaseFields.PARENT_BID, "0");
                }
            }
        }
        return mSpaceAppDataList;
    }


    public static List<MObjectTree> handleMObjectTree(List<MObjectTree> mSpaceAppDataList) {
        if (CollectionUtils.isNotEmpty(mSpaceAppDataList)) {
            Map<String, String> bidMap = mSpaceAppDataList.stream().collect(Collectors.toMap(MObjectTree::getBid, MObjectTree::getBid, (k1, k2) -> k1));
            for (MObjectTree mSpaceAppData : mSpaceAppDataList) {
                if (!bidMap.containsKey(mSpaceAppData.get(TranscendModelBaseFields.PARENT_BID) + "")) {
                    mSpaceAppData.put(TranscendModelBaseFields.PARENT_BID, "0");
                }
            }
        }
        return mSpaceAppDataList;
    }

}
