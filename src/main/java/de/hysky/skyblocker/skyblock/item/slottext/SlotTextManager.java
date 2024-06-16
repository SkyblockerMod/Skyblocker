package de.hysky.skyblocker.skyblock.item.slottext;

import de.hysky.skyblocker.skyblock.item.slottext.adders.*;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SlotTextManager {
	private static final SlotTextAdder[] adders = new SlotTextAdder[]{
			new EnchantmentLevelAdder(),
			new MinionLevelAdder(),
			new PetLevelAdder(),
			new SkyblockLevelAdder(),
			new SkillLevelAdder(),
			new CatacombsLevelAdder.Dungeoneering(),
			new CatacombsLevelAdder.DungeonClasses(),
			new CatacombsLevelAdder.ReadyUp(),
			new RancherBootsSpeedAdder(),
			new AttributeShardAdder(),
			new PrehistoricEggAdder(),
			new PotionLevelAdder(),
			new CollectionAdder(),
			new CommunityShopAdder()
	};
	private static final ArrayList<SlotTextAdder> currentScreenAdders = new ArrayList<>();

	private SlotTextManager() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (screen instanceof HandledScreen<?> handledScreen && Utils.isOnSkyblock()) {
				onScreenChange(handledScreen);
				ScreenEvents.remove(screen).register(ignored -> currentScreenAdders.clear());
			}
		});
	}

	private static void onScreenChange(HandledScreen<?> screen) {
		for (SlotTextAdder adder : adders) {
			if (adder.isEnabled() && adder.test(screen)) {
				currentScreenAdders.add(adder);
			}
		}
	}

	/**
	 * The returned text is rendered on top of the slot. The text will be scaled if it doesn't fit in the slot,
	 * but 3 characters should be seen as the maximum to keep it readable and in place as it tends to move around when scaled.
	 *
	 * @implNote Only the first adder that returns a non-null text will be used.
	 * The order of the adders remains the same as they were added to the {@link SlotTextManager#adders} array.
	 */
	@NotNull
	public static List<SlotText> getText(Slot slot) {
		if (currentScreenAdders.isEmpty()) return List.of();
		for (SlotTextAdder adder : currentScreenAdders) {
			List<SlotText> text = adder.getText(slot);
			if (!text.isEmpty()) return text;
		}
		return List.of();
	}
}
