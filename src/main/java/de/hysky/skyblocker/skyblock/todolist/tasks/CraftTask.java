package de.hysky.skyblocker.skyblock.todolist.tasks;

import io.github.moulberry.repo.data.NEURecipe;
import org.checkerframework.checker.units.qual.C;

public class CraftTask extends RecipeTask {

	public CraftTask(String name, NEURecipe recipe, int amount) {
		super(name, TaskType.CRAFT);
	}
}
