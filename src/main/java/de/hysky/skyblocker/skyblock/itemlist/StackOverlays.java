package de.hysky.skyblocker.skyblock.itemlist;

import java.nio.file.Files;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.NEURepoFile;
import io.github.moulberry.repo.data.NEUItem;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;

/**
 * Handles applying "overlays" to modern {@code ItemStack}s from the NEU Repository. Overlays are already in the modern components
 * format according to the data version of the given directory which matches the vanilla data version number. This system allows the
 * NEU Repository to provide support for modern items while maintaining backwards compatibility with 1.8 and older modern releases (say 1.21.1).
 * 
 * Note that overlays do not contain all the original components, lore and custom name are notably left out for ease of maintenance.
 */
public class StackOverlays {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** Data Version for 1.21.5 */
	private static final int DATA_VERSION = 4325;
	private static final String OVERLAY_DIRECTORY = "itemsOverlay/" + DATA_VERSION;

	/**
	 * Applies the necessary overlay for the {@code stack} if applicable.
	 */
	protected static void applyOverlay(NEUItem neuItem, ItemStack stack) {
		try {
			NEURepoFile file = NEURepoManager.NEU_REPO.file(OVERLAY_DIRECTORY + "/" + neuItem.getSkyblockItemId() + ".snbt");

			//The returned file is null if it does not exist
			if (file != null) {
				//Read the overlay file and parse an ItemStack from it
				String overlayData = Files.readString(file.getFsPath());
				ItemStack overlayStack = ItemStack.CODEC.parse(NbtOps.INSTANCE, StringNbtReader.readCompound(overlayData))
						.setPartial(ItemStack.EMPTY)
						.resultOrPartial(error -> logParseError(neuItem, error))
						.get();

				if (!overlayStack.isEmpty()) {
					//Apply the component changes from the overlay stack
					ComponentChanges changes = overlayStack.getComponentChanges();
					stack.applyChanges(changes);
				}
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Stack Overlays] Failed to apply stack overlay! Item: {}", neuItem.getSkyblockItemId(), e);
		}
	}

	private static void logParseError(NEUItem neuItem, String message) {
		LOGGER.error("[Skyblocker Stack Overlays] Failed to parse item \"{}\". Error: {}", neuItem.getSkyblockItemId(), message);
	}
}
