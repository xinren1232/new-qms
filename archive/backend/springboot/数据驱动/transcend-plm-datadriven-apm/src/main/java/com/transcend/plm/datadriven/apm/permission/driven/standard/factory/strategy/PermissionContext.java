package com.transcend.plm.datadriven.apm.permission.driven.standard.factory.strategy;

import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.plm.datadriven.apm.permission.driven.standard.factory.IPermissionFactory;
import com.transcend.plm.datadriven.apm.permission.driven.standard.factory.category.AlmPermissionFactory;
import com.transcend.plm.datadriven.apm.permission.driven.standard.factory.category.PlmPermissionFactory;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author unknown
 * 权限上下文
 */
public class PermissionContext {

    @Getter
    private static final String almModel = "alm";
    @Getter
    private static final String plmModel = "plm";

    /**
     * 简单工厂，维护多个策略工厂
     */
    private final static Map<String, IPermissionFactory> permissionFactoryMap = new HashMap<>();
    static {
        permissionFactoryMap.put(getAlmModel(), new PlmPermissionFactory());
        permissionFactoryMap.put(getPlmModel(), new AlmPermissionFactory());
    }

    public static IPermissionFactory getFactory(String factory){
        IPermissionFactory iPermissionFactory = permissionFactoryMap.get(factory);
        if (null == iPermissionFactory) {
            throw new TranscendBizException("找不到权限工厂，请检查！");
        }
        return iPermissionFactory;
    }

    /**
     * TODO 未来根据空间或者整个租户给到是哪种权限类型
     * @param spaceAppBid
     * @return
     */
    public static IPermissionFactory getFactoryBySpaceBid(String spaceBid){
        IPermissionFactory iPermissionFactory = permissionFactoryMap.get(getPlmModel());
        if (null == iPermissionFactory) {
            throw new TranscendBizException("找不到权限工厂，请检查！");
        }
        return iPermissionFactory;
    }

    /**
     * TODO 未来根据租户给到是哪种权限类型
     * @return
     */
    public static IPermissionFactory getFactoryByTenant() {
        return permissionFactoryMap.get(getPlmModel());
    }
}
