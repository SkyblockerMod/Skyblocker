package de.hysky.skyblocker.skyblock.todolist.tasks;

import de.hysky.skyblocker.skyblock.todolist.ui.AddTaskScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.List;

public class MessageTask extends Task
{
	private String message;

	public MessageTask(String name) {
		super(name, TaskType.MESSAGE);
	}

	@Override
	public List<ClickableWidget> getCustomEditWidgets(AddTaskScreen addTaskScreen) {
		var client = MinecraftClient.getInstance();
		var label = new TextWidget(204, 20, Text.of("Enter the message"),client.textRenderer);

		var messageField = new TextFieldWidget(client.textRenderer, 204, 20, Text.of("New Task message"));

		messageField.setChangedListener((s) -> message = s);

		return List.of(label, messageField);
	}
}
