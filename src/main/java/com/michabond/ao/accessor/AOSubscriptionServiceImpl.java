package com.michabond.ao.accessor;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.michabond.ao.Subscription;
import com.michabond.ao.User;
import com.michabond.rest.subscription.SubscriptionResourceModel;
import com.michabond.rest.subscription.exceptions.BadParametersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.UUID;

@ExportAsService({AOSubscriptionService.class})
@Named
public class AOSubscriptionServiceImpl implements AOSubscriptionService {

    private static final String EMPTY = "";
    private static final long SUBSCRIPTION_EXPIRATION_PERIOD = 60 * 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(AOSubscriptionServiceImpl.class);

    private ActiveObjects ao;

    @Inject
    public AOSubscriptionServiceImpl(ActiveObjects activeObjects) {
        this.ao = activeObjects;
    }

    @Override
    public void collectGarbage() {
        Subscription[] expired = ActiveObjectsHelper.getExpiredSubscriptions(this.ao);
        for (Subscription subscription : expired) {
            User user = subscription.getUser();
            this.ao.delete(subscription);
            LOGGER.info("Deleted subscription-ao, subscription-id: " + subscription.getSubscriptionId());
            deleteOrphanRelations(subscription, user);
        }
    }

    private void deleteOrphanRelations(Subscription subscription, User user) {
        if (null == user) {
            return;
        }
        Subscription[] userSubscriptions = user.getSubscriptions();
        if (null == userSubscriptions || 0 == userSubscriptions.length) {
            this.ao.delete(user);
            LOGGER.info("Deleted user-ao, username: " + user.getName());
        }
    }

    @Override
    public void addSubscription(SubscriptionResourceModel params) throws BadParametersException {
        validateParams(params);
        //
        // User
        //
        User user = ActiveObjectsHelper.findUniqueUser(this.ao, params.getUsername());
        if (user == null) {
            user = ActiveObjectsHelper.createUser(this.ao, params);
            user.save();
            LOGGER.info("Created new user-ao, username: " + params.getUsername());
        }
        //
        // Subscription
        //
        params.setSubscriptionPeriod(SUBSCRIPTION_EXPIRATION_PERIOD);
        params.setSubscriptionId(UUID.randomUUID().toString());
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expireTime = new Timestamp(now.getTime() + params.getSubscriptionPeriod());
        params.setSubscriptionStartTime(now);
        params.setSubscriptionExpireTime(expireTime);
        Subscription subscription = ActiveObjectsHelper.createSubscription(this.ao, params);
        // Relate to user
        subscription.setUser(user);
        subscription.save();
        LOGGER.info("Created new subscription-ao, companyName: " + params.getCompanyName());
    }

    private static void validateParams(SubscriptionResourceModel params) throws BadParametersException {
        String username = params.getUsername();
        String companyName = params.getCompanyName();
        if (null == username || username.equals(EMPTY)) {
            throw new BadParametersException("Bad username - " + username);
        }
        if (null == companyName || companyName.equals(EMPTY)) {
            throw new BadParametersException("Bad companyName - " + companyName);
        }
    }
}
