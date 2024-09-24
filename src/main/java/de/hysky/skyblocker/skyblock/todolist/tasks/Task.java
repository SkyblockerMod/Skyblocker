package de.hysky.skyblocker.skyblock.todolist.tasks;

public abstract class Task {
	public float percentageComplete;
	public String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

