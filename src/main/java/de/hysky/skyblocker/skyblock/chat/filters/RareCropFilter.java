package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.SkyBlockIcons;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import de.hysky.skyblocker.utils.render.gui.BasicToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class RareCropFilter extends ChatPatternListener {
	private static final Map<String, String> IDS;
	private static final Map<String, FlexibleItemStack> ICONS = new HashMap<>();

	public RareCropFilter() {
		super("^RARE CROP!\\s+(?<crop>[\\w\\s]+)\\s+\\(\\+\\d+" + SkyBlockIcons.OVERBLOOM + "\\)$");
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

	static {
		IDS = new HashMap<>();
		IDS.put("Cropie", "CROPIE");
		IDS.put("Squash", "SQUASH");
		IDS.put("Fermento", "FERMENTO");
		IDS.put("Helianthus", "HELIANTHUS");
		IDS.put("Warty", "WARTY");
		IDS.put("Burrowing Spores", "BURROWING_SPORES");
		IDS.put("Overclocker 3000", "OVERCLOCKER_3000");
		IDS.put("Ethereal Vine", "ETHEREAL_VINE");
		IDS.put("Rarefinder Chip", "RAREFINDER_GARDEN_CHIP");
		// Seasoning has no item to display, so it's excluded here
		IDS.put("Cornucopia", "CORNUCOPIA");
		IDS.put("Carrot Zest", "CARROT_ZEST");
		IDS.put("Deepfries", "DEEPFRIES");
		IDS.put("Aggourdian", "AGGOURDIAN");
		IDS.put("Cane Knot", "CANE_KNOT");
		IDS.put("Melon Juice", "MELON_JUICE");
		IDS.put("Cactus Flower", "CACTUS_FLOWER");
		IDS.put("Designer Coffee Beans", "DESIGNER_COFFEE_BEANS");
		IDS.put("Feastfungus", "FEASTFUNGUS");
		IDS.put("Botroot", "BOTROOT");
		IDS.put("Salted Sunflower Seeds", "SALTED_SUNFLOWER_SEEDS");
		IDS.put("Crystalized Moonlight", "CRYSTALIZED_MOONLIGHT");
		IDS.put("Floral Gelatin", "FLORAL_GELATIN");
		// Wild Strawberry Dye and Ray of Helios are handled as rare drops instead
	}
}
