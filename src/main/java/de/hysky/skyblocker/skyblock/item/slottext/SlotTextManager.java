package de.hysky.skyblocker.skyblock.item.slottext;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.WardrobeKeybinds;
import de.hysky.skyblocker.skyblock.bazaar.BazaarHelper;
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.galatea.TunerSolver;
import de.hysky.skyblocker.skyblock.dungeon.terminal.SameColorTerminal;
import de.hysky.skyblocker.skyblock.hunting.AttributeLevelHelper;
import de.hysky.skyblocker.skyblock.item.slottext.adders.BestiaryLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.CatacombsLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.ChoosePetLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.CollectionAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.CommunityShopAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.EnchantmentAbbreviationAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.EnchantmentLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.EssenceShopAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.EvolvingItemAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.HotfPerkLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.HotmPerkLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.HuntingToolkitIndicatorAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.MinionLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.NewYearCakeAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.PetLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.PotionLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.PowerStonesGuideAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.PrehistoricEggAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.RancherBootsSpeedAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.SkillLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.SkyblockGuideAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.SkyblockLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.StatsTuningAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.YourEssenceAdder;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.skyblock.radialMenu.RadialMenuScreen;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.CommonColors;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SlotTextManager {
	private static final SlotTextAdder[] adders = new SlotTextAdder[]{
			new EssenceShopAdder(),
			new EnchantmentAbbreviationAdder(),
			new EnchantmentLevelAdder(),
			new MinionLevelAdder(),
			new PetLevelAdder(),
			new ChoosePetLevelAdder(),
			new SkyblockLevelAdder(),
			new HotmPerkLevelAdder(),
			new HotfPerkLevelAdder(),
			new SkillLevelAdder(),
			new CatacombsLevelAdder.Dungeoneering(),
			new CatacombsLevelAdder.DungeonClasses(),
			new CatacombsLevelAdder.ReadyUp(),
			new RancherBootsSpeedAdder(),
			new PrehistoricEggAdder(),
			new PotionLevelAdder(),
			new CollectionAdder(),
			new CommunityShopAdder(),
			new YourEssenceAdder(),
			new PowerStonesGuideAdder(),
			new BazaarHelper(),
			new StatsTuningAdder(),
			TunerSolver.INSTANCE,
			ChocolateFactorySolver.INSTANCE,
			new EvolvingItemAdder(),
			new NewYearCakeAdder(),
			WardrobeKeybinds.INSTANCE,
			new SkyblockGuideAdder(),
			SameColorTerminal.INSTANCE,
			AttributeLevelHelper.INSTANCE,
			new BestiaryLevelAdder(),
			new HuntingToolkitIndicatorAdder()
	};
	private static final ArrayList<SlotTextAdder> currentScreenAdders = new ArrayList<>();
	private static final KeyMapping keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.skyblocker.slottext", GLFW.GLFW_KEY_LEFT_ALT, SkyblockerMod.KEYBINDING_CATEGORY));
	private static boolean keyHeld = false;

	private SlotTextManager() {
	}

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if ((screen instanceof AbstractContainerScreen<?> && Utils.isOnSkyblock()) || screen instanceof ProfileViewerScreen || screen instanceof RadialMenuScreen) {
				onScreenChange(screen);
				ScreenEvents.remove(screen).register(ignored -> currentScreenAdders.clear());
			}
			ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, input) -> {
				if (keyBinding.matches(input)) {
					SkyblockerConfigManager.get().uiAndVisuals.slotText.slotTextToggled = !SkyblockerConfigManager.get().uiAndVisuals.slotText.slotTextToggled;
					keyHeld = true;
				}
			});
			ScreenKeyboardEvents.afterKeyRelease(screen).register((screen1, input) -> {
				if (keyBinding.matches(input)) {
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
	public static List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		List<SlotText> text = new ObjectArrayList<>();
		if (currentScreenAdders.isEmpty() || !isEnabled()) return text;
		for (SlotTextAdder adder : currentScreenAdders) {
			text.addAll(adder.getText(slot, stack, slotId));
		}
		return text;
	}

	public static void renderSlotText(GuiGraphics context, Font textRenderer, Slot slot) {
		renderSlotText(context, textRenderer, slot, slot.getItem(), slot.index, slot.x, slot.y);
	}

	public static void renderSlotText(GuiGraphics context, Font textRenderer, @Nullable Slot slot, ItemStack stack, int slotId, int x, int y) {
		List<SlotText> textList = getText(slot, stack, slotId);
		if (textList.isEmpty()) return;
		Matrix3x2fStack matrices = context.pose();

		for (SlotText slotText : textList) {
			matrices.pushMatrix();
			int length = textRenderer.width(slotText.text());
			if (length > 16) {
				float scale = 16f / length;
				matrices.scale(scale, scale);
				// Both of these translations translate by (-x, -y, 0) and then by the correct scaling and translation.
				switch (slotText.position()) {
					case TOP_LEFT, TOP_RIGHT -> matrices.translate(x / scale - x, y / scale - y);
					case BOTTOM_LEFT, BOTTOM_RIGHT -> matrices.translate(x / scale - x, (y + 16f) / scale - textRenderer.lineHeight + 2f - y);
				}
			} else {
				switch (slotText.position()) {
					case TOP_LEFT -> { /*Do Nothing*/ }
					case TOP_RIGHT -> matrices.translate(16f - length, 0.0f);
					case BOTTOM_LEFT -> matrices.translate(0.0f, 16f - textRenderer.lineHeight + 2f);
					case BOTTOM_RIGHT -> matrices.translate(16f - length, 16f - textRenderer.lineHeight + 2f);
				}
			}
			context.drawString(textRenderer, slotText.text(), x, y, CommonColors.WHITE, true);
			matrices.popMatrix();
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
