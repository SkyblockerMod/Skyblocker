package de.hysky.skyblocker.skyblock.todolist.ui;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.skyblock.itemlist.ItemListWidget;
import de.hysky.skyblocker.skyblock.todolist.TodoList;
import de.hysky.skyblocker.skyblock.todolist.tasks.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TodoListTab extends ItemListWidget.TabContainerWidget {
	private final MinecraftClient client;
	private List<TaskRenderer> tasks;

	private final TextFieldWidget searchField;
	private final ButtonWidget addButton;
	private final ElementListWidget<TodoListScroll.Entry> scroll;

	public TodoListTab(int x, int y, MinecraftClient client) {
		super(x, y, Text.literal("Todo List Tab"));
		this.client = client;
		tasks = TodoList.getTasks().values().stream().map(TaskRenderer::new).toList();

		this.searchField = new TextFieldWidget(this.client.textRenderer, x + 16, y + 4, 81, 14, Text.translatable("itemGroup.search"));
		this.searchField.setMaxLength(50);
		this.searchField.setVisible(true);
		this.searchField.setEditable(true);
		this.searchField.setEditableColor(16777215);
		this.searchField.setText("");
		this.searchField.setPlaceholder(Text.translatable("skyblocker.todolist.searchText").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));

		var currentScreen = client.currentScreen;

		addButton = ButtonWidget.builder(Text.of("+"), (button) -> {
			this.client.setScreen(new AddTaskScreen());
			this.updateTaskList();
		}).size(16, 16).position(x + 16 + 81 + 5, y + 3).tooltip(Tooltip.of(Text.translatable("skyblocker.todolist.addTask"))).build();

		scroll = new TodoListScroll(client, 125, 115, y + 3 + 24, 20);
		scroll.setX(x + 2);

	}

	private void updateTaskList() {
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

		searchField.render(context, mouseX, mouseY, delta);

		addButton.render(context, mouseX, mouseY, delta);

		scroll.render(context, mouseX, mouseY, delta);



		context.disableScissor();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (searchField.mouseClicked(mouseX, mouseY, button)) {
			this.searchField.setFocused(true);
			return true;
		}
		else if (addButton.mouseClicked(mouseX, mouseY, button)) {
			addButton.onClick(mouseX, mouseY);
			return true;
		}
		else {
			this.searchField.setFocused(false);
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		else if (this.searchField.isFocused() && this.searchField.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
			return true;
		}
		else if (this.client.options.chatKey.matchesKey(keyCode, scanCode) && !this.searchField.isFocused()) {
			this.searchField.setFocused(true);
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if(this.searchField.keyReleased(keyCode, scanCode, modifiers)) {
			//this.refreshSearchResults();
			return true;
		}
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (this.searchField.charTyped(chr, modifiers)) {
			//this.refreshSearchResults();
			return true;
		} else {
			return super.charTyped(chr, modifiers);
		}
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if(scroll.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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
