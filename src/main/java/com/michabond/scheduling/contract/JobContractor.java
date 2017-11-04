package com.michabond.scheduling.contract;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.michabond.scheduling.jobs.DBGarbageCollector;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashMap;

import static com.atlassian.scheduler.config.RunMode.RUN_LOCALLY;
import static com.atlassian.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;


@Named
public class JobContractor {

    /**
     * Class related
     */

    public static final long DEFAULT_JOB_INTERVAL = 60 * 1000L;

    //
    //  DB garbage collector job
    //
    // Job runner key
    private static final JobRunnerKey DBGC_JOB_RUNNER_KEY = JobRunnerKey.of(DBGarbageCollector.class.getName());
    // Job config
    private static final JobConfig DBGC_JOB_CONFIG = JobConfig.forJobRunnerKey(DBGC_JOB_RUNNER_KEY)
            .withRunMode(RUN_ONCE_PER_CLUSTER);
    // Job id
    private static final String DBGC_JOB_ID_STR = "DBGC single job";
    private static final JobId DBGC_JOB_ID = JobId.of(DBGC_JOB_ID_STR);

    // DB garbage collector job contract
    public static final Contract DBGC_CONTRACT = new Contract(DBGC_JOB_CONFIG, DBGC_JOB_ID);

    /**
     * Instance related
     */
    public DBGarbageCollector dbgc;

    @Inject
    public JobContractor(DBGarbageCollector dbgc) {
        this.dbgc = dbgc;
    }
}