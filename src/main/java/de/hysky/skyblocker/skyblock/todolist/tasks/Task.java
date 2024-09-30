package de.hysky.skyblocker.skyblock.todolist.tasks;

import de.hysky.skyblocker.skyblock.todolist.ui.AddTaskScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Supplier;

public abstract class Task {
	public Task(String name, TaskType type) {
		this.name = name;
		this.type = type;
	}

	public float percentageComplete;

	protected String name;
	protected final TaskType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ClickableWidget> getCustomWidgets(AddTaskScreen addTaskScreen) {
		return List.of(new TextWidget(204, 20, Text.of(type.name()), MinecraftClient.getInstance().textRenderer));
	}

	public enum TaskType
	{
		CRAFT(() -> new CraftTask("Craft Task")),
		FORGE(() -> new ForgeTask("Forge Task")),
		PET(() -> new PetTask("Pet Task")),
		MESSAGE(() -> new MessageTask("Message Task"));

		private final Supplier<Task> taskSupplier;

		TaskType(Supplier<Task> taskSupplier) {
			this.taskSupplier = taskSupplier;
		}

		public Supplier<Task> getTaskSupplier() {
			return taskSupplier;
		}
	}

	public void readCustomData(java.util.List<net.minecraft.client.gui.widget.Widget> customWidgets)
	{

	}

	@Override
	public String toString() {
		return "Task{" +
				"name='" + name + '\'' +
				", type=" + type +
				'}';
	}


}

