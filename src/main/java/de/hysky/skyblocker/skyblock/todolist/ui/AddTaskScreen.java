package de.hysky.skyblocker.skyblock.todolist.ui;

import de.hysky.skyblocker.skyblock.todolist.tasks.Task;
import de.hysky.skyblocker.skyblock.waypoint.DropdownWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class AddTaskScreen extends Screen
{
	private static Logger LOGGER = LoggerFactory.getLogger(AddTaskScreen.class);

	protected AddTaskScreen() {
		super(Text.translatable("skyblocker.todolist.addTaskScreen.title"));
	}

	@Override
	protected void init() {
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().margin(4, 4, 4, 4);

		List<Task.TaskType> taskSubclasses = Task.taskTypeClassMap.keySet().stream().toList();
		selectedTaskType = taskSubclasses.getFirst();


		int row = 0;

		gridWidget.add(new TextWidget(204, 20, Text.of("Select the type of task"), client.textRenderer), row , 0);

		DropdownWidget<Task.TaskType> dropdownWidget = new DropdownWidget<>(client, 0, 0, 204, 100, taskSubclasses, this::selectTaskType, taskSubclasses.getFirst());
		gridWidget.add(dropdownWidget, row, 0, gridWidget.copyPositioner().marginTop(20));

		gridWidget.add(new TextWidget(204, 20, Text.of("Enter the task name"), client.textRenderer), ++row , 0);

		var nameField = new TextFieldWidget(204, 20, Text.of("New Task"), client.textRenderer);


		gridWidget.add(new TextWidget(204, 20, Text.of("Enter the task description"), client.textRenderer), ++row , 0);

		gridWidget.add(ButtonWidget.builder(Text.of("Create Task"), button -> {
			try {
				var clazz = Task.taskTypeClassMap.get(selectedTaskType);
				var task = clazz.getDeclaredConstructor(String.class).newInstance("New Task");
				LOGGER.error("Created task: {}", task);
			} catch (Exception e) {
				LOGGER.error("Failed to create task", e);
			}


		}).width(204).build(), ++row, 0);

		gridWidget.add(ButtonWidget.builder(Text.of("Exit Without Saving"), button -> {
			close();
		}).width(204).build(), ++row, 0);

		gridWidget.refreshPositions();
	    SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5F, 0.25F);
		gridWidget.forEachChild(this::addDrawableChild);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

	}

	private Task.TaskType selectedTaskType;

	private void selectTaskType(Task.TaskType taskType) {
		LOGGER.info("Selected task type: " + taskType);
		selectedTaskType = taskType;
	}

	@Override
	public void close()
	{
		if(client != null) {
			client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
		}
	}
}

