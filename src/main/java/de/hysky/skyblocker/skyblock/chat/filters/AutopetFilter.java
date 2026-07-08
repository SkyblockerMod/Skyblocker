package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import java.util.Objects;
import java.util.regex.Matcher;

import de.hysky.skyblocker.utils.render.gui.BasicToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class AutopetFilter extends ChatPatternListener {
	private static final FlexibleItemStack ICON = ItemUtils.createSkull(HeadTextures.AUTOPET_RULES_2);

	public AutopetFilter() {
		super("^Autopet equipped your .*! VIEW RULE$");
	}

	@Override
	public boolean onMatch(Component message, Matcher matcher) {
		if (SkyblockerConfigManager.get().chat.hideAutopet == ChatFilterResult.ACTION_BAR) {
			Objects.requireNonNull(Minecraft.getInstance().player).sendOverlayMessage(
					Component.literal(
							message.getString().replace("VIEW RULE", "")
					));
		} else if (SkyblockerConfigManager.get().chat.hideAutopet == ChatFilterResult.TOAST) {
			Minecraft.getInstance().gui.toastManager().addToast(new BasicToast(Component.literal(message.getString().replace("VIEW RULE", "")), (long) (SkyblockerConfigManager.get().chat.toastDisplayDuration * 1000L), ICON.getStack()));
		}
		return true;
	}

	@Override
	public ChatFilterResult state() {
		if (SkyblockerConfigManager.get().chat.hideAutopet == ChatFilterResult.ACTION_BAR || SkyblockerConfigManager.get().chat.hideAutopet == ChatFilterResult.TOAST)
			return ChatFilterResult.FILTER;
		else
			return SkyblockerConfigManager.get().chat.hideAutopet;
	}
}
