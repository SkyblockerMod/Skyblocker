package de.hysky.skyblocker.skyblock.end;

import com.mojang.authlib.properties.PropertyMap;
import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.Set;

@RegisterWidget
public class EndHudWidget extends ComponentBasedWidget {
	private static final MutableText TITLE = Text.literal("The End").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD);
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.THE_END);

	private static EndHudWidget instance = null;

	private static final ItemStack ENDERMAN_HEAD = Util.make(new ItemStack(Items.PLAYER_HEAD), stack -> stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of("MHF_Enderman"), Optional.empty(), new PropertyMap())));
	private static final ItemStack POPPY = Util.make(new ItemStack(Items.POPPY), stack -> stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true));

	public EndHudWidget() {
		super(TITLE, Formatting.DARK_PURPLE.getColorValue(), "hud_end");
		instance = this;
		this.update();
	}

	public static EndHudWidget getInstance() {
		return instance;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return location.equals(Location.THE_END) && SkyblockerConfigManager.get().otherLocations.end.hudEnabled;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!location.equals(Location.THE_END)) return;
		SkyblockerConfigManager.get().otherLocations.end.hudEnabled = enabled;
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void updateContent() {
		// Zealots
		if (SkyblockerConfigManager.get().otherLocations.end.zealotKillsEnabled) {
			TheEnd.EndStats endStats = TheEnd.PROFILES_STATS.computeIfAbsent(TheEnd.EndStats.EMPTY);
			addComponent(Components.iconTextComponent(ENDERMAN_HEAD, Text.literal("Zealots").formatted(Formatting.BOLD)));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.zealotsSinceLastEye", endStats.zealotsSinceLastEye())));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.zealotsTotalKills", endStats.totalZealotKills())));
			String avg = endStats.eyes() == 0 ? "???" : Formatters.DOUBLE_NUMBERS.format((float) endStats.totalZealotKills() / endStats.eyes());
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.avgKillsPerEye", avg)));
		}

		// Endstone protector
		if (SkyblockerConfigManager.get().otherLocations.end.protectorLocationEnabled) {
			addComponent(Components.iconTextComponent(POPPY, Text.literal("Endstone Protector").formatted(Formatting.BOLD)));
			if (TheEnd.stage == 5) {
				addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.stage", "IMMINENT")));
			} else {
				addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.stage", String.valueOf(TheEnd.stage))));
			}
			if (TheEnd.currentProtectorLocation == null) {
				addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.location", "?")));
			} else {
				addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.location", TheEnd.currentProtectorLocation.name())));
			}
		}
	}

	@Override
	public Text getDisplayName() {
		return Text.literal("End Hud");
	}
}
