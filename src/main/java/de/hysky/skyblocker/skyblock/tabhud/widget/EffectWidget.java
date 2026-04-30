package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widgte shows, how many active effects you have.
// it also shows one of those in detail.
// the parsing is super suspect and should be replaced by some regexes sometime later
@RegisterWidget
public class EffectWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Active Effects").withStyle(ChatFormatting.DARK_PURPLE,
			ChatFormatting.BOLD);
	private static final Pattern COOKIE_PATTERN = Pattern.compile(".*\\nCookie Buff\\n(?<buff>.*)\\n");

	public EffectWidget() {
		super("Active Effects", TITLE, ChatFormatting.DARK_PURPLE.getColor());
		PlayerListManager.registerFooterListener(() -> {
			if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.effectsFromFooter && WidgetManager.isWidgetInCurrentScreen(this)) update();
		});
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		String string = widget.detail().getString().replaceAll("[()]", "");
		addElement(new PlainTextElement(Component.literal(string).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD).append(Component.literal(" effect(s) active").withStyle(ChatFormatting.WHITE))));
		for (Component line : widget.lines()) {
			addElement(new PlainTextElement(line));
		}
	}

	@Override
	protected void updateContentMissing() {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.effectsFromFooter) fetchFromFooter();
	}

	private void fetchFromFooter() {
		String footertext = PlayerListManager.getFooter();

		if (footertext == null || !footertext.contains("Active Effects")) {
			this.addElement(Elements.iconTextComponent());
			return;

		}

		Matcher m = COOKIE_PATTERN.matcher(footertext);
		if (m.find() && m.group("buff") != null) {
			String buff = m.group("buff");
			if (buff.startsWith("Not")) {
				this.addElement(Elements.iconTextComponent(ItemRepository.getItemStack("BOOSTER_COOKIE", Ico.COOKIE), Component.nullToEmpty("Cookie: not active")));
			} else {
				Component cookie = Component.literal("Cookie: ").append(buff);
				this.addElement(Elements.iconTextComponent(ItemRepository.getItemStack("BOOSTER_COOKIE", Ico.COOKIE), cookie));
			}
		}

		String[] lines = footertext.split("Active Effects")[1].split("\n");
		if (lines.length < 2) {
			this.addElement(Elements.iconTextComponent());
			return;
		}

		if (lines[1].startsWith("No")) {
			Component txt = Component.literal("No effects active").withStyle(ChatFormatting.GRAY);
			this.addElement(Elements.iconTextComponent(Ico.POTION, txt));
		} else if (lines[1].contains("God")) {
			String timeleft = lines[1].split("! ")[1];
			Component godpot = Component.literal("God potion!").withStyle(ChatFormatting.RED);
			Component txttleft = Component.literal(timeleft).withStyle(ChatFormatting.LIGHT_PURPLE);
			this.addElement(Elements.iconFatTextComponent(ItemRepository.getItemStack("GOD_POTION_2", Ico.GOD_POTION), godpot, txttleft));
		} else {
			String number = lines[1].substring("You have ".length());
			int idx = number.indexOf(' ');
			if (idx == -1 || lines.length < 4) {
				this.addElement(Elements.iconFatTextComponent());
				return;
			}
			number = number.substring(0, idx);
			Component active = Component.literal("Active Effects: ")
					.append(Component.literal(number).withStyle(ChatFormatting.YELLOW));

			this.addElement(Elements.iconFatTextComponent(Ico.POTION, active,
					Component.literal(lines[2]).withStyle(ChatFormatting.AQUA)));
		}
	}
}
