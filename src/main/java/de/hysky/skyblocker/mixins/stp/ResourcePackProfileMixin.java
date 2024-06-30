package de.hysky.skyblocker.mixins.stp;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.injected.SkyblockerResourcePackProfile;
import de.hysky.skyblocker.stp.SkyblockerPackMetadata;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackProfile;

@Mixin(ResourcePackProfile.class)
public class ResourcePackProfileMixin implements SkyblockerResourcePackProfile {
	@Unique
	private SkyblockerPackMetadata skyblockerMetadata;

	@ModifyReturnValue(method = "create", at = @At("RETURN"))
	private static ResourcePackProfile skyblocker$injectSkyblockerMetadata(ResourcePackProfile original, @Local(argsOnly = true) ResourcePackInfo info, @Local(argsOnly = true) ResourcePackProfile.PackFactory packFactory) {
		if (original != null) {
			try (ResourcePack pack = packFactory.open(info)) {
				SkyblockerPackMetadata metadata = pack.parseMetadata(SkyblockerPackMetadata.SERIALIZER);

				original.setSkyblockerMetadata(metadata.withCompatibility(info.id()));
			} catch (Exception ignored) {}
		}

		return original;
	}

	@Override
	public void setSkyblockerMetadata(SkyblockerPackMetadata metadata) {
		this.skyblockerMetadata = metadata;
	}

	@Override
	@Nullable
	public SkyblockerPackMetadata getSkyblockerMetadata() {
		return this.skyblockerMetadata;
	}
}
