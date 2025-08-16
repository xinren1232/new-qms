package com.transcend.plm.datadriven.common.util;

import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户加工数据，将parentBid没有匹配上的数据，parentBid设置为0
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
public class ParentBidHandler {

    /**
     * @param mSpaceAppDataList
     * @return {@link List }<{@link MObjectTree }>
     */
    public static List<MObjectTree> handleMObjectTree(List<MObjectTree> mSpaceAppDataList){
        if(CollectionUtils.isNotEmpty(mSpaceAppDataList)){
            Map<String,String> bidMap = mSpaceAppDataList.stream().collect(Collectors.toMap(MObjectTree::getBid,MObjectTree::getBid));
            for(MObjectTree mSpaceAppData : mSpaceAppDataList){
                if(!bidMap.containsKey(mSpaceAppData.get(TranscendModelBaseFields.PARENT_BID)+"")){
                    mSpaceAppData.put(TranscendModelBaseFields.PARENT_BID,"0");
                }
            }
        }
        return mSpaceAppDataList;
    }

}
