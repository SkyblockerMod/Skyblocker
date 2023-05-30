package me.xmrvizzy.skyblocker.utils;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.PriorityQueue;

/**
 * A scheduler for running tasks at a later time. Tasks will be run synchronously on the main client thread. Use the instance stored in {@link SkyblockerMod#scheduler}. Do not instantiate this class.
 */
public class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private int currentTick;
    protected final PriorityQueue<ScheduledTask> tasks;

    /**
     * Do not instantiate this class. Use {@link SkyblockerMod#scheduler} instead.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public Scheduler() {
        currentTick = 0;
        tasks = new PriorityQueue<>();
    }

    /**
     * Schedules a task to run after a delay.
     * @param task the task to run
     * @param delay the delay in ticks
     */
    public void schedule(Runnable task, int delay) {
        if (delay < 0)
            LOGGER.warn("Scheduled a task with negative delay");
        ScheduledTask tmp = new ScheduledTask(task, currentTick + delay);
        tasks.add(tmp);
    }

    /**
     * Schedules a task to run every period ticks.
     * @param task the task to run
     * @param period the period in ticks
     */
    public void scheduleCyclic(Runnable task, int period) {
        if (period <= 0)
            LOGGER.error("Attempted to schedule a cyclic task with period lower than 1");
        else
            new CyclicTask(task, period).run();
    }

    public void tick() {
        currentTick += 1;
        ScheduledTask task;
        while ((task = tasks.peek()) != null && task.schedule <= currentTick && runTask(task)) {
            tasks.poll();
        }
    }

    protected boolean runTask(Runnable task) {
        task.run();
        return true;
    }

    /**
     * A task that runs every period ticks. More specifically, this task reschedules itself to run again after period ticks every time it runs.
     * @param inner the task to run
     * @param period the period in ticks
     */
    protected record CyclicTask(Runnable inner, int period) implements Runnable {
        @Override
        public void run() {
            SkyblockerMod.getInstance().scheduler.schedule(this, period);
            inner.run();
        }
    }

    /**
     * A task that runs at a specific tick, relative to {@link #currentTick}.
     * @param inner the task to run
     * @param schedule the tick to run at
     */
    protected record ScheduledTask(Runnable inner, int schedule) implements Comparable<ScheduledTask>, Runnable {
        @Override
        public int compareTo(ScheduledTask o) {
            return schedule - o.schedule;
        }

        @Override
        public void run() {
            inner.run();
        }
    }
}
