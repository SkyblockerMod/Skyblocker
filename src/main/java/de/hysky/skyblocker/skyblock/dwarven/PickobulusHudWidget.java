package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@RegisterWidget
public class PickobulusHudWidget extends ComponentBasedWidget {
	private static final MutableText TITLE = Text.literal("Pickobulus").formatted(Formatting.BLUE, Formatting.BOLD);
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.GOLD_MINE, Location.DEEP_CAVERNS, Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS, Location.GLACITE_MINESHAFTS);
	@Nullable
	private static PickobulusHudWidget instance;

	public PickobulusHudWidget() {
		super(TITLE, Formatting.BLUE.getColorValue(), "hud_pickobulus");
		instance = this;
		update();
	}

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
		Text errorMessage = PickobulusHelper.getErrorMessage();
		if (errorMessage != null) {
			addComponent(new PlainTextComponent(errorMessage));
			return;
		}

		addComponent(new PlainTextComponent(Text.literal("Total Blocks: " + PickobulusHelper.getTotalBlocks())));

		int[] drops = PickobulusHelper.getDrops();
		for (PickobulusHelper.MiningDrop drop : PickobulusHelper.MiningDrop.values()) {
			int count = drops[drop.ordinal()];
			if (count > 0) {
				addComponent(new PlainTextComponent(Text.literal(drop.friendlyName() + ": " + count)));
			}
		}
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return AVAILABLE_LOCATIONS.contains(location) && SkyblockerConfigManager.get().mining.enablePickobulusHelper && PickobulusHelper.shouldRender();
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!AVAILABLE_LOCATIONS.contains(location)) return;
		SkyblockerConfigManager.get().mining.enablePickobulusHelper = enabled;
	}
}
