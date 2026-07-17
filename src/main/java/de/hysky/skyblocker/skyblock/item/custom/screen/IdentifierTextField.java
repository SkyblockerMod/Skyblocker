package de.hysky.skyblocker.skyblock.item.custom.screen;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;

import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ItemSelectionPopup;

class IdentifierTextField extends EditBox {
	private final Consumer<@Nullable Identifier> callback;
	private String lastValid = "";
	private boolean valid = false;

	IdentifierTextField(int width, int height, Consumer<@Nullable Identifier> callback) {
		super(Minecraft.getInstance().font, width, height, Component.empty());
		super.setResponder(this::onChanged);
		setMaxLength(100);
		this.callback = callback;
		addFormatter((string, _) -> FormattedCharSequence.forward(string, valid ? Style.EMPTY : Style.EMPTY.applyFormat(ChatFormatting.RED)));
	}

	private void onChanged(String s) {
		Identifier identifier = Identifier.tryParse(s);
		valid = true;
		if (s.isBlank()) {
			callback.accept(null);
			lastValid = "";
		} else if (identifier != null) {
			callback.accept(identifier);
			lastValid = s;
		} else valid = false;
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused && !lastValid.equals(getValue())) {
			setValue(lastValid);
		}
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == InputConstants.MOUSE_BUTTON_MIDDLE) {
			Minecraft minecraft = Minecraft.getInstance();
			Consumer<@Nullable ItemStack> applyItemModel = stack -> {
				if (stack != null && stack.has(DataComponents.ITEM_MODEL)) {
					Identifier itemModel = stack.get(DataComponents.ITEM_MODEL);

					this.setValue(itemModel.toString());
					this.callback.accept(itemModel);
				}
			};
			Predicate<FlexibleItemStack> hasSkyblockModel = stack -> {
				Identifier itemModel = stack.get(DataComponents.ITEM_MODEL);

				return itemModel != null && itemModel.getNamespace().equals(Utils.HYPIXEL_SKYBLOCK_NAMESPACE);
			};

			minecraft.gui.setScreen(new ItemSelectionPopup(minecraft.gui.screen(), applyItemModel, hasSkyblockModel));

			// Don't let middle clicks do other things
			return;
		}

		super.onClick(event, doubleClick);
	}

	@Override
	protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
		return buttonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT || buttonInfo.button() == InputConstants.MOUSE_BUTTON_MIDDLE;
	}
}
