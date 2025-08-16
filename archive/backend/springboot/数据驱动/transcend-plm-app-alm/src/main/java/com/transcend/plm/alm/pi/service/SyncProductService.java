package com.transcend.plm.alm.pi.service;

import com.transcend.plm.alm.pi.dto.ProductDto;
import com.transcend.plm.alm.pi.dto.SyncDto;

public interface SyncProductService {

     boolean syncProduct(ProductDto productDto);

     boolean saveOrUpdate(SyncDto syncDto);

}
