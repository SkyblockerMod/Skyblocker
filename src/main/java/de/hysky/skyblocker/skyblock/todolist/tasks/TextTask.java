package de.hysky.skyblocker.skyblock.todolist.tasks;

public class TextTask extends Task
{
	private String text;

	public TextTask(String name) {
		super(name, TaskType.TEXT);
	}
}
