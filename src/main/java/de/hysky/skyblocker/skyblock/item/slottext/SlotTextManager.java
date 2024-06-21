package de.hysky.skyblocker.skyblock.item.slottext;

import de.hysky.skyblocker.skyblock.bazaar.BazaarHelper;
import de.hysky.skyblocker.skyblock.item.slottext.adders.*;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.AbstractSlotTextAdder;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SlotTextManager {
	private static final AbstractSlotTextAdder[] adders = new AbstractSlotTextAdder[]{
			new EssenceShopAdder(),
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
			new CommunityShopAdder(),
			new YourEssenceAdder(),
			new PowerStonesGuideAdder(),
			new BazaarHelper(),
			new StatsTuningAdder()
	};
	private static final ArrayList<AbstractSlotTextAdder> currentScreenAdders = new ArrayList<>();

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
		for (AbstractSlotTextAdder adder : adders) {
			if (!adder.isEnabled()) continue;
			if (adder.test(screen)) {
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
	public static List<SlotText> getText(@NotNull ItemStack itemStack, int slotId) {
		if (currentScreenAdders.isEmpty()) return List.of();
		for (AbstractSlotTextAdder adder : currentScreenAdders) {
			List<SlotText> text = adder.getText(itemStack, slotId);
			if (!text.isEmpty()) return text;
		}
		return List.of();
	}

	public static void renderSlotText(DrawContext context, TextRenderer textRenderer, Slot slot) {
		renderSlotText(context, textRenderer, slot.getStack(), slot.id, slot.x, slot.y);
	}

	public static void renderSlotText(DrawContext context, TextRenderer textRenderer, ItemStack itemStack, int slotId, int x, int y) {
		List<SlotText> textList = SlotTextManager.getText(itemStack, slotId);
		if (textList.isEmpty()) return;
		MatrixStack matrices = context.getMatrices();

		for (SlotText slotText : textList) {
			matrices.push();
			matrices.translate(0.0f, 0.0f, 200.0f);
			int length = textRenderer.getWidth(slotText.text());
			if (length > 16) {
				matrices.scale(16f / length, 16f / length, 1.0f);
				switch (slotText.position()) {
					case TOP_LEFT, TOP_RIGHT -> matrices.translate(x * length / 16f - x, (y * length / 16.0f) - y, 0.0f);
					case BOTTOM_LEFT, BOTTOM_RIGHT -> matrices.translate(x * length / 16f - x, ((y + 16f - textRenderer.fontHeight + 2f + 0.7f) * length / 16.0f) - y, 0.0f);
				}
			} else {
				switch (slotText.position()) {
					case TOP_LEFT -> { /*Do Nothing*/ }
					case TOP_RIGHT -> matrices.translate(16f - length, 0.0f, 0.0f);
					case BOTTOM_LEFT -> matrices.translate(0.0f, 16f - textRenderer.fontHeight + 2f, 0.0f);
					case BOTTOM_RIGHT -> matrices.translate(16f - length, 16f - textRenderer.fontHeight + 2f, 0.0f);
				}
			}
			context.drawText(textRenderer, slotText.text(), x, y, 0xFFFFFF, true);
			matrices.pop();
		}
	}
}
