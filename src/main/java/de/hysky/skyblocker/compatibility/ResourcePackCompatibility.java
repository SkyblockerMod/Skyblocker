package de.hysky.skyblocker.compatibility;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

public class ResourcePackCompatibility {

	public static final Logger LOGGER = LogUtils.getLogger();

	public static ResourcePackOptions options = ResourcePackOptions.EMPTY;

	@Init(priority = -1)
	public static void init() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ReloadListener());
	}

	private static final class ReloadListener extends SinglePreparationResourceReloader<List<Pair<String, ResourcePackOptions>>> implements IdentifiableResourceReloadListener {

		private static final ResourceMetadataSerializer<String> ID_SERIALIZER = new ResourceMetadataSerializer<>(
				"resource_pack_id",
				Codec.STRING
		);
		private static final ResourceMetadataSerializer<ResourcePackOptions> SKYBLOCKER_SERIALIZER = new ResourceMetadataSerializer<>(
				"skyblocker",
				ResourcePackOptions.CODEC
		);

		@Override
		public Identifier getFabricId() {
			return Identifier.of(SkyblockerMod.NAMESPACE, "reload_listener");
		}

		@Override
		protected List<Pair<String, ResourcePackOptions>> prepare(ResourceManager manager, Profiler profiler) {
			return manager.streamResourcePacks().map(
					resourcePack -> {
						try {
							return Pair.of(resourcePack.parseMetadata(ID_SERIALIZER), resourcePack.parseMetadata(SKYBLOCKER_SERIALIZER));
						} catch (IOException e) {
							LOGGER.error("Failed to parse resource pack metadata", e);
							return null;
						}
					}
			).toList();
		}

		@Override
		protected void apply(List<Pair<String, ResourcePackOptions>> prepared, ResourceManager manager, Profiler profiler) {
			if (prepared.stream().anyMatch(p -> "FURFSKY_GUI".equals(p.first()))) {
				options = new ResourcePackOptions(Optional.of(true), Optional.of(true), Optional.of(false));
				LOGGER.info("FURFSKY_GUI detected. Enabling compatibility names.");
				return;
			}
			List<ResourcePackOptions> list = new ArrayList<>(prepared.stream().map(Pair::right).filter(Objects::nonNull).toList());
			if (!list.isEmpty()) {
				options = merge(null, list);
			} else {
				options = ResourcePackOptions.EMPTY;
			}
			LOGGER.info(options.toString());

		}
	}

	public record ResourcePackOptions(
		Optional<Boolean> renameAuctionBrowser,
		Optional<Boolean> renameInventoryScreen,
		Optional<Boolean> renameCraftingTable
	) {
		public static final ResourcePackOptions EMPTY = new ResourcePackOptions(Optional.empty(), Optional.empty(), Optional.empty());

		public static final Codec<ResourcePackOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("rename_auction_browser").forGetter(ResourcePackOptions::renameAuctionBrowser),
				Codec.BOOL.optionalFieldOf("rename_inventory_screen").forGetter(ResourcePackOptions::renameInventoryScreen),
				Codec.BOOL.optionalFieldOf("rename_crafting_table").forGetter(ResourcePackOptions::renameCraftingTable)
		).apply(instance, ResourcePackOptions::new));
	}

	public static ResourcePackOptions merge(@Nullable ResourcePackOptions a, List<ResourcePackOptions> b) {
		ResourcePackOptions above = b.removeFirst();
		if (a == null) {
			return b.isEmpty() ? above : merge(above, b);
		}
		ResourcePackOptions merged = new ResourcePackOptions(
				above.renameAuctionBrowser().isPresent() ? above.renameAuctionBrowser() : a.renameAuctionBrowser(),
				above.renameInventoryScreen().isPresent() ? above.renameInventoryScreen() : a.renameInventoryScreen(),
				above.renameCraftingTable().isPresent() ? above.renameCraftingTable() : a.renameCraftingTable()
		);
		if (b.isEmpty()) {
			return merged;
		}
		return merge(merged, b);
	}
}
