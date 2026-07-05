package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import java.util.Objects;
import java.util.regex.Matcher;

import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.render.gui.BasicToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class AutopetFilter extends ChatPatternListener {
	private static ItemStack ICON = ItemStackComponentizationFixer.fromItemString("minecraft:player_head[minecraft:profile={id:[I;-7390193,1641495988,-1810636176,-1326826012],name:\"\",properties:[{name:\"textures\",signature:\"pxcnzvUMWZ5ESaviDIxUmfz3BEvz+wIUYkfsYM4ZJqtOhcgvsR/oC3/Gp+wgpRHBfTNTcOs/V+nlMdpyLPvXD7bhtdVqF5DiYO3O6UuCi30+InAlvPLKsUFEn0NSAH5oKjRr2ppVTwieZSUfoIvF/xXxgrSlZo+U", 1);

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
			Minecraft.getInstance().gui.toastManager().addToast(new BasicToast(Component.literal(message.getString().replace("VIEW RULE", "")), (long) (SkyblockerConfigManager.get().chat.toastDisplayDuration * 1000L), ICON));
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
