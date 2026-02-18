package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@RegisterWidget
public class PickobulusHudWidget extends ComponentBasedWidget {
	private static final MutableComponent TITLE = Component.literal("Pickobulus").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.GOLD_MINE, Location.DEEP_CAVERNS, Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS, Location.GLACITE_MINESHAFTS);
	private static @Nullable PickobulusHudWidget instance;

	public PickobulusHudWidget() {
		super(TITLE, ChatFormatting.BLUE.getColor(), "hud_pickobulus");
		instance = this;
		update();
	}

	@SuppressWarnings("unused")
	public static PickobulusHudWidget getInstance() {
		if (instance == null) new PickobulusHudWidget();
		return instance;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public void updateContent() {
		Component errorMessage = PickobulusHelper.getErrorMessage();
		if (errorMessage != null) {
			addComponent(new PlainTextComponent(errorMessage));
			return;
		}

		addComponent(new PlainTextComponent(Component.literal("Total Blocks: " + PickobulusHelper.getTotalBlocks())));

		int[] drops = PickobulusHelper.getDrops();
		for (PickobulusHelper.MiningDrop drop : PickobulusHelper.MiningDrop.values()) {
			int count = drops[drop.ordinal()];
			if (count > 0) {
				addComponent(new PlainTextComponent(Component.literal(drop.friendlyName() + ": " + count)));
			}
		}
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public boolean shouldRender(Location location) {
		return super.shouldRender(location) && PickobulusHelper.shouldRender();
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return AVAILABLE_LOCATIONS.contains(location) && SkyblockerConfigManager.get().mining.pickobulusHelper.enablePickobulusHud;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("skyblocker.config.mining.pickobulusHelper");
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!AVAILABLE_LOCATIONS.contains(location)) return;
		SkyblockerConfigManager.get().mining.pickobulusHelper.enablePickobulusHud = enabled;
	}
}
