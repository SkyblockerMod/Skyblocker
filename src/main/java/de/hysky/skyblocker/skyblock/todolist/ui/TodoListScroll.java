package de.hysky.skyblocker.skyblock.todolist.ui;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.skyblock.todolist.TodoList;
import de.hysky.skyblocker.skyblock.todolist.tasks.Task;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.Colors;
import org.apache.commons.logging.Log;

import java.awt.*;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TodoListScroll extends ElementListWidget<TodoListScroll.Entry>
{
	public TodoListScroll(MinecraftClient client, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
		for(Task task : TodoList.getTasks().values()) {
			addTaskToList(task);
		}
	}

	public void addTaskToList(Task task) {
		addEntry(new Entry(task));
	}


	@Environment(EnvType.CLIENT)
	public static class Entry extends ElementListWidget.Entry<TodoListScroll.Entry>
	{
		private final Task task;
		public Entry(Task task) {
			this.task = task;
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{
			LogUtils.getLogger().error("Rendering task: {}", task.getName());
			var client = MinecraftClient.getInstance();
			context.drawText(client.textRenderer, task.getName(), x + 4, y + 4, Colors.WHITE, true);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}
	}
}
