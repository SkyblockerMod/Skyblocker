package me.xmrvizzy.skyblocker.compatibility;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

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
		if (mixinClassName.endsWith("WorldRendererMixin") && OPTIFABRIC_LOADED) return false;

		return true;
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
