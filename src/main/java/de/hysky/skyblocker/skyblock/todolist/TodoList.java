package de.hysky.skyblocker.skyblock.todolist;

import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TodoList {
	public static KeyBinding addToTodoList;

	public static final Logger LOGGER = LoggerFactory.getLogger(TodoList.class);

	@Init
	public static void init() {
		System.out.println("Initialized Todo List Keybind before");
		addToTodoList = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.skyblocker.addToTodoList",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_B,
						"key.categories.skyblocker"));
		LOGGER.error("Initialized Todo List Keybind"); //wtf why isnt this called
			System.out.println("Initialized Todo List Keybind");
	}
}
