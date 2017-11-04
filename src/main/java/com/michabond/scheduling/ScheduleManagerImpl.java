package com.michabond.scheduling;

import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;

import com.michabond.scheduling.Exceptions.SchedulingException;
import com.michabond.scheduling.contract.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Random;


@Named
public class ScheduleManagerImpl implements ScheduleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleManagerImpl.class);
    private static final Random RANDOM = new Random();

    // A minimum amount of time, in milliseconds, to wait before the job runs for the first time.  If you
    // are scheduling a RUN_ONCE_PER_CLUSTER job and both nodes start at once, there is a race condition
    // where both nodes can miss the fact that the other node is also scheduling the job.  If they both
    // try to schedule it at once, then it is possible for both nodes to successfully run the job.  Putting
    // in a small delay (15 seconds is recommended) before the job can possibly run for the first time
    // prevents this from happening because the second node will overwrite the first node's schedule before
    // it gets to run in all but the most extreme circumstances.  If your network latency is so high that
    // a 15 second delay is too long, then your cluster is likely to break for other reasons.
    private static final int MIN_DELAY = 15000;

    // The purpose of the jitter is to reduce the risk that your plugin will schedule itself to run at more
    // or less the same time as countless other plugin jobs do by waiting a random time period before the
    // first run.  This is important if your plugin is part of Atlassian's OnDemand offering, but not
    // necessary for other plugins.
    private static final int MAX_JITTER = 10000;

    // We will generate our own JobIds, both so that they will be meaningful and so that we can find them again
    // later.  This also makes it very unlikely that we would accidentally delete some other plugin's jobs,
    // because we are using our own class to namespace them.  Something like the plugin key would also make
    // sense.  If you are using Schedule.runOnce jobs, then you may be happy using an ID that is generated
    // for you using SchedulerService.scheduleJobWithGeneratedId.
    private static final String JOB_ID_PREFIX = "AwesomeJob for id=";


    private final SchedulerService schedulerService;

    @Inject
    public ScheduleManagerImpl(final SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    /**
     * This is an example of creating a schedule only if it does not already exist.  This is simplest and
     * quickest unless you have a good reason to think that the existing schedule needs to be replaced,
     * such as if the schedule interval is being changed or you need to change what information is
     * stored in the parameter map.
     *
     * @param contract The contract of the job we wish to schedule.
     * @param intervalInMillis How frequently to run.
     */
    @Override
    public void createScheduleIfAbsent(Contract contract, long intervalInMillis) throws SchedulingException {
        final JobId jobId = contract.getJobId();
        final JobDetails existing = schedulerService.getJobDetails(jobId);
        if (null == existing) {
            LOGGER.info("Schedule for jobId=" + jobId + " does not exist, so createScheduleIfAbsent will create it");
            createSchedule(contract, intervalInMillis);
        }
        else {
            LOGGER.info("Schedule for jobId=" + jobId +
                    " already exists, so createScheduleIfAbsent is not going to do anything: " + existing);
        }
    }

    /**
     * This will replace the existing schedule if it has to.
     *
     * @param contract The contract of the job we wish to schedule.
     * @param intervalInMillis How frequently to run.
     */
    @Override
    public void createOrUpdateSchedule(Contract contract, long intervalInMillis) throws SchedulingException
    {
        final JobId jobId = contract.getJobId();
        final JobDetails existing = schedulerService.getJobDetails(jobId);
        if (null == existing) {
            LOGGER.info("Schedule for jobId=" + jobId +
                    " does not exist, so createOrUpdateSchedule will create it normally");
        }
        else {
            LOGGER.info("Schedule for jobId=" + jobId +
                    " already exists, so createOrUpdateSchedule is removing the existing one first: " + existing);
        }
        createSchedule(contract, intervalInMillis);
    }

    private void createSchedule(Contract contract, long intervalInMillis) throws SchedulingException {
        final int jitter = RANDOM.nextInt(MAX_JITTER);
        final Date firstRun = new Date(System.currentTimeMillis() + MIN_DELAY + jitter);

        final JobConfig jobConfig = contract.getJobConfig()
                .withSchedule(Schedule.forInterval(intervalInMillis, firstRun));
        LOGGER.info("Scheduling job with jitter=" + jitter + ": " + jobConfig);

        final JobId jobId = contract.getJobId();
        try {
            final JobDetails existing = schedulerService.getJobDetails(jobId);
            if (null != existing) {
                LOGGER.info("We will be replacing an existing job with jobId=" + jobId + ": " + existing);
                // Note that we don't need to delete the existing job first; scheduleJob will replace the previous one
                // deleteAwesomeSchedule(existing);
            }

            schedulerService.scheduleJob(jobId, jobConfig);
            LOGGER.info("Successfully scheduled jobId=" + jobId);
        }
        catch (SchedulerServiceException e) {
            throw new SchedulingException("Unable to create schedule for job with jobId=" + jobId, e);
        }
    }

    @Override
    public void deleteSchedule(Contract contract) throws SchedulingException {
        final JobId jobId = contract.getJobId();
        final JobDetails existing = schedulerService.getJobDetails(jobId);

        // Some plugins may prefer to throw an exception if you attempt to delete a schedule that
        // does not exist, but idempotency is more polite.
        if (null == existing) {
            return;
        }

        // Why did we get asked to delete somebody else's job?!  Calculating the jobId directly
        // would probably be less trouble.
        if (!jobId.equals(existing.getJobId())) {
            throw new SchedulingException("JobId '" + existing.getJobId() + "' does not belong to me!");
        }
        schedulerService.unscheduleJob(jobId);
    }
}
