package de.hysky.skyblocker.skyblock.end;

import com.mojang.authlib.properties.PropertyMap;
import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@RegisterWidget
public class EndHudWidget extends ComponentBasedWidget {
	private static final MutableText TITLE = Text.literal("The End").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD);

	private static EndHudWidget instance = null;

	private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance(Locale.US);
	private static final ItemStack ENDERMAN_HEAD = new ItemStack(Items.PLAYER_HEAD);
	private static final ItemStack POPPY = new ItemStack(Items.POPPY);

	static {
		DECIMAL_FORMAT.setMinimumFractionDigits(0);
		DECIMAL_FORMAT.setMaximumFractionDigits(2);

		ENDERMAN_HEAD.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of("MHF_Enderman"), Optional.empty(), new PropertyMap()));
		POPPY.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
	}

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
		return Set.of(Location.THE_END);
	}

	@Override
	public void updateContent() {
		// Zealots
		if (SkyblockerConfigManager.get().otherLocations.end.zealotKillsEnabled) {
			TheEnd.EndStats endStats = TheEnd.PROFILES_STATS.putIfAbsent(TheEnd.EndStats.EMPTY);
			addComponent(new IcoTextComponent(ENDERMAN_HEAD, Text.literal("Zealots").formatted(Formatting.BOLD)));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.zealotsSinceLastEye", endStats.zealotsSinceLastEye())));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.zealotsTotalKills", endStats.totalZealotKills())));
			String avg = endStats.eyes() == 0 ? "???" : DECIMAL_FORMAT.format((float) endStats.totalZealotKills() / endStats.eyes());
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.end.hud.avgKillsPerEye", avg)));
		}

		// Endstone protector
		if (SkyblockerConfigManager.get().otherLocations.end.protectorLocationEnabled) {
			addComponent(new IcoTextComponent(POPPY, Text.literal("Endstone Protector").formatted(Formatting.BOLD)));
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
