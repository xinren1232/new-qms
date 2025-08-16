package com.transcend.plm.alm.demandmanagement.service;

public interface RrDemandHistoryDomainHandleService {
    boolean handleDomain();

    boolean handleIrDomain();

    boolean handleRrModule();

    boolean handleIrModule();

    boolean handleSrModule();

    boolean handleArModule();

    boolean handleArDomain();

    boolean handleSrDomain();

    boolean handleIrOsVersion();
}
