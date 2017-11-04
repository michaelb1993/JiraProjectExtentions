package com.michabond.scheduling;

import com.michabond.scheduling.Exceptions.SchedulingException;
import com.michabond.scheduling.contract.Contract;

public interface ScheduleManager {

    void createScheduleIfAbsent(Contract contract, long intervalInMillis) throws SchedulingException;

    void createOrUpdateSchedule(Contract contract, long intervalInMillis) throws SchedulingException;

    void deleteSchedule(Contract contract) throws SchedulingException;
}
