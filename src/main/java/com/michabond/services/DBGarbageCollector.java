package com.michabond.services;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.AbstractService;
import com.michabond.ao.accessor.AOSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class DBGarbageCollector extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBGarbageCollector.class);

    private AOSubscriptionService aoSubscriptionService;

    @Inject
    public DBGarbageCollector(AOSubscriptionService aoSubscriptionService) {
        this.aoSubscriptionService = aoSubscriptionService;
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException {
        return getObjectConfiguration("db-garbage-collector-id", "services/db-garbage-collector.xml", null);
    }

    @Override
    public void run() {
        LOGGER.warn("Start collecting..");
        this.aoSubscriptionService.collectGarbage();
        LOGGER.warn("Finished collecting..");
    }
}
