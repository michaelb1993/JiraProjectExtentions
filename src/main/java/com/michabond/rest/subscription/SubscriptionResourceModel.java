package com.michabond.rest.subscription;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

@XmlRootElement(name = "subscription")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubscriptionResourceModel {

    @XmlElement
    private String companyName;

    @XmlElement
    private String username;

    @XmlElement
    private long subscriptionPeriod;

    @XmlElement
    private String subscriptionId;

    @XmlElement
    private Timestamp subscriptionStartTime;

    @XmlElement
    private Timestamp subscriptionExpireTime;

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getSubscriptionPeriod() {
        return this.subscriptionPeriod;
    }

    public void setSubscriptionPeriod(long subscriptionPeriod) {
        this.subscriptionPeriod = subscriptionPeriod;
    }

    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Timestamp getSubscriptionStartTime() {
        return this.subscriptionStartTime;
    }

    public void setSubscriptionStartTime(Timestamp subscriptionStartTime) {
        this.subscriptionStartTime = subscriptionStartTime;
    }

    public Timestamp getSubscriptionExpireTime() {
        return this.subscriptionExpireTime;
    }

    public void setSubscriptionExpireTime(Timestamp subscriptionExpireTime) {
        this.subscriptionExpireTime = subscriptionExpireTime;
    }
}
