package de.hysky.skyblocker.skyblock.chat.filters;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.SkyBlockIcons;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import de.hysky.skyblocker.utils.render.gui.BasicToast;

import static java.util.Map.entry;

public class RareCropFilter extends ChatPatternListener {
	private static final Map<String, String> IDS = Map.ofEntries(
			entry("Cropie", "CROPIE"),
			entry("Squash", "SQUASH"),
			entry("Fermento", "FERMENTO"),
			entry("Helianthus", "HELIANTHUS"),
			entry("Warty", "WARTY"),
			entry("Burrowing Spores", "BURROWING_SPORES"),
			entry("Overclocker 3000", "OVERCLOCKER_3000"),
			entry("Ethereal Vine", "ETHEREAL_VINE"),
			entry("Rarefinder Chip", "RAREFINDER_GARDEN_CHIP"),
			// Seasoning has no item to display, so it's excluded here
			entry("Cornucopia", "CORNUCOPIA"),
			entry("Carrot Zest", "CARROT_ZEST"),
			entry("Deepfries", "DEEPFRIES"),
			entry("Aggourdian", "AGGOURDIAN"),
			entry("Cane Knot", "CANE_KNOT"),
			entry("Melon Juice", "MELON_JUICE"),
			entry("Cactus Flower", "CACTUS_FLOWER"),
			entry("Designer Coffee Beans", "DESIGNER_COFFEE_BEANS"),
			entry("Feastfungus", "FEASTFUNGUS"),
			entry("Botroot", "BOTROOT"),
			entry("Salted Sunflower Seeds", "SALTED_SUNFLOWER_SEEDS"),
			entry("Crystalized Moonlight", "CRYSTALIZED_MOONLIGHT"),
			entry("Floral Gelatin", "FLORAL_GELATIN")
			// Wild Strawberry Dye and Ray of Helios are handled as rare drops instead
	);
	private static final Map<String, FlexibleItemStack> ICONS = new HashMap<>();

	public RareCropFilter() {
		super("^RARE CROP!\\s+(?<crop>[\\w\\s]+)\\s+\\(\\+\\d+(?:\\.\\d+)?" + SkyBlockIcons.OVERBLOOM + "\\).*");
	}

	private @Nullable ItemStack getCropIcon(Matcher matcher) {
		String skyblockId = IDS.get(matcher.group("crop"));
		if (skyblockId == null) return null;
		if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return ItemUtils.getItemIdPlaceholder(skyblockId).getStack();
		return ICONS.computeIfAbsent(skyblockId, id -> ItemRepository.getItemStack(id, ItemUtils.getItemIdPlaceholder(id))).getStack();
	}

	@Override
	public boolean onMatch(Component message, Matcher matcher) {
		if (SkyblockerConfigManager.get().chat.hideRareCrops == ChatFilterResult.TOAST) {
			Minecraft.getInstance().gui.toastManager().addToast(new BasicToast(message, (long) (SkyblockerConfigManager.get().chat.toastDisplayDuration * 1000L), getCropIcon(matcher)));
		}
		return true;
	}

	@Override
	public ChatFilterResult state() {
		if (SkyblockerConfigManager.get().chat.hideRareCrops == ChatFilterResult.TOAST)
			return ChatFilterResult.FILTER;
		else
			return SkyblockerConfigManager.get().chat.hideRareCrops;
	}
}
