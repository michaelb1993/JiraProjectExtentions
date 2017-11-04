package com.michabond.services;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.service.AbstractService;
import com.michabond.ao.accessor.SubscriptionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBGarbageCollector extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBGarbageCollector.class);

    private SubscriptionDao subscriptionDao;

    public DBGarbageCollector() {
        this.subscriptionDao = ComponentAccessor.getOSGiComponentInstanceOfType(SubscriptionDao.class);
    }

    @Override
    public void init(com.opensymphony.module.propertyset.PropertySet props) throws ObjectConfigurationException {
        super.init(props);
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException {
        return getObjectConfiguration("db-garbage-collector-id", "services/db-garbage-collector.xml", null);
    }

    @Override
    public void run() {
        LOGGER.warn("Collecting garbage ..");
        this.subscriptionDao.collectGarbage();
        LOGGER.warn("Finished collecting garbage :)");
    }
}
