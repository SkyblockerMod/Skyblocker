package de.hysky.skyblocker.skyblock.todolist;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.rift.EnigmaSouls;
import de.hysky.skyblocker.skyblock.todolist.tasks.Task;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.waypoint.ProfileAwareWaypoint;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TodoList {
	public static KeyBinding addToTodoList;

	public static final Logger LOGGER = LoggerFactory.getLogger(TodoList.class);

	private static final Map<String, Task> tasks = new HashMap<>();

	public static Map<String, Task> getTasks() { return tasks; }

	private static final Path TASKS_FILE = SkyblockerMod.CONFIG_DIR.resolve("tasks.json");

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(TodoList::load);
		ClientLifecycleEvents.CLIENT_STOPPING.register(TodoList::save);

		addToTodoList = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.skyblocker.addToTodoList",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_B,
						"key.categories.skyblocker"));
	}

	private static void load(MinecraftClient minecraftClient) {

	}

	private static void save(MinecraftClient client) {

	}
}
