package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
	}

	@Override
	public void updateContent(List<Component> lines) {

		if (lines.isEmpty())
			fetchFromFooter();
		else
			fetchFromWidget(lines);
	}

	private void fetchFromWidget(List<Component> lines) {
		String string = lines.getFirst().getString().replaceAll("[()]", "");
		addComponent(new PlainTextComponent(Component.literal(string).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD).append(Component.literal(" effect(s) active").withStyle(ChatFormatting.WHITE))));
		for (int i = 1; i < lines.size(); i++) {
			addComponent(new PlainTextComponent(lines.get(i)));
		}
	}

	private void fetchFromFooter() {
		String footertext = PlayerListManager.getFooter();

		if (footertext == null || !footertext.contains("Active Effects")) {
			this.addComponent(Components.iconTextComponent());
			return;

		}

		Matcher m = COOKIE_PATTERN.matcher(footertext);
		if (m.find() && m.group("buff") != null) {
			String buff = m.group("buff");
			if (buff.startsWith("Not")) {
				this.addComponent(Components.iconTextComponent(ItemRepository.getItemStack("BOOSTER_COOKIE", Ico.COOKIE), Component.nullToEmpty("Cookie: not active")));
			} else {
				Component cookie = Component.literal("Cookie: ").append(buff);
				this.addComponent(Components.iconTextComponent(ItemRepository.getItemStack("BOOSTER_COOKIE", Ico.COOKIE), cookie));
			}
		}

		String[] lines = footertext.split("Active Effects")[1].split("\n");
		if (lines.length < 2) {
			this.addComponent(Components.iconTextComponent());
			return;
		}

		if (lines[1].startsWith("No")) {
			Component txt = Component.literal("No effects active").withStyle(ChatFormatting.GRAY);
			this.addComponent(Components.iconTextComponent(Ico.POTION, txt));
		} else if (lines[1].contains("God")) {
			String timeleft = lines[1].split("! ")[1];
			Component godpot = Component.literal("God potion!").withStyle(ChatFormatting.RED);
			Component txttleft = Component.literal(timeleft).withStyle(ChatFormatting.LIGHT_PURPLE);
			this.addComponent(Components.iconFatTextComponent(ItemRepository.getItemStack("GOD_POTION_2", Ico.GOD_POTION), godpot, txttleft));
		} else {
			String number = lines[1].substring("You have ".length());
			int idx = number.indexOf(' ');
			if (idx == -1 || lines.length < 4) {
				this.addComponent(Components.iconFatTextComponent());
				return;
			}
			number = number.substring(0, idx);
			Component active = Component.literal("Active Effects: ")
					.append(Component.literal(number).withStyle(ChatFormatting.YELLOW));

			this.addComponent(Components.iconFatTextComponent(Ico.POTION, active,
					Component.literal(lines[2]).withStyle(ChatFormatting.AQUA)));
		}
	}
}
