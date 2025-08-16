package com.transcend.plm.datadriven.apm.dto;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class StateDataDriveDto {

    private String spaceAppBid;
    private String bid;
    private MSpaceAppData mSpaceAppData;
}
