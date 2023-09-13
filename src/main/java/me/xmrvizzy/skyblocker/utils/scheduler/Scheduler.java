package me.xmrvizzy.skyblocker.utils.scheduler;

import com.mojang.brigadier.Command;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A scheduler for running tasks at a later time. Tasks will be run synchronously on the main client thread. Use the instance stored in {@link #INSTANCE}. Do not instantiate this class.
 */
public class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    public static final Scheduler INSTANCE = new Scheduler();
    private int currentTick = 0;
    private final AbstractInt2ObjectMap<List<ScheduledTask>> tasks = new Int2ObjectOpenHashMap<>();

    protected Scheduler() {
    }

    /**
     * Schedules a task to run after a delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks
     */
    public void schedule(Runnable task, int delay) {
        if (delay >= 0) {
            addTask(new ScheduledTask(task), currentTick + delay);
        } else {
            LOGGER.warn("Scheduled a task with negative delay");
        }
    }

    /**
     * Schedules a task to run every period ticks.
     *
     * @param task   the task to run
     * @param period the period in ticks
     */
    public void scheduleCyclic(Runnable task, int period) {
        if (period > 0) {
            addTask(new CyclicTask(task, period), currentTick);
        } else {
            LOGGER.error("Attempted to schedule a cyclic task with period lower than 1");
        }
    }

    public static Command<FabricClientCommandSource> queueOpenScreenCommand(Supplier<Screen> screenSupplier) {
        return context -> INSTANCE.queueOpenScreen(screenSupplier);
    }

    /**
     * Schedules a screen to open in the next tick. Used in commands to avoid screen immediately closing after the command is executed.
     *
     * @param screenSupplier the supplier of the screen to open
     * @see #queueOpenScreenCommand(Supplier)
     */
    public int queueOpenScreen(Supplier<Screen> screenSupplier) {
        MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(screenSupplier.get()));
        return Command.SINGLE_SUCCESS;
    }

    public void tick() {
        if (tasks.containsKey(currentTick)) {
            List<ScheduledTask> currentTickTasks = tasks.get(currentTick);
            //noinspection ForLoopReplaceableByForEach (or else we get a ConcurrentModificationException)
            for (int i = 0; i < currentTickTasks.size(); i++) {
                ScheduledTask task = currentTickTasks.get(i);
                if (!runTask(task)) {
                    tasks.computeIfAbsent(currentTick + 1, key -> new ArrayList<>()).add(task);
                }
            }
            tasks.remove(currentTick);
        }
        currentTick += 1;
    }

    /**
     * Runs the task if able.
     *
     * @param task the task to run
     * @return {@code true} if the task is run, and {@link false} if task is not run.
     */
    protected boolean runTask(Runnable task) {
        task.run();
        return true;
    }

    private void addTask(ScheduledTask scheduledTask, int schedule) {
        if (tasks.containsKey(schedule)) {
            tasks.get(schedule).add(scheduledTask);
        } else {
            List<ScheduledTask> list = new ArrayList<>();
            list.add(scheduledTask);
            tasks.put(schedule, list);
        }
    }

    /**
     * A task that runs every period ticks. More specifically, this task reschedules itself to run again after period ticks every time it runs.
     */
    protected class CyclicTask extends ScheduledTask {
        private final int period;

        CyclicTask(Runnable inner, int period) {
            super(inner);
            this.period = period;
        }

        @Override
        public void run() {
            super.run();
            addTask(this, currentTick + period);
        }
    }

    /**
     * A task that runs at a specific tick, relative to {@link #currentTick}.
     */
    protected static class ScheduledTask implements Runnable {
        private final Runnable inner;

        public ScheduledTask(Runnable inner) {
            this.inner = inner;
        }

        @Override
        public void run() {
            inner.run();
        }
    }
}
