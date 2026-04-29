package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@RegisterWidget
public class EndHudWidget extends ElementBasedWidget {
	private static final MutableComponent TITLE = Component.literal("The End").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);

	private static @Nullable EndHudWidget instance = null;

	private static final FlexibleItemStack ENDERMAN_HEAD = Util.make(new FlexibleItemStack(Items.PLAYER_HEAD), stack -> stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("MHF_Enderman")));
	private static final FlexibleItemStack POPPY = Util.make(new FlexibleItemStack(Items.POPPY), stack -> stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

	public EndHudWidget() {
		super(TITLE, ChatFormatting.DARK_PURPLE.getColor(), new Information("hud_end", Component.literal("End Hud"), Location.THE_END));
		instance = this;
		this.update();
	}

	public static EndHudWidget getInstance() {
		return Objects.requireNonNull(instance, "EndHudWidget not initialized");
	}

	@Override
	public void updateContent() {
		// Zealots
		if (SkyblockerConfigManager.get().otherLocations.end.zealotKillsEnabled) {
			TheEnd.EndStats endStats = TheEnd.PROFILES_STATS.computeIfAbsent(TheEnd.EndStats.EMPTY);
			assert endStats != null; // remove warning, even though it can't be null...
			addElement(Elements.iconTextComponent(ENDERMAN_HEAD, Component.literal("Zealots").withStyle(ChatFormatting.BOLD)));
			addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.zealotsSinceLastEye", endStats.zealotsSinceLastEye())));
			addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.zealotsTotalKills", Formatters.INTEGER_NUMBERS.format(endStats.totalZealotKills()))));
			String avg = endStats.eyes() == 0 ? "???" : Formatters.DOUBLE_NUMBERS.format((float) endStats.totalZealotKills() / endStats.eyes());
			addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.avgKillsPerEye", avg)));
		}

		// Endstone protector
		if (SkyblockerConfigManager.get().otherLocations.end.protectorLocationEnabled) {
			addElement(Elements.iconTextComponent(POPPY, Component.literal("End Stone Protector").withStyle(ChatFormatting.BOLD)));
			if (TheEnd.stage == 5) {
				addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.stage", "IMMINENT")));
			} else {
				addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.stage", String.valueOf(TheEnd.stage))));
			}
			if (TheEnd.currentProtectorLocation == null) {
				addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.location", "?")));
			} else {
				addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.location", TheEnd.currentProtectorLocation.name())));
			}
		}
	}
}
