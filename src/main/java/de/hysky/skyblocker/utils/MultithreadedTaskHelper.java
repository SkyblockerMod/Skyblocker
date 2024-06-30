package de.hysky.skyblocker.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Assists with queuing a bunch of tasks off-thread and then waiting on the current thread for all tasks to complete, and
 * upon completion run a task based on the result.
 */
public final class MultithreadedTaskHelper<T> {
	private final List<CompletableFuture<T>> futures = new ObjectArrayList<>();

	private MultithreadedTaskHelper() {}

	public static <T> MultithreadedTaskHelper<T> create() {
		return new MultithreadedTaskHelper<>();
	}

	/**
	 * @see #addTask(Supplier, Executor)
	 */
	public void addTask(Supplier<T> task) {
		addTask(task, null);
	}

	/**
	 * Schedules the {@code task} to be ran off-thread.
	 * 
	 * @param executor the executor to use for running the {@code task}.
	 */
	public void addTask(Supplier<T> task, @Nullable Executor executor) {
		CompletableFuture<T> future = executor == null ? CompletableFuture.supplyAsync(task) : CompletableFuture.supplyAsync(task, executor);

		futures.add(future);
	}

	/**
	 * Blocks the thread until all tasks have completed, then passes the result of each task to the {@code taskResultConsumer}.
	 * 
	 * @param taskResultConsumer an action to perform on each completed task.
	 * 
	 * @apiNote This method may throw an unchecked exception if a task completed exceptionally, therefore it is advised that the caller
	 * handles errors on their end to ensure nothing goes south.
	 */
	public void complete(Consumer<T> taskResultConsumer) {
		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

		for (CompletableFuture<T> future : futures) {
			taskResultConsumer.accept(future.join());
		}
	}
}
