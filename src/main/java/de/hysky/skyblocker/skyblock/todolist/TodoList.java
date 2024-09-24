package de.hysky.skyblocker.skyblock.todolist;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.todolist.tasks.Task;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TodoList {
	public static KeyBinding addToTodoList;

	public static final Logger LOGGER = LoggerFactory.getLogger(TodoList.class);

	private static final Map<String, Task> tasks = new HashMap<>();

	public static Map<String, Task> getTasks() {
		return tasks;
	}

	@Init
	public static void init() {
		addToTodoList = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.skyblocker.addToTodoList",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_B,
						"key.categories.skyblocker"));
	}
}
