package de.hysky.skyblocker.compatibility;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ResourcePackCompatibility {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static ResourcePackOptions options = ResourcePackOptions.EMPTY;

	@Init(priority = -1)
	public static void init() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(SkyblockerMod.id("pack_compatibility_listener"), new ReloadListener());
	}

	private static final class ReloadListener extends SimplePreparableReloadListener<List<Pair<String, ResourcePackOptions>>>  {
		private static final MetadataSectionType<String> ID_SERIALIZER = new MetadataSectionType<>(
				"resource_pack_id",
				Codec.STRING
		);
		private static final MetadataSectionType<ResourcePackOptions> SKYBLOCKER_SERIALIZER = new MetadataSectionType<>(
				"skyblocker",
				ResourcePackOptions.CODEC
		);

		@Override
		protected List<Pair<String, ResourcePackOptions>> prepare(ResourceManager manager, ProfilerFiller profiler) {
			return manager.listPacks().map(
					resourcePack -> {
						try {
							return Pair.of(resourcePack.getMetadataSection(ID_SERIALIZER), resourcePack.getMetadataSection(SKYBLOCKER_SERIALIZER));
						} catch (IOException e) {
							LOGGER.error("[Skyblocker ResourcePack Compat] Failed to parse resource pack metadata", e);
							return null;
						}
					}
			).toList();
		}

		@Override
		protected void apply(List<Pair<String, ResourcePackOptions>> prepared, ResourceManager manager, ProfilerFiller profiler) {
			if (prepared.stream().anyMatch(p -> "FURFSKY_GUI".equals(p.first()))) {
				options = new ResourcePackOptions(Optional.of(true), Optional.of(true), Optional.of(false));
				LOGGER.info("[Skyblocker ResourcePack Compat] FURFSKY_GUI detected. Enabling compatibility names.");
				return;
			}
			List<ResourcePackOptions> list = new ArrayList<>(prepared.stream().map(Pair::right).filter(Objects::nonNull).toList());
			if (!list.isEmpty()) {
				options = merge(null, list);
			} else {
				options = ResourcePackOptions.EMPTY;
			}
			LOGGER.info("[Skyblocker ResourcePack Compat] " + options.toString());

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
