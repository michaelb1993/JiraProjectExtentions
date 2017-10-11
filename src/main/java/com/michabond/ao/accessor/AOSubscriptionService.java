package com.michabond.ao.accessor;

import com.michabond.rest.subscription.SubscriptionResourceModel;

public interface AOSubscriptionService extends AOService {

    void addSubscription(SubscriptionResourceModel params);
}
