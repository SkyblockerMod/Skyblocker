package de.hysky.skyblocker.skyblock.todolist.tasks;

import de.hysky.skyblocker.skyblock.todolist.ui.AddTaskScreen;
import io.github.moulberry.repo.data.NEURecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.checkerframework.checker.units.qual.C;

import java.util.List;

public class CraftTask extends RecipeTask {

	public CraftTask(String name) {
		super(name, TaskType.CRAFT);
	}

	@Override
	public List<ClickableWidget> getCustomWidgets(AddTaskScreen addTaskScreen) {
		return List.of(new TextWidget(204, 20, Text.of("craft text"), MinecraftClient.getInstance().textRenderer));
	}
}
