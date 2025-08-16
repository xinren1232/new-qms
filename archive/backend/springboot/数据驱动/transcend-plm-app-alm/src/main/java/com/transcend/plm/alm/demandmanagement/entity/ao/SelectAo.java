package com.transcend.plm.alm.demandmanagement.entity.ao;

import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author bin.yin
 * @description: RR需求选取领域AO
 * @version:
 * @date 2024/06/21 18:09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectAo {
    /**
     * 级联选择bid list
     */
    private List<List<String>> selectedList;

    /**
     * 用于匹配名字,省去再查询一次
     */
    private List<SelectVo> nameList;
}
