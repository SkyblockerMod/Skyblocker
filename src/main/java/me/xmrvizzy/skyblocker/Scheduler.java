package me.xmrvizzy.skyblocker;

import java.util.PriorityQueue;

public class Scheduler {
    private int currentTick;
    private final PriorityQueue<ScheduledTask> tasks;

    public Scheduler() {
        currentTick = 0;
        tasks = new PriorityQueue<>();
    }

    public void schedule(Runnable task, int delay) {
        assert delay > 0;
        ScheduledTask tmp = new ScheduledTask(currentTick + delay, task);
        tasks.add(tmp);
    }

    public void scheduleCyclic(Runnable task, int period) {
        new CyclicTask(task, period).run();
    }

    public void tick() {
        currentTick += 1;
        ScheduledTask task;
        while ((task = tasks.peek()) != null && task.schedule <= currentTick) {
            task.run();
            tasks.poll();
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
