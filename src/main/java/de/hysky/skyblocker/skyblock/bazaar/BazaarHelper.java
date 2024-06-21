package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BazaarHelper extends SimpleSlotTextAdder {
    private static final Pattern FILLED_PATTERN = Pattern.compile("Filled: \\S+ \\(?([\\d.]+)%\\)?!?");
    private static final int RED = 0xe60b1e;
    private static final int YELLOW = 0xe6ba0b;
    private static final int GREEN = 0x1ee60b;

    public BazaarHelper() {
        super("(?:Co-op|Your) Bazaar Orders");
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

    @Override
    public boolean isEnabled() {
        return SkyblockerConfigManager.get().helpers.bazaar.enableBazaarHelper;
    }

    public static void BazaarLookup(@NotNull Slot slot) {
        if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().general.bazaarLookup.enableBazaarLookup) return;
        String itemText = String.valueOf(ItemUtils.getItemId(slot.getStack()));
        NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(itemText);
        if (neuItem != null) {
            itemName = Formatting.strip(neuItem.getDisplayName());
        }
        MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName);

    }

	//maybe rename to ItemTooltipPriceRefresh? since updating more than BZ?
	public static void BazaarRefresh() {
		if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().general.bazaarLookup.enableBazaarRefresh) return;
		List<CompletableFuture<Void>> futureList = new ArrayList<>();
		TooltipInfoType.NPC.downloadIfEnabled(futureList);
		TooltipInfoType.BAZAAR.downloadIfEnabled(futureList);
		TooltipInfoType.LOWEST_BINS.downloadIfEnabled(futureList);
		TooltipInfoType.ONE_DAY_AVERAGE.download(futureList);
		TooltipInfoType.THREE_DAY_AVERAGE.download(futureList);
		MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.general.bazaarLookup.yesBazaarRefresh")));
	}
}
