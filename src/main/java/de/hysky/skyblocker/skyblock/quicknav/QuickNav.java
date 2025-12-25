package de.hysky.skyblocker.skyblock.quicknav;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class QuickNav {
	static final Logger LOGGER = LoggerFactory.getLogger(QuickNav.class);

	public static List<QuickNavButton> init(String screenTitle) {
		List<QuickNavButton> buttons = new ArrayList<>();
		QuickNavigationConfig data = SkyblockerConfigManager.get().quickNav;
		try {
			if (data.button1.render) buttons.add(parseButton(data.button1, screenTitle, 0));
			if (data.button2.render) buttons.add(parseButton(data.button2, screenTitle, 1));
			if (data.button3.render) buttons.add(parseButton(data.button3, screenTitle, 2));
			if (data.button4.render) buttons.add(parseButton(data.button4, screenTitle, 3));
			if (data.button5.render) buttons.add(parseButton(data.button5, screenTitle, 4));
			if (data.button6.render) buttons.add(parseButton(data.button6, screenTitle, 5));
			if (data.button7.render) buttons.add(parseButton(data.button7, screenTitle, 6));
			if (data.button8.render) buttons.add(parseButton(data.button8, screenTitle, 7));
			if (data.button9.render) buttons.add(parseButton(data.button9, screenTitle, 8));
			if (data.button10.render) buttons.add(parseButton(data.button10, screenTitle, 9));
			if (data.button11.render) buttons.add(parseButton(data.button11, screenTitle, 10));
			if (data.button12.render) buttons.add(parseButton(data.button12, screenTitle, 11));
			if (data.button13.render) buttons.add(parseButton(data.button13, screenTitle, 12));
			if (data.button14.render) buttons.add(parseButton(data.button14, screenTitle, 13));
		} catch (CommandSyntaxException e) {
			LOGGER.error("[Skyblocker] Failed to initialize Quick Nav Button", e);
		}
		return buttons;
	}

	private static QuickNavButton parseButton(QuickNavigationConfig.QuickNavItem buttonInfo, String screenTitle, int id) throws CommandSyntaxException {
		QuickNavigationConfig.ItemData itemData = buttonInfo.itemData;
		ItemStack stack = itemData != null && itemData.item != null && itemData.components != null ? ItemStackComponentizationFixer.fromComponentsString(itemData.item.toString(), Math.clamp(itemData.count, 1, 99), itemData.components) : new ItemStack(Items.BARRIER);

		boolean uiTitleMatches = false;
		try {
			uiTitleMatches = screenTitle.matches(buttonInfo.uiTitle);
		} catch (PatternSyntaxException e) {
			LOGGER.error("[Skyblocker] Failed to parse Quick Nav Button with regex: {}", buttonInfo.uiTitle, e);
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				player.displayClientMessage(Constants.PREFIX.get().append(Component.literal("Invalid regex in Quick Nav Button " + (id + 1) + "!").withStyle(ChatFormatting.RED)), false);
			}
		}

		if (buttonInfo.doubleClick) {
			return new QuickNavConfirmationButton(id, uiTitleMatches, buttonInfo.clickEvent, stack, buttonInfo.tooltip);
		}
		return new QuickNavButton(id, uiTitleMatches, buttonInfo.clickEvent, stack, buttonInfo.tooltip);
	}
}
