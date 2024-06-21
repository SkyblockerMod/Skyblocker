package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BazaarHelper extends ContainerSolver {
	public BazaarHelper() {
		super("Your Bazaar Orders");
	}

	public static KeyBinding BazaarLookup;
	public static KeyBinding BazaarRefresh;
	public static String itemName;
	public static void init() {
		BazaarLookup = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.bazaarLookup",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F3,
				"key.categories.skyblocker"
		));
		BazaarRefresh = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.bazaarRefresh",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_Z,
				"key.categories.skyblocker"
		));
	}

	public static void BazaarLookup(@NotNull Slot slot) {
		if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().general.bazaarLookup.enableBazaarLookup) return;
		String itemText = String.valueOf(ItemUtils.getItemId(slot.getStack()));
		NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(itemText);
		if (neuItem != null && TooltipInfoType.BAZAAR.getData() != null) {
			itemName = Formatting.strip(neuItem.getDisplayName());
			MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName);
		}
		else {
            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.general.bazaarLookup.failedBazaarLookup")));
		}
	}

	//maybe rename to ItemTooltipPriceRefresh? since updating more than BZ?
	public static void BazaarRefresh() {
		assert MinecraftClient.getInstance().player != null;
		assert MinecraftClient.getInstance().currentScreen != null;
		if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().misc.bazaarRefresh.enableBazaarRefresh) return;
		List<CompletableFuture<Void>> futureList = new ArrayList<>();
		TooltipInfoType.NPC.downloadIfEnabled(futureList);
		TooltipInfoType.BAZAAR.downloadIfEnabled(futureList);
		TooltipInfoType.LOWEST_BINS.downloadIfEnabled(futureList);
		TooltipInfoType.ONE_DAY_AVERAGE.download(futureList);
		TooltipInfoType.THREE_DAY_AVERAGE.download(futureList);
        MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.misc.debugOptions.yesBazaarRefresh")));
	}
}
