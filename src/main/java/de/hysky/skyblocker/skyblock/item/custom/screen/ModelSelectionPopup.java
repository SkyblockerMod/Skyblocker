package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.mixins.ModelManagerAccessor;
import de.hysky.skyblocker.utils.render.gui.AbstractSelectionPopup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ModelSelectionPopup extends AbstractSelectionPopup<ModelSelectionPopup.Widget> {
	protected ModelSelectionPopup(Screen backgroundScreen, Consumer<@Nullable Identifier> onDone) {
		super(Component.literal("Select Model"), backgroundScreen, opt -> onDone.accept(opt.map(Widget::getValue).orElse(null)), 20);
	}

	private final List<Widget> widgets = ((ModelManagerAccessor) Minecraft.getInstance().getModelManager()).getBakedItemStackModels().keySet().stream().sorted().map(Widget::new).toList();

	@Override
	protected Collection<Widget> filterWidgets(String input) {
		return widgets.stream().filter(w -> w.value.toString().contains(input.replace(' ', '_').toLowerCase(Locale.ENGLISH))).toList();
	}

	protected class Widget extends AbstractWidget {
		private final ItemStack icon;
		private final Identifier value;

		@SuppressWarnings("deprecation")
		public Widget(Identifier identifier) {
			super(0, 0, 20, 20, Component.literal(identifier.toString()));
			this.value = identifier;
			this.icon = new ItemStack(Items.CAT_SPAWN_EGG.builtInRegistryHolder(), 1, DataComponentPatch.builder()
					.set(DataComponents.ITEM_MODEL, identifier)
					.build());
			setTooltip(Tooltip.create(getMessage()));
		}

		public Identifier getValue() {
			return value;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			graphics.item(icon, getX() + 2, getY() + 2);
			if (selectedItem == this) graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x3000FF00);
			if (isHovered()) graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x20FFFFFF);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {}
	}
}
