package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

@RegisterWidget
public class PickobulusHudWidget extends ElementBasedWidget {
	private static final MutableComponent TITLE = Component.literal("Pickobulus").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
	private static final Set<Location> AVAILABLE_LOCATIONS = EnumSet.of(Location.GOLD_MINE, Location.DEEP_CAVERNS, Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS, Location.GLACITE_MINESHAFTS);
	private static @Nullable PickobulusHudWidget instance;

	public PickobulusHudWidget() {
		super(TITLE, ChatFormatting.BLUE.getColor(), new Information("hud_pickobulus", Component.literal("Pickobulus HUD"), AVAILABLE_LOCATIONS));
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
			addComponent(new PlainTextElement(errorMessage));
			return;
		}

		addComponent(new PlainTextElement(Component.literal("Total Blocks: " + PickobulusHelper.getTotalBlocks())));

		int[] drops = PickobulusHelper.getDrops();
		for (PickobulusHelper.MiningDrop drop : PickobulusHelper.MiningDrop.values()) {
			int count = drops[drop.ordinal()];
			if (count > 0) {
				addComponent(new PlainTextElement(Component.literal(drop.friendlyName() + ": " + count)));
			}
		}
	}

	@Override
	public boolean shouldRender() {
		return PickobulusHelper.shouldRender();
	}
}
