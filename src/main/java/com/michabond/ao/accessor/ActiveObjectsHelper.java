package com.michabond.ao.accessor;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.michabond.ao.Subscription;
import com.michabond.ao.User;
import com.michabond.rest.subscription.SubscriptionResourceModel;
import net.java.ao.DBParam;
import net.java.ao.Query;

import java.sql.Timestamp;


public class ActiveObjectsHelper {

    public static User createUser(ActiveObjects activeObjects, SubscriptionResourceModel params) {
        return activeObjects.create(User.class, new DBParam("NAME", params.getUsername()));
    }

    public static Subscription createSubscription(ActiveObjects activeObjects, SubscriptionResourceModel params) {
        return activeObjects.create(Subscription.class,
                new DBParam("SUBSCRIPTION_ID", params.getSubscriptionId()),
                new DBParam("COMPANY_NAME" , params.getCompanyName()),
                new DBParam("START_TIME", params.getSubscriptionStartTime()),
                new DBParam("EXPIRE_TIME", params.getSubscriptionExpireTime()));
    }

    public static User findUniqueUser(ActiveObjects activeObjects, String username) {
        User[] userRes = activeObjects.find(User.class, Query.select().where("NAME = ?", username));
        if ((userRes != null && userRes.length > 0)) {
            return userRes[0];
        }
        return null;
    }

    public static Subscription[] getExpiredSubscriptions(ActiveObjects activeObjects) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return activeObjects.find(Subscription.class, Query.select().where("EXPIRE_TIME < ?", now));
    }
}
