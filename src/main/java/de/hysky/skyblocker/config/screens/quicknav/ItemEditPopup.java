package de.hysky.skyblocker.config.screens.quicknav;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.utils.command.argumenttypes.item.ComponentArgument;
import de.hysky.skyblocker.utils.command.suggestions.TextFieldSuggestions;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;

class ItemEditPopup extends AbstractPopupScreen {

	private final Runnable onClose;
	private final QuickNavigationConfig.QuickNavItem item;
	private final QuickNavConfigScreen.ConfigItemSetter setter;
	private final LinearLayout layout = LinearLayout.vertical().spacing(4);
	private TextFieldSuggestions suggestions;

	protected ItemEditPopup(Screen backgroundScreen, Runnable onClose, QuickNavigationConfig.QuickNavItem item, QuickNavConfigScreen.ConfigItemSetter setter) {
		super(Component.literal("Edit button or something"), backgroundScreen);
		this.onClose = onClose;
		this.item = item;
		this.setter = setter;
	}

	@Override
	protected void init() {
		EditBox box = layout.addChild(new EditBox(font, 250, 20, Component.empty()));
		this.suggestions = new TextFieldSuggestions(
				this.minecraft, this, box, this.font, true, 7,
				RequiredArgumentBuilder.<ClientSuggestionProvider, DataComponentPatch>argument("argument", new ComponentArgument(TextFieldSuggestions.getContext())).build()
		);
		this.suggestions.setAllowSuggestions(true);
		layout.visitWidgets(this::addRenderableWidget);
		box.setResponder(_ -> suggestions.updateCommandInfo());
		super.init();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (suggestions.keyPressed(event)) return true;
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		return suggestions.mouseScrolled(scrollY) || super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return suggestions.mouseClicked(event) || super.mouseClicked(event, doubleClick);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		extractPopupBackground(graphics, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		suggestions.extractRenderState(graphics, mouseX, mouseY);
		graphics.text(font, String.valueOf(suggestions.isVisible()), 0, 0, -1);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		layout.arrangeElements();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	@Override
	public void onClose() {
		super.onClose();
		onClose.run();
	}
}
