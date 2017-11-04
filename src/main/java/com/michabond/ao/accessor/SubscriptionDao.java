package com.michabond.ao.accessor;

import com.michabond.rest.subscription.SubscriptionResourceModel;

public interface SubscriptionDao {

    void collectGarbage();

    void addSubscription(SubscriptionResourceModel params);
}
