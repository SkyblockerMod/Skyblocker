package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@RegisterWidget
public class EndHudWidget extends ComponentBasedWidget {
	private static final MutableComponent TITLE = Component.literal("The End").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.THE_END);

	private static EndHudWidget instance = null;

	private static final ItemStack ENDERMAN_HEAD = Util.make(new ItemStack(Items.PLAYER_HEAD), stack -> stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("MHF_Enderman")));
	private static final ItemStack POPPY = Util.make(new ItemStack(Items.POPPY), stack -> stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

	public EndHudWidget() {
		super(TITLE, ChatFormatting.DARK_PURPLE.getColor(), new Information("hud_end", Component.literal("End Hud"), AVAILABLE_LOCATIONS::contains));
		instance = this;
		this.update();
	}

	public static EndHudWidget getInstance() {
		return instance;
	}

	@Override
	public void updateContent() {
		// Zealots
		if (SkyblockerConfigManager.get().otherLocations.end.zealotKillsEnabled) {
			TheEnd.EndStats endStats = TheEnd.PROFILES_STATS.computeIfAbsent(TheEnd.EndStats.EMPTY);
			assert endStats != null; // remove warning, even though it can't be null...
			addComponent(Components.iconTextComponent(ENDERMAN_HEAD, Component.literal("Zealots").withStyle(ChatFormatting.BOLD)));
			addComponent(new PlainTextComponent(Component.translatable("skyblocker.end.hud.zealotsSinceLastEye", endStats.zealotsSinceLastEye())));
			addComponent(new PlainTextComponent(Component.translatable("skyblocker.end.hud.zealotsTotalKills", Formatters.INTEGER_NUMBERS.format(endStats.totalZealotKills()))));
			String avg = endStats.eyes() == 0 ? "???" : Formatters.DOUBLE_NUMBERS.format((float) endStats.totalZealotKills() / endStats.eyes());
			addComponent(new PlainTextComponent(Component.translatable("skyblocker.end.hud.avgKillsPerEye", avg)));
		}

		// Endstone protector
		if (SkyblockerConfigManager.get().otherLocations.end.protectorLocationEnabled) {
			addComponent(Components.iconTextComponent(POPPY, Component.literal("Endstone Protector").withStyle(ChatFormatting.BOLD)));
			if (TheEnd.stage == 5) {
				addComponent(new PlainTextComponent(Component.translatable("skyblocker.end.hud.stage", "IMMINENT")));
			} else {
				addComponent(new PlainTextComponent(Component.translatable("skyblocker.end.hud.stage", String.valueOf(TheEnd.stage))));
			}
			if (TheEnd.currentProtectorLocation == null) {
				addComponent(new PlainTextComponent(Component.translatable("skyblocker.end.hud.location", "?")));
			} else {
				addComponent(new PlainTextComponent(Component.translatable("skyblocker.end.hud.location", TheEnd.currentProtectorLocation.name())));
			}
		}
	}

	@Override
	protected List<de.hysky.skyblocker.skyblock.tabhud.widget.component.Component> getConfigComponents() {
		return List.of(Components.iconTextComponent(Ico.BARRIER, Component.literal("TODO"))); // TODO
	}
}
