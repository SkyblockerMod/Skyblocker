package de.hysky.skyblocker.utils.scheduler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A scheduler for running tasks at a later time. Tasks will be run synchronously on the main client thread. Use the instance stored in {@link #INSTANCE}. Do not instantiate this class.
 */
public class Scheduler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    public static final Scheduler INSTANCE = new Scheduler();
    private int currentTick = 0;
    private final AbstractInt2ObjectMap<List<Runnable>> tasks = new Int2ObjectOpenHashMap<>();
    private final ExecutorService executors = ForkJoinPool.commonPool();

    protected Scheduler() {
    }

    /**
     * @see #schedule(Runnable, int, boolean)
     */
    public void schedule(Runnable task, int delay) {
        schedule(task, delay, false);
    }

    /**
     * @see #scheduleCyclic(Runnable, int, boolean)
     */
    public void scheduleCyclic(Runnable task, int period) {
        scheduleCyclic(task, period, false);
    }

    /**
     * Schedules a task to run after a delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks
     * @param multithreaded whether to run the task on the schedulers dedicated thread pool
     */
    public void schedule(Runnable task, int delay, boolean multithreaded) {
    	if (!RenderSystem.isOnRenderThread() && MinecraftClient.getInstance() != null) {
    		LOGGER.warn("[Skyblocker Scheduler] Called the scheduler from the {} class on the {} thread. This will be unsupported in the future.", StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass().getName(), Thread.currentThread().getName());
    		MinecraftClient.getInstance().send(() -> schedule(task, delay, multithreaded));

    		return;
    	}

        if (delay >= 0) {
            addTask(multithreaded ? new ScheduledTask(task, true) : task, currentTick + delay);
        } else {
            LOGGER.warn("Scheduled a task with negative delay");
        }
    }

    /**
     * Schedules a task to run every period ticks.
     *
     * @param task   the task to run
     * @param period the period in ticks
     * @param multithreaded whether to run the task on the schedulers dedicated thread pool
     */
    public void scheduleCyclic(Runnable task, int period, boolean multithreaded) {
    	if (!RenderSystem.isOnRenderThread() && MinecraftClient.getInstance() != null) {
    		LOGGER.warn("[Skyblocker Scheduler] Called the scheduler from the {} class on the {} thread. This will be unsupported in the future.", StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass().getName(), Thread.currentThread().getName());
    		MinecraftClient.getInstance().send(() -> scheduleCyclic(task, period, multithreaded));

    		return;
    	}

        if (period > 0) {
            addTask(new ScheduledTask(task, period, true, multithreaded), currentTick);
        } else {
            LOGGER.error("Attempted to schedule a cyclic task with period lower than 1");
        }
    }

    /**
     * Creates a command that queues a screen to open in the next tick. Used in commands to avoid screen immediately closing after the command is executed.
     *
     * @param screenFactory the factory of the screen to open
     * @return the command
     */
    public static Command<FabricClientCommandSource> queueOpenScreenFactoryCommand(Function<CommandContext<FabricClientCommandSource>, Screen> screenFactory) {
        return context -> queueOpenScreen(screenFactory.apply(context));
    }

    /**
     * Creates a command that queues a screen to open in the next tick. Used in commands to avoid screen immediately closing after the command is executed.
     *
     * @param screenSupplier the supplier of the screen to open
     * @return the command
     */
    public static Command<FabricClientCommandSource> queueOpenScreenCommand(Supplier<Screen> screenSupplier) {
        return context -> queueOpenScreen(screenSupplier.get());
    }

    /**
     * Creates a command that queues a screen to open in the next tick. Used in commands to avoid screen immediately closing after the command is executed.
     *
     * @param screen the screen to open
     * @return the command
     */
    public static Command<FabricClientCommandSource> queueOpenScreenCommand(Screen screen) {
        return context -> queueOpenScreen(screen);
    }

    /**
     * Schedules a screen to open in the next tick. Used in commands to avoid screen immediately closing after the command is executed.
     *
     * @param screenSupplier the supplier of the screen to open
     * @see #queueOpenScreenCommand(Supplier)
     * @deprecated Use {@link #queueOpenScreen(Screen)} instead
     */
    @Deprecated(forRemoval = true)
    public static int queueOpenScreen(Supplier<Screen> screenSupplier) {
        return queueOpenScreen(screenSupplier.get());
    }

    /**
     * Schedules a screen to open in the next tick. Used in commands to avoid screen immediately closing after the command is executed.
     *
     * @param screen the screen to open
     * @see #queueOpenScreenFactoryCommand(Function)
     */
    public static int queueOpenScreen(Screen screen) {
        MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }

    public void tick() {
        Profiler profiler = Profilers.get();
        profiler.push("skyblockerSchedulerTick");

        if (tasks.containsKey(currentTick)) {
            List<Runnable> currentTickTasks = tasks.get(currentTick);
            //noinspection ForLoopReplaceableByForEach (or else we get a ConcurrentModificationException)
            for (int i = 0; i < currentTickTasks.size(); i++) {
                Runnable task = currentTickTasks.get(i);
                if (!runTask(task, task instanceof ScheduledTask scheduledTask && scheduledTask.multithreaded)) {
                    tasks.computeIfAbsent(currentTick + 1, key -> new ArrayList<>()).add(task);
                }
            }
            tasks.remove(currentTick);
        }

        currentTick += 1;
        profiler.pop();
    }

    /**
     * Runs the task if able.
     *
     * @param task the task to run
     * @return {@code true} if the task is run, and {@link false} if task is not run.
     */
    protected boolean runTask(Runnable task, boolean multithreaded) {
        if (multithreaded) {
            executors.execute(task);
        } else {
            task.run();
        }

        return true;
    }

    private void addTask(Runnable task, int schedule) {
        if (tasks.containsKey(schedule)) {
            tasks.get(schedule).add(task);
        } else {
            List<Runnable> list = new ArrayList<>();
            list.add(task);
            tasks.put(schedule, list);
        }
    }

    /**
     * A task that that is scheduled to execute once after the {@code interval}, or that is run every {@code interval} ticks.
     */
    protected record ScheduledTask(Runnable task, int interval, boolean cyclic, boolean multithreaded) implements Runnable {
        private ScheduledTask(Runnable task, boolean multithreaded) {
            this(task, -1, false, multithreaded);
        }

        @Override
        public void run() {
            task.run();

            if (cyclic) INSTANCE.addTask(this, INSTANCE.currentTick + interval);
        }
    }
}
