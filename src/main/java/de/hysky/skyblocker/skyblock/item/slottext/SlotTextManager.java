package de.hysky.skyblocker.skyblock.item.slottext;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.WardrobeKeybinds;
import de.hysky.skyblocker.skyblock.bazaar.BazaarHelper;
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.item.slottext.adders.*;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SlotTextManager {
	private static final SlotTextAdder[] adders = new SlotTextAdder[]{
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
			new StatsTuningAdder(),
			ChocolateFactorySolver.INSTANCE,
			new EvolvingItemAdder(),
			new NewYearCakeAdder(),
			WardrobeKeybinds.INSTANCE
	};
	private static final ArrayList<SlotTextAdder> currentScreenAdders = new ArrayList<>();
	private static final KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.skyblocker.slottext", GLFW.GLFW_KEY_LEFT_ALT, "key.categories.skyblocker"));
	private static boolean keyHeld = false;

	private SlotTextManager() {
	}

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if ((screen instanceof HandledScreen<?> && Utils.isOnSkyblock()) || screen instanceof ProfileViewerScreen) {
				onScreenChange(screen);
				ScreenEvents.remove(screen).register(ignored -> currentScreenAdders.clear());
			}
			ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
				if (keyBinding.matchesKey(key, scancode)) {
					SkyblockerConfigManager.get().uiAndVisuals.slotText.slotTextToggled = !SkyblockerConfigManager.get().uiAndVisuals.slotText.slotTextToggled;
					keyHeld = true;
				}
			});
			ScreenKeyboardEvents.afterKeyRelease(screen).register((screen1, key, scancode, modifiers) -> {
				if (keyBinding.matchesKey(key, scancode)) {
					keyHeld = false;
				}
			});
		});
	}

	private static void onScreenChange(Screen screen) {
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
	 * @implNote The order of the adders remains the same as they were added to the {@link SlotTextManager#adders} array.
	 *           It is the implementors' duty to ensure they do not add slot text to the same location as other adders on the same slot.
	 */
	@NotNull
	public static List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		List<SlotText> text = new ObjectArrayList<>();
		if (currentScreenAdders.isEmpty() || !isEnabled()) return text;
		for (SlotTextAdder adder : currentScreenAdders) {
			text.addAll(adder.getText(slot, stack, slotId));
		}
		return text;
	}

	public static void renderSlotText(DrawContext context, TextRenderer textRenderer, @NotNull Slot slot) {
		renderSlotText(context, textRenderer, slot, slot.getStack(), slot.id, slot.x, slot.y);
	}

	public static void renderSlotText(DrawContext context, TextRenderer textRenderer, @Nullable Slot slot, ItemStack stack, int slotId, int x, int y) {
		List<SlotText> textList = getText(slot, stack, slotId);
		if (textList.isEmpty()) return;
		MatrixStack matrices = context.getMatrices();

		for (SlotText slotText : textList) {
			matrices.push();
			matrices.translate(0.0f, 0.0f, 200.0f);
			int length = textRenderer.getWidth(slotText.text());
			if (length > 16) {
				float scale = 16f / length;
				matrices.scale(scale, scale, 1.0f);
				// Both of these translations translate by (-x, -y, 0) and then by the correct scaling and translation.
				switch (slotText.position()) {
					case TOP_LEFT, TOP_RIGHT -> matrices.translate(x / scale - x, y / scale - y, 0.0f);
					case BOTTOM_LEFT, BOTTOM_RIGHT -> matrices.translate(x / scale - x, (y + 16f) / scale - textRenderer.fontHeight + 2f - y, 0.0f);
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

	public static Stream<SlotTextAdder> getAdderStream() {
		return Arrays.stream(adders);
	}

	public static boolean isEnabled(String adderId) {
		return SkyblockerConfigManager.get().uiAndVisuals.slotText.textEnabled.getOrDefault(adderId, true);
	}

	public static boolean isEnabled() {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.slotText.slotTextMode) {
			case ENABLED -> true;
			case DISABLED -> false;
			case PRESS_TO_TOGGLE -> SkyblockerConfigManager.get().uiAndVisuals.slotText.slotTextToggled;
			case HOLD_TO_HIDE -> !keyHeld;
			case HOLD_TO_SHOW -> keyHeld;
		};
	}
}
