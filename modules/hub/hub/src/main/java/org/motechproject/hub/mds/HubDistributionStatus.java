package org.motechproject.hub.mds;

// Generated Apr 21, 2014 1:51:45 PM by Hibernate Tools 3.4.0.CR1

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * HubDistributionStatus generated by hbm2java
 */
@Entity
public class HubDistributionStatus implements java.io.Serializable {

    private static final long serialVersionUID = -6224225769462872750L;

    @Field(required = true)
    private Integer distributionStatusId;

    public Integer getDistributionStatusId() {
        return distributionStatusId;
    }

    public void setDistributionStatusId(Integer distributionStatusId) {
        this.distributionStatusId = distributionStatusId;
    }

    public String getDistributionStatusCode() {
        return distributionStatusCode;
    }

    public void setDistributionStatusCode(String distributionStatusCode) {
        this.distributionStatusCode = distributionStatusCode;
    }

    @Field(required = true)
    private String distributionStatusCode;

    public HubDistributionStatus() {
    }

    public HubDistributionStatus(Integer distributionStatusId,
            String distributionStatusCode) {
        this.distributionStatusId = distributionStatusId;
        this.distributionStatusCode = distributionStatusCode;
    }

}