package de.hysky.skyblocker.compatibility;

import net.fabricmc.loader.api.FabricLoader;
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
		//OptiFabric Compatibility
        return !mixinClassName.endsWith("WorldRendererMixin") || !OPTIFABRIC_LOADED;
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
