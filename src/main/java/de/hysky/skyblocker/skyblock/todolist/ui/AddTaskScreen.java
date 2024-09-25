package de.hysky.skyblocker.skyblock.todolist.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class AddTaskScreen extends Screen
{
	private final Screen parent;

	protected AddTaskScreen(Screen parent) {
		// The parameter is the title of the screen,
		// which will be narrated when you enter the screen.
		super(Text.translatable("skyblocker.todolist.addTaskScreen.title"));
		this.parent = parent;
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}
}

