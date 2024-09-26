package de.hysky.skyblocker.skyblock.todolist.tasks;

import java.util.List;
import java.util.Map;

public abstract class Task {
	public Task(String name, TaskType type) {
		this.name = name;
		this.type = type;
	}

	public static Map<TaskType, Class<?>> taskTypeClassMap = Map.of(
				TaskType.CRAFT, CraftTask.class,
				TaskType.FORGE, ForgeTask.class,
				TaskType.PET, PetTask.class,
				TaskType.TEXT, TextTask.class);

	public float percentageComplete;

	protected String name;
	protected final TaskType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public enum TaskType
	{
		CRAFT,
		FORGE,
		PET,
		TEXT
	}

	@Override
	public String toString() {
		return "Task{" +
				"name='" + name + '\'' +
				", type=" + type +
				'}';
	}
}

