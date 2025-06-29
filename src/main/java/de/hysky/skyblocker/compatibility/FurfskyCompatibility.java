package de.hysky.skyblocker.compatibility;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collection;

public class FurfskyCompatibility {

	public static final Logger LOGGER = LogUtils.getLogger();

	public static boolean isFurfskyPresent = false;

	@Init(priority = -1)
	public static void init() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ReloadListener());
	}

	private static final class ReloadListener extends SinglePreparationResourceReloader<Collection<String>> implements IdentifiableResourceReloadListener {

		private static final ResourceMetadataSerializer<String> ID_SERIALIZER = new ResourceMetadataSerializer<>(
				"resource_pack_id",
				Codec.STRING
		);

		@Override
		public Identifier getFabricId() {
			return Identifier.of(SkyblockerMod.NAMESPACE, "resource_pack_id_reload_listener");
		}

		@Override
		protected Collection<String> prepare(ResourceManager manager, Profiler profiler) {
			isFurfskyPresent = false;
			return manager.streamResourcePacks().map(
					resourcePack -> {
						try {
							return resourcePack.parseMetadata(ID_SERIALIZER);
						} catch (IOException e) {
							LOGGER.error("Failed to parse resource pack metadata", e);
							return null;
						}
					}
			).toList();
		}

		@Override
		protected void apply(Collection<String> prepared, ResourceManager manager, Profiler profiler) {
			isFurfskyPresent = prepared.contains("FURFSKY_FULL");
			if (isFurfskyPresent) {
				LOGGER.info("Furfsky detected. Enabling compatibility names.");
			}
		}
	}
}
