package com.michabond.scheduling.contract;

import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;

public class Contract {

    public JobConfig jobConfig;
    public JobId jobId;

    public Contract(JobConfig jobConfig, JobId jobId) {
        this.jobConfig = jobConfig;
        this.jobId = jobId;
    }

    public JobConfig getJobConfig() {
        return this.jobConfig;
    }

    public JobId getJobId() {
        return this.jobId;
    }
}
