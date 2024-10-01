package de.hysky.skyblocker.skyblock.todolist;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.todolist.tasks.Task;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TodoList {
	public static final Logger LOGGER = LoggerFactory.getLogger(TodoList.class);

	private static final Map<String, Task> tasks = new HashMap<>();

	public static Map<String, Task> getTasks() { return tasks; }

	private static final Path TASKS_FILE = SkyblockerMod.CONFIG_DIR.resolve("tasks.json");

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(TodoList::load);
		ClientLifecycleEvents.CLIENT_STOPPING.register(TodoList::save);
	}

	private static void load(MinecraftClient minecraftClient) {
		if (!Files.exists(TASKS_FILE)) {
			return;
		}
		try {
			JsonObject jsonObject = SkyblockerMod.GSON.fromJson(Files.newBufferedReader(TASKS_FILE), JsonObject.class);
			JsonArray tasksArray = jsonObject.getAsJsonArray("tasks");
			for (int i = 0; i < tasksArray.size(); i++) {
				JsonObject taskObject = tasksArray.get(i).getAsJsonObject();
				Task task = Task.createFromJson(taskObject);
				if (task != null) {
					task.fromJson(taskObject);
					tasks.put(task.getName(), task);
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load tasks", e);
		}
	}

	public static void save(MinecraftClient client) {
		JsonObject jsonObject = new JsonObject();
		JsonArray tasksArray = new JsonArray();
		for (Task task : tasks.values()) {
			JsonObject taskObject = new JsonObject();
			task.toJson(taskObject);
			tasksArray.add(taskObject);
		}
		jsonObject.add("tasks", tasksArray);
		try (BufferedWriter writer = Files.newBufferedWriter(TASKS_FILE)) {
			writer.write(jsonObject.toString());
		} catch (IOException e) {
			LOGGER.error("Failed to save tasks", e);
		}
	}
}
