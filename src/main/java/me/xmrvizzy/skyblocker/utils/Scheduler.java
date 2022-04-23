package me.xmrvizzy.skyblocker.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.PriorityQueue;

public class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private int currentTick;
    private final PriorityQueue<ScheduledTask> tasks;

    public Scheduler() {
        currentTick = 0;
        tasks = new PriorityQueue<>();
    }

    public void schedule(Runnable task, int delay) {
        if (delay < 0)
            LOGGER.warn("Scheduled a task with negative delay");
        ScheduledTask tmp = new ScheduledTask(currentTick + delay, task);
        tasks.add(tmp);
    }

    public void scheduleCyclic(Runnable task, int period) {
        if (period <= 0)
            LOGGER.error("Attempted to schedule a cyclic task with period lower than 1");
        else
            new CyclicTask(task, period).run();
    }

    public void tick() {
        currentTick += 1;
        ScheduledTask task;
        while ((task = tasks.peek()) != null && task.schedule <= currentTick) {
            tasks.poll();
            task.run();
        }
    }

    private class CyclicTask implements Runnable {
        private final Runnable inner;
        private final int period;

        public CyclicTask(Runnable task, int period) {
            this.inner = task;
            this.period = period;
        }

        @Override
        public void run() {
            schedule(this, period);
            inner.run();
        }
    }

    private record ScheduledTask(int schedule, Runnable inner) implements Comparable<ScheduledTask>, Runnable {
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
