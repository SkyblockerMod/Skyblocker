package de.hysky.skyblocker.skyblock.todolist.tasks;

import io.github.moulberry.repo.data.NEURecipe;

public abstract class RecipeTask extends Task {
	public NEURecipe recipe;
	public int amount;

	public RecipeTask(String name, TaskType type) {
		super(name, type);
	}
}
