package de.hysky.skyblocker.skyblock.todolist.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.List;

@Environment(EnvType.CLIENT)
public class TodoListScroll extends ElementListWidget<TodoListScroll.Entry>
{

	public TodoListScroll(MinecraftClient client, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
	}



	@Environment(EnvType.CLIENT)
	public static class Entry extends ElementListWidget.Entry<TodoListScroll.Entry>
	{
		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{

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
