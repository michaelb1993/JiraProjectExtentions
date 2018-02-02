package com.michabond.scheduling.jobs;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.status.JobDetails;

import com.michabond.ao.accessor.SubscriptionDao;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Implements the job scheduling service as well as the {@code JobRunner} implementation.
 * <p>
 * A few notes about this example:
 * </p>
 * <ul>
 * <li>This plugin logs heavily at the {@code INFO} level to help developers follow what is happening and when.
 *      Plugin developers should <strong>not</strong> log this verbosely.  If it isn't something that would help
 *      a system administrator or support engineer track down a problem, it should be logged at {@code DEBUG}
 *      level, if it is logged at all.</li>
 * <li>Note that the job parameters are only used to hold small, {@code Serializable} data that would be
 *      meaningful to every node in the cluster &mdash; in this case, the ID of the active object that
 *      the job is going to be working with.  In particular, note that components like managers, services,
 *      and DAOs do not belong in it.  Inject them into your {@code JobRunner} implementation, instead.
 *      Your local component is not something that other nodes of the cluster can normally share, and it
 *      would take some very ugly serialization hacks to make that work.</li>
 * <li>Similarly, although you may include your plugin's own {@code Serializable} data types in the parameter
 *      map, be aware that this will break if the serialized form changes.  Your plugin can detect this by
 *      looking up its own jobs during initialization and verifying them.  If deserialization is broken,
 *      then its jobs will return {@code false} for {@link JobDetails#isRunnable()} <strong>even after the
 *      {@code JobRunner} is registered</strong>.  This problem can be avoided by using only simple Java
 *      types like {@code Long}, {@code String}, {@code ArrayList}, and {@code HashMap} in the parameter
 *      map.</li>
 * </ul>
 *
 * @since v1.0
 */
@Named
public class DBGarbageCollector implements JobRunner {

    private static final Logger LOGGER = Logger.getLogger(com.michabond.services.DBGarbageCollector.class);

    private SubscriptionDao subscriptionDao;

    @Inject
    public DBGarbageCollector(final SubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;

        LOGGER.info("Job runner instance created");
    }

    @Override
    public JobRunnerResponse runJob(JobRunnerRequest request) {
        try {
            LOGGER.warn("Collecting garbage ..");
            this.subscriptionDao.collectGarbage();
            LOGGER.warn("Finished collecting garbage :)");
            return JobRunnerResponse.success();
        }
        catch (Exception e) {
            return JobRunnerResponse.failed(e);
        }
    }
}
