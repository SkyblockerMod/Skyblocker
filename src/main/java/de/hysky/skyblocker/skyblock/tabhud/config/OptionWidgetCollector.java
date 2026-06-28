package de.hysky.skyblocker.skyblock.tabhud.config;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public record OptionWidgetCollector(List<AbstractWidget> collectorList) {

	public <T extends AbstractWidget> T addWidget(T widget) {
		collectorList.add(widget);
		return widget;
	}

	public void yesNoButton(Component label, Consumer<Boolean> callback, boolean initialValue) {
		addWidget(CycleButton.booleanBuilder(CommonComponents.GUI_YES, CommonComponents.GUI_NO, initialValue)
				.create(label, (_, value) -> callback.accept(value)));
	}
}
