package com.michabond.ao;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Unique;

import java.sql.Timestamp;

public interface Subscription extends Entity {

    @Unique
    @NotNull
    String getSubscriptionId();
    void setSubscriptionId(String subscriptionId);

    @NotNull
    String getCompanyName();
    void setCompanyName(String companyName);

    @NotNull
    Timestamp getStartTime();
    void setStartTime(Timestamp startTime);

    @NotNull
    Timestamp getExpireTime();
    void setExpireTime(Timestamp expireTime);

    User getUser();
    void setUser(User user);
}
