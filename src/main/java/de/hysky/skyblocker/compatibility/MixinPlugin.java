package de.hysky.skyblocker.compatibility;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
	private static final boolean OPTIFABRIC_LOADED = FabricLoader.getInstance().isModLoaded("optifabric");
	private static final boolean FCWLS_LOADED = FabricLoader.getInstance().isModLoaded("forcecloseworldloadingscreen");

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
		if (OPTIFABRIC_LOADED && mixinClassName.endsWith("WorldRendererMixin")) return false;

		//Force Close World Screen Compatibility
		if (FCWLS_LOADED && mixinClassName.endsWith("MinecraftClientScreenMixin")) {
			String mixinCodeSource = MixinEnvironment.class.getProtectionDomain().getCodeSource().getLocation().toString();

			return !mixinCodeSource.contains("0.12.5+mixin.0.8.5");
		}

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
