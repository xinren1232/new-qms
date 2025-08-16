package com.transcend.plm.configcenter.menuapp.domain.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.common.constant.RoleConst;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.menuapp.infrastructure.repository.CfgMenuAppRepository;
import com.transcend.plm.configcenter.menuapp.pojo.CfgMenuAppConverter;
import com.transcend.plm.configcenter.menuapp.pojo.dto.CfgMenuAppDto;
import com.transcend.plm.configcenter.menuapp.pojo.qo.CfgMenuAppQo;
import com.transcend.plm.configcenter.menuapp.pojo.vo.CfgMenuAppVo;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:38
 **/
@Service
public class CfgMenuAppDomainService {
    @Resource
    private CfgMenuAppRepository repository;

    public CfgMenuAppVo save(CfgMenuAppDto dto) {
        Assert.notNull(dto, "CfgMenuApp is null");
        long count = repository.getCount(dto.getTypeCode(),dto.getTypeValue());
        if(count > 0){
            throw new BusinessException("已经存在相同类型的应用");
        }
        return repository.save(dto);
    }

    public CfgMenuAppVo update(CfgMenuAppDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "CfgMenuApp is null");
        Assert.hasText(cfgAttributeDto.getBid(), "CfgMenuApp bid is blank");
        return repository.update(cfgAttributeDto);
    }

    public CfgMenuAppVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    public PagedResult<CfgMenuAppVo> page(BaseRequest<CfgMenuAppQo> pageQo) {
        return repository.page(pageQo);
    }


    /**
     * 树查询
     *
     * @return
     */
    public List<CfgMenuAppVo> tree() {
        // 查询所有标准角色
        List<CfgMenuAppVo> cfgRoleVos = repository.listAll();
        // 组装树 时间复杂度需要2N，不能是 N*N
        // 根节点集合
        return convert2tree(cfgRoleVos);
    }

    /**
     * 组装树 时间复杂度需要2N，不能是 N*N
     *
     * @param cfgRoleVos 角色信息
     * @return
     */
    @NotNull
    private List<CfgMenuAppVo> convert2tree(List<? extends CfgMenuAppVo> cfgRoleVos) {
        List<CfgMenuAppVo> rootCfgRoleVos = Lists.newArrayList();
        // 父bid为key,子列表为value
        Map<String, List<CfgMenuAppVo>> parentKeyChildrenMap = Maps.newHashMap();
        cfgRoleVos.forEach(cfgRoleVo -> {
            // 父bid
            String parentBid = cfgRoleVo.getParentBid();
            if (CommonConst.ROLE_TREE_DEFAULT_ROOT_BID.equals(parentBid)) {
                // a.收集根节点
                rootCfgRoleVos.add(cfgRoleVo);
            } else {
                // b.收集父bid为key,子列表为value
                List<CfgMenuAppVo> children = parentKeyChildrenMap.get(parentBid);
                if(CollectionUtils.isEmpty(children)){
                    children = Lists.newArrayList();
                    parentKeyChildrenMap.put(parentBid, children);
                }
                children.add(cfgRoleVo);
            }
        });
        recursiveSetChildren(rootCfgRoleVos, parentKeyChildrenMap);
        return rootCfgRoleVos.stream().sorted(Comparator.comparing(CfgMenuAppVo::getSort)).collect(Collectors.toList());
    }

    /**
     * 递归设置子
     */
    private void recursiveSetChildren(List<CfgMenuAppVo> roots,
                                      Map<String, List<CfgMenuAppVo>> parentCodeAndChildrenMap) {
        if (CollectionUtils.isEmpty(roots)) {
            return;
        }
        roots.forEach(root -> {
            String parentCode = root.getBid();
            List<CfgMenuAppVo> children = parentCodeAndChildrenMap.get(parentCode);
            if (CollectionUtil.isNotEmpty(children)) {
                children.sort(Comparator.comparing(CfgMenuAppVo::getSort));
            }
            root.setChildren(children);
            this.recursiveSetChildren(children, parentCodeAndChildrenMap);
        });
    }

    public Boolean changeEnableFlag(String bid, Integer enableFlag) {
        CfgMenuAppVo vo = getByBid(bid);
        vo.setEnableFlag(enableFlag);
        CfgMenuAppDto dto = CfgMenuAppConverter.INSTANCE.vo2dto(vo);
        update(dto);
        return true;
    }

    public Boolean logicalDelete(String bid) {
        return repository.logicalDeleteByBid(bid);
    }

}
