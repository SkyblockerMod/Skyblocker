package de.hysky.skyblocker.skyblock.item.custom;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import io.github.moulberry.repo.constants.ResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class SkyblockItemModels {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(() -> {
			// Run this on the main thread
			CompletableFuture.runAsync(SkyblockItemModels::updateItemModels, Minecraft.getInstance());
		});
	}

	private static void updateItemModels() {
		SkyblockerConfigManager.update(config -> {
			try {
				ResourcePack.ItemModels itemModelsData = NEURepoManager.getConstants().getResourcePack().getItemModels();
				Iterator<Map.Entry<String, Identifier>> iterator = config.general.customItemModel.entrySet().iterator();

				while (iterator.hasNext()) {
					Map.Entry<String, Identifier> entry = iterator.next();
					Identifier itemModel = entry.getValue();
					String itemModelString = itemModel.toString();

					if (itemModel.getNamespace().equals(Utils.HYPIXEL_SKYBLOCK_NAMESPACE)) {
						// Remove item models that no longer exist
						if (itemModelsData.getRemoved().contains(itemModelString)) {
							iterator.remove();
						}

						// Skip updating model name if possible
						if (!itemModelsData.getRenamed().containsKey(itemModelString)) {
							continue;
						}

						// Update old item models recursively with some protection against circular map references
						String currentModel = itemModelsData.getRenamed().get(itemModelString);
						Set<String> visited = new HashSet<>();
						visited.add(itemModelString);

						String nextModel = itemModelsData.getRenamed().get(currentModel);

						while (nextModel != null) {
							if (visited.contains(nextModel)) {
								// Circular reference so break loop
								break;
							}

							entry.setValue(Identifier.parse(nextModel));
							visited.add(nextModel);

							currentModel = nextModel;
							nextModel = itemModelsData.getRenamed().get(currentModel);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Skyblock Item Models] Failed to update item models", e);
			}
		});
	}
}
