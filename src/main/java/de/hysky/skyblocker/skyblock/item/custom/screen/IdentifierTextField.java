package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

class IdentifierTextField extends TextFieldWidget {

	private @NotNull String lastValid = "";
	private boolean valid = false;

	private String uuid = "";

	IdentifierTextField(int width, int height) {
		super(MinecraftClient.getInstance().textRenderer, width, height, Text.empty());
		super.setChangedListener(this::onChanged);
		setRenderTextProvider((s, integer) -> OrderedText.styledForwardsVisitedString(s, valid ? Style.EMPTY : Style.EMPTY.withFormatting(Formatting.RED)));
		setPlaceholder(Text.translatable("skyblocker.customization.item.modelOverride").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
	}

	public void setItem(ItemStack stack) {
		uuid = ItemUtils.getItemUuid(stack);
		if (SkyblockerConfigManager.get().general.customItemModel.containsKey(uuid)) {
			Identifier identifier = SkyblockerConfigManager.get().general.customItemModel.get(uuid);
			String string = identifier.toString();
			setText(string);
		} else {
			setText("");
		}
	}

	private void onChanged(String s) {
		Identifier identifier = Identifier.tryParse(s);
		valid = true;
		if (s.isBlank()) {
			SkyblockerConfigManager.get().general.customItemModel.remove(uuid);
			lastValid = "";
		} else if (identifier != null) {
			SkyblockerConfigManager.get().general.customItemModel.put(uuid, identifier);
			lastValid = s;
		} else valid = false;
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused && !lastValid.equals(getText())) {
			setText(lastValid);
		}
	}
}
