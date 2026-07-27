package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractSelectionPopup<W extends AbstractWidget> extends AbstractPopupScreen {
	private final Consumer<Optional<W>> onDone;
	private final int expectedWidgetWidth;
	protected @Nullable W selectedItem = null;

	protected AbstractSelectionPopup(Component title, Screen backgroundScreen, Consumer<Optional<W>> onDone, int expectedWidgetWidth) {
		super(title, backgroundScreen);
		this.onDone = onDone;
		this.expectedWidgetWidth = expectedWidgetWidth;
	}

	private final GridLayout gridWidget = new GridLayout();
	private @Nullable Button doneButton;

	@Override
	public void init() {
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
		addRenderableWidget(adder.addChild(new ItemList(300, (int) (height * 0.8f)), 2));
		addRenderableWidget(adder.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> {
			onClose();
			onDone.accept(Optional.empty());
		}).build()));
		doneButton = Button.builder(CommonComponents.GUI_DONE, _ -> {
			onClose();
			onDone.accept(Optional.ofNullable(selectedItem));
		}).build();
		doneButton.active = false;
		addRenderableWidget(adder.addChild(doneButton));
		gridWidget.arrangeElements();
		repositionElements();
	}

	protected void setSelectedItem(W selectedItem) {
		this.selectedItem = selectedItem;
		if (doneButton != null) doneButton.active = true;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.extractBackground(context, mouseX, mouseY, delta);
		extractPopupBackground(context, gridWidget.getX(), gridWidget.getY(), gridWidget.getWidth(), gridWidget.getHeight());
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		gridWidget.setPosition((width - gridWidget.getWidth()) / 2, (height - gridWidget.getHeight()) / 2);
	}

	protected abstract Collection<W> filterWidgets(String input);

	private class ItemList extends SearchableGridWidget {

		private ItemList(int width, int height) {
			super(0, 0, width, height, Component.literal("Item List"), expectedWidgetWidth);
			setSearch("");
		}

		@Override
		protected Collection<? extends AbstractWidget> filterWidgets(String input) {
			return AbstractSelectionPopup.this.filterWidgets(input);
		}

		@Override
		protected double scrollRate() {
			return 15;
		}
	}
}
