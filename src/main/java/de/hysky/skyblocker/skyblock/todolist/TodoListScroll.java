package de.hysky.skyblocker.skyblock.todolist;

import de.hysky.skyblocker.skyblock.waypoint.AbstractWaypointsScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TodoListScroll extends AlwaysSelectedEntryListWidget<TodoListScroll.Entry>
{
	private final HandledScreen<?> parent;

	public TodoListScroll(HandledScreen<?> parent, MinecraftClient client, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
	}


	@Environment(EnvType.CLIENT)
	public static class Entry extends AlwaysSelectedEntryListWidget.Entry<TodoListScroll.Entry>
	{
		@Override
		public Text getNarration() {
			return null;
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{

		}
	}
}
