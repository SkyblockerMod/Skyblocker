package de.hysky.skyblocker.compatibility;

import net.fabricmc.loader.api.FabricLoader;

import org.apache.commons.lang3.SystemUtils;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
	private static final boolean OPTIFABRIC_LOADED = FabricLoader.getInstance().isModLoaded("optifabric");

	@Override
	public void onLoad(String mixinPackage) {
		//Do nothing
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return switch (mixinClassName) {
			case String s when s.endsWith("WorldRendererMixin") -> !OPTIFABRIC_LOADED;
			case String s when s.endsWith("GlCommandEncoderMixin") -> SystemUtils.IS_OS_MAC && "aarch64".equalsIgnoreCase(SystemUtils.OS_ARCH);

			default -> true;
		};
    }

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
		//Do nothing
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		//Do nothing
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		//Do nothing
	}
}
