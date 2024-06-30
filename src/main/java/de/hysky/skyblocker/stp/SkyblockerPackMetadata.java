package de.hysky.skyblocker.stp;

import java.util.Optional;

import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.dynamic.Range;

public record SkyblockerPackMetadata(int skyblockerFormat, Optional<Range<Integer>> supportedFormats, @UnknownNullability ResourcePackCompatibility compatibility) {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Codec<SkyblockerPackMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("skyblocker_format").forGetter(SkyblockerPackMetadata::skyblockerFormat),
			Range.createCodec(Codec.INT).lenientOptionalFieldOf("skyblocker_supported_formats").forGetter(SkyblockerPackMetadata::supportedFormats))
			.apply(instance, SkyblockerPackMetadata::new));
	public static final ResourceMetadataSerializer<SkyblockerPackMetadata> SERIALIZER = ResourceMetadataSerializer.fromCodec(SkyblockerMod.NAMESPACE, CODEC);
	public static final int STP_VERSION = 1;

	private SkyblockerPackMetadata(int skyblockerFormat, Optional<Range<Integer>> supportedFormats) {
		this(skyblockerFormat, supportedFormats, null);
	}

	public SkyblockerPackMetadata withCompatibility(String packId) {
		return new SkyblockerPackMetadata(this.skyblockerFormat, this.supportedFormats, ResourcePackCompatibility.from(getSupportedFormats(packId, this), STP_VERSION));
	}

	private static Range<Integer> getSupportedFormats(String packId, SkyblockerPackMetadata skyblockerPackMetadata) {
		int skyblockerFormat = skyblockerPackMetadata.skyblockerFormat();

		if (skyblockerPackMetadata.supportedFormats().isEmpty()) {
			return new Range<>(skyblockerFormat);
		} else {
			Range<Integer> supportedFormats = skyblockerPackMetadata.supportedFormats().get();

			if (!supportedFormats.contains(skyblockerFormat)) {
				LOGGER.warn("[Skyblocker Pack Metadata] Pack {} declared support for versions {} but declared main format is {}, defaulting to {}", packId, supportedFormats, skyblockerFormat, skyblockerFormat);

				return new Range<>(skyblockerFormat);
			} else {
				return supportedFormats;
			}
		}
	}
}
