package com.michabond.impl;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.scheduler.SchedulerService;

import com.michabond.scheduling.Exceptions.SchedulingException;
import com.michabond.scheduling.ScheduleManager;
import com.michabond.scheduling.contract.JobContractor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Coordinate all the startup information to decide when it is safe to do complicated work.
 * <p>
 * First, a disclaimer: Navigating the startup of the plugin system is unnecessarily
 * complicated.  We know this and we plan to fix it in a future version of the plugins
 * framework, but for now the solution to these problems is admittedly <em>ad hoc</em>.
 * Some of the problem areas that frequently come up include:
 * </p>
 * <ul>
 * <li>You cannot use Active Objects safely until the plugin has been fully enabled.  The
 *      way to know that this has happened is that your event listener receives a
 *      {@code PluginEnabledEvent}.  Of course, you have to have registered the event
 *      listener for this to happen.</li>
 * <li>You should not register an event listener in your constructor.  It complicates unit
 *      testing because you will have to inject a mock instead of leaving it {@code null},
 *      but the bigger problem is that the listener will very likely be triggered by another
 *      thread, and this introduces visibility concerns.  It is admittedly a rare problem,
 *      but another thread can see {@code null} values for your fields, even {@code final}
 *      ones, if you let the {@code this} pointer escape the constructor, and that is what
 *      registering an event listener there does.  See section 3.2.1 of the excellent book
 *      <em>Java Concurrency in Practice</em> for a full explanation.  The workaround for
 *      this is to register in {@code InitializingBean.afterPropertiesSet} or a method
 *      marked with {@code @PostConstruct}.</li>
 * <li>You cannot interact with the scheduling system (either SAL's {@code PluginScheduler}
 *      or the new {@code SchedulerService}) before the application has initialized its
 *      database.  In JIRA, the first time your plugin is enabled on a new system, this
 *      has not happened yet.  The way to find that this is happened is to register a
 *      public component using SAL's {@code LifecycleAware} interface.  It will not get
 *      the call to {@link LifecycleAware#onStart()} until the database is set up.</li>
 * <li>Since the scheduled work is likely to interact with other components (like Active
 *      Objects) that are uncooperative when the plugin is not yet fully enabled, the
 *      best policy is to make sure that <strong>all</strong> of these things have happened
 *      before you schedule anything.</li>
 * <li>Finally, you must not leave scheduler objects or event listeners behind when the
 *      plugin is disabled, so these actions need to be cleaned back up afterwards.</li>
 * </ul>
 * <p>
 * This class copies the approach used by JIRA Agile to solve this problem.  In particular,
 * it watches as the various events occur.  The actual launching, creation of initial data,
 * and scheduling of background tasks is delayed until all of the pieces of the puzzle are
 * in place.  The other components are initilized explicitly by this launcher, though there
 * are other strategies (like using an event to decouple this interaction) that might be
 * better.
 * </p>
 *
 * @since v1.0
 */
@ExportAsService
@Named
public class PluginLauncher implements LifecycleAware, InitializingBean, DisposableBean {
    private static final Logger LOGGER = Logger.getLogger(PluginLauncher.class);
    private static final String PLUGIN_KEY = "com.michabond.jpext";

    private final ActiveObjects ao;
    private final EventPublisher eventPublisher;
    private final SchedulerService schedulerService;
    // Helpers
    private final JobContractor jobContractor;
    private final ScheduleManager scheduleManager;

    /**
     * Used to keep track of everything that needs to happen before we are sure that it is safe
     * to talk to all of the components we need to use, particularly the {@code SchedulerService}
     * and Active Objects.  We will not try to initialize until all of them have happened.
     */
    enum LifecycleEvent {
        AFTER_PROPERTIES_SET,
        PLUGIN_ENABLED,
        LIFECYCLE_AWARE_ON_START
    }

    @GuardedBy("this")
    private final Set<LifecycleEvent> lifecycleEvents = EnumSet.noneOf(LifecycleEvent.class);


    @Inject
    public PluginLauncher(final ActiveObjects ao,
                          final EventPublisher eventPublisher,
                          final SchedulerService schedulerService,
                          final ScheduleManager scheduleManager,
                          final JobContractor jobContractor) {
        this.ao = ao;
        this.eventPublisher = eventPublisher;
        this.schedulerService = schedulerService;
        this.scheduleManager = scheduleManager;
        this.jobContractor = jobContractor;
    }


    /**
     * This is received from Spring after the bean's properties are set.  We need to accept this to know when
     * it is safe to register an event listener.
     */
    @Override
    public void afterPropertiesSet() {
        registerListener();
        onLifecycleEvent(LifecycleEvent.AFTER_PROPERTIES_SET);
    }

    /**
     * This is received from SAL after the system is really up and running from its perspective.  This includes
     * things like the database being set up and other tricky things like that.  This needs to happen before we
     * try to schedule anything, or the scheduler's tables may not be in a good state on a clean install.
     */
    @Override
    public void onStart() {
        onLifecycleEvent(LifecycleEvent.LIFECYCLE_AWARE_ON_START);
    }

    @Override
    public void onStop() {
    }

    /**
     * This is received from the plugin system after the plugin is fully initialized.  It is not safe to use
     * Active Objects before this event is received.
     */
    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
        if (PLUGIN_KEY.equals(event.getPlugin().getKey())) {
            onLifecycleEvent(LifecycleEvent.PLUGIN_ENABLED);
        }
    }

    /**
     * This is received from Spring when we are getting destroyed.  We should make sure we do not leave any
     * event listeners or job runners behind; otherwise, we could leak the current plugin context, leading to
     * exceptions from destroyed OSGi proxies, memory leaks, and strange behaviour in general.
     */
    @Override
    public void destroy() throws Exception {
        unregisterListener();
        unregisterJobRunners();
        removeSchedules();
    }


    /**
     * The latch which ensures all of the plugin/application lifecycle progress is completed before we call
     * {@code launch()}.
     */
    private void onLifecycleEvent(LifecycleEvent event) {
        LOGGER.warn("onLifecycleEvent: " + event);
        if (isLifecycleReady(event)) {
            LOGGER.info("Got the last lifecycle event... Time to get started!");
            unregisterListener();

            try {
                launch();
            }
            catch (Exception ex) {
                LOGGER.error("Unexpected error during launch", ex);
            }
        }
    }

    /**
     * The event latch.
     * <p>
     * When something related to the plugin initialization happens, we call this with
     * the corresponding type of the event.  We will return {@code true} at most once, when the very last type
     * of event is triggered.  This method has to be {@code synchronized} because {@code EnumSet} is not
     * thread-safe and because we have multiple accesses to {@code lifecycleEvents} that need to happen
     * atomically for correct behaviour.
     * </p>
     *
     * @param event the lifecycle event that occurred
     * @return {@code true} if this completes the set of initialization-related events; {@code false} otherwise
     */
    synchronized private boolean isLifecycleReady(LifecycleEvent event) {
        return lifecycleEvents.add(event) && lifecycleEvents.size() == LifecycleEvent.values().length;
    }

    /**
     * Do all the things we can't do before the system is fully up.
     */
    private void launch() throws Exception {
        LOGGER.warn("LAUNCH!");
        initActiveObjects();
        registerJobRunners();
        createSchedules();
        LOGGER.warn("launched successfully");
    }

    /**
     * Prod AO to make sure it is really and truly ready to go.  If AO needs to do things like upgrade the
     * schema or if it is going to completely blow up on us, then hopefully that will happen here.  If we
     * don't do this, then AO will do all of these things when we first touch it at some arbitrary other
     * point in the code, meaning that the place where the upgrades, failures, etc. happen might not be
     * deterministic.  Explicitly prodding AO here makes the system more deterministic and therefore easier
     * to troubleshoot.
     * <p/>
     * Note that this is not necessary for AO 0.26 onwards (JIRA 6.4 and later), as AO is initialised as
     * soon as it can be &mdash; that is, once the {@code <ao>} configuration module and a data source
     * are both present.
     */
    @Transactional
    private void initActiveObjects() {
        LOGGER.info("initActiveObjects");
        ao.flushAll();
    }

    private void registerListener() {
        LOGGER.info("registerListeners");
        eventPublisher.register(this);
    }

    private void unregisterListener() {
        LOGGER.info("unregisterListeners");
        eventPublisher.unregister(this);
    }

    private void registerJobRunners() {
        LOGGER.info("registerJobRunner");
        schedulerService.registerJobRunner(
                JobContractor.DBGC_CONTRACT.getJobConfig().getJobRunnerKey(),
                this.jobContractor.dbgc);
    }

    private void unregisterJobRunners() {
        LOGGER.info("unregisterJobRunner");
        schedulerService.unregisterJobRunner(JobContractor.DBGC_CONTRACT.getJobConfig().getJobRunnerKey());
    }

    private void createSchedules() {
        try {
            this.scheduleManager.createScheduleIfAbsent(JobContractor.DBGC_CONTRACT, JobContractor.DEFAULT_JOB_INTERVAL);
        }
        catch (SchedulingException e) {
            //
        }
    }

    private void removeSchedules() {
        try {
            this.scheduleManager.deleteSchedule(JobContractor.DBGC_CONTRACT);
        }
        catch (SchedulingException e) {
            //
        }
    }
}
