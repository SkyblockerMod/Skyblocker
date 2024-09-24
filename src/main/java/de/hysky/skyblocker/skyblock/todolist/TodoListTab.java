package de.hysky.skyblocker.skyblock.todolist;

import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.skyblock.itemlist.ItemListWidget;
import de.hysky.skyblocker.skyblock.itemlist.UpcomingEventsTab;
import de.hysky.skyblocker.skyblock.todolist.tasks.Task;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class TodoListTab extends ItemListWidget.TabContainerWidget {
	private final MinecraftClient client;
	private final List<TaskRenderer> tasks;

	public TodoListTab(int x, int y, MinecraftClient client) {
		super(x, y, Text.literal("Todo List Tab"));
		this.client = client;
		tasks = TodoList.getTasks().values().stream().map(TaskRenderer::new).toList();
	}

	@Override
	public void drawTooltip(DrawContext context, int mouseX, int mouseY) {

	}

	@Override
	public List<? extends Element> children() {
		return List.of();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		int x = getX();
		int y = getY();
		context.enableScissor(x, y, getRight(), getBottom());

		context.drawItem(new ItemStack(Items.CLOCK), x, y + 4);
		context.drawText(this.client.textRenderer, "Todo List", x + 17, y + 7, -1, true);

		int tasksY = y + 7 + 24;
		for (TaskRenderer eventRenderer : tasks) {
			eventRenderer.render(context, x + 1, tasksY, mouseX, mouseY);
			tasksY += eventRenderer.getHeight();
		}

		context.disableScissor();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	public static class TaskRenderer
	{
		private final Task task;
		private final String taskName;

		public TaskRenderer(Task task) {
			this.task = task;
			this.taskName = task.getName();
		}

		public void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
			long time = System.currentTimeMillis() / 1000;
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

			context.drawText(textRenderer, Text.literal(Long.toString(time)).fillStyle(Style.EMPTY.withUnderline(isMouseOver(mouseX, mouseY, x, y))), x, y, -1, true);
			if (task == null) {
				context.drawText(textRenderer, Text.literal(" ").append(Text.translatable("skyblocker.events.tab.noMore")), x, y + textRenderer.fontHeight, Colors.GRAY, false);
			}

		}

		public int getHeight() {
			return 20;
		}

		public boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
			return mouseX >= x && mouseX <= x + 131 && mouseY >= y && mouseY <= y+getHeight();
		}

		public List<TooltipComponent> getTooltip() {
			List<TooltipComponent> components = new ArrayList<>();
			components.add(TooltipComponent.of(Text.literal(taskName).formatted(Formatting.UNDERLINE).asOrderedText()));

			return components;
		}
	}
}
