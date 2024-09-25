package de.hysky.skyblocker.skyblock.todolist.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class AddTaskScreen extends Screen
{
	private final Screen parent;

	protected AddTaskScreen(Screen parent) {
		// The parameter is the title of the screen,
		// which will be narrated when you enter the screen.
		super(Text.translatable("skyblocker.todolist.addTaskScreen.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ButtonWidget buttonWidget = ButtonWidget.builder(Text.of("Hello World"), (btn) -> {
			// When the button is clicked, we can display a toast to the screen.
			this.client.getToastManager().add(
					SystemToast.create(this.client, SystemToast.Type.NARRATOR_TOGGLE, Text.of("Hello World!"), Text.of("This is a toast."))
			);
		}).dimensions(40, 40, 120, 20).build();
		// x, y, width, height
		// It's recommended to use the fixed height of 20 to prevent rendering issues with the button
		// textures.

		// Register the button widget.
		this.addDrawableChild(buttonWidget);

	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		// Minecraft doesn't have a "label" widget, so we'll have to draw our own text.
		// We'll subtract the font height from the Y position to make the text appear above the button.
		// Subtracting an extra 10 pixels will give the text some padding.
		// textRenderer, text, x, y, color, hasShadow
		context.drawText(this.textRenderer, "Special Button", 40, 40 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}
}

