package de.hysky.skyblocker.skyblock.todolist.ui;

import de.hysky.skyblocker.skyblock.todolist.TodoList;
import de.hysky.skyblocker.skyblock.todolist.tasks.Task;
import de.hysky.skyblocker.skyblock.waypoint.DropdownWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class AddTaskScreen extends Screen
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AddTaskScreen.class);

	DirectionalLayoutWidget verticalLayout = DirectionalLayoutWidget.vertical();

	private final ScrollableDirectionalLayoutWidget customTaskInput = new ScrollableDirectionalLayoutWidget();

	private Task tempTask;

	protected AddTaskScreen() {
		super(Text.translatable("skyblocker.todolist.addTaskScreen.title"));
	}

	@Override
	protected void init()
	{
		super.init();

		verticalLayout.spacing(4).getMainPositioner().alignHorizontalCenter().alignVerticalCenter();

		verticalLayout.add(new TextWidget(204, 20, Text.of("Select the type of task"), client.textRenderer));

		DropdownWidget<Task.TaskType> dropdownWidget = new DropdownWidget<>(client, 0, 0, 204, 80, Arrays.stream(Task.TaskType.values()).toList(), this::updateSelectedTaskType, Task.TaskType.values()[0]);
		verticalLayout.add(dropdownWidget);

		verticalLayout.add(new TextWidget(204, 20, Text.of("Enter the task name"), client.textRenderer));

		var nameField = new TextFieldWidget(client.textRenderer, 204, 20, Text.of("New Task name"));

		verticalLayout.add(nameField);

		verticalLayout.add(customTaskInput);

		verticalLayout.add(ButtonWidget.builder(Text.of("Create Task"), button -> {
			try {
				if(tempTask == null) {
					this.client.getToastManager().add(
							SystemToast.create(this.client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Error creating a new task!"), Text.of("tempTask cannot be null")));
					return;
				}

				tempTask.setName(nameField.getText());
				if(tempTask.getName() == null || tempTask.getName().isEmpty())
				{
					this.client.getToastManager().add(
							SystemToast.create(this.client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Error creating a new task!"), Text.of("Task name cannot be empty")));
					return;
				}

				TodoList.getTasks().put(tempTask.getName(), tempTask);

				LOGGER.error("Created task: {}", tempTask);
				TodoList.save(client);
			} catch (Exception e) {
				this.client.getToastManager().add(
						SystemToast.create(this.client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Error creating a new task!"), Text.of(e.getLocalizedMessage())));
						LOGGER.error("Failed to create task", e);
			}
			close();
		}).width(204).build());

		verticalLayout.add(ButtonWidget.builder(Text.of("Exit Without Saving"), button -> {
			close();
		}).width(204).build());

		verticalLayout.refreshPositions();
		SimplePositioningWidget.setPos(verticalLayout, ScreenRect.of(NavigationAxis.VERTICAL, 0, 0, height, width));
		verticalLayout.forEachChild(this::addDrawableChild);
		updateSelectedTaskType(Task.TaskType.values()[0]);
	}

	private void updateSelectedTaskType(Task.TaskType taskType) {
		tempTask = taskType.getTaskSupplier().get();

		customTaskInput.reset();

		for(var widget : tempTask.getCustomWidgets( this)) {
			customTaskInput.add(widget);
		}

		customTaskInput.add(new TextWidget(204, 10, Text.of(""), client.textRenderer));

		customTaskInput.refreshPositions();
		customTaskInput.forEachChild(this::addDrawableChild);

		verticalLayout.refreshPositions();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void close()
	{
		tempTask = null;
		if(client != null) {
			client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
		}
	}


	public class ScrollableDirectionalLayoutWidget extends DirectionalLayoutWidget
	{
		public ScrollableDirectionalLayoutWidget() {
			super(0,0, DisplayAxis.VERTICAL);
		}

		public void reset() {
			forEachChild(AddTaskScreen.this::remove);
			grid.children.clear();
			grid.grids.clear();
			grid.height = 0;
			currentIndex = 0;
			grid.refreshPositions();
		}

		public void readCustomData(Task task)
		{
			task.readCustomData(grid.children);
		}
	}
}

