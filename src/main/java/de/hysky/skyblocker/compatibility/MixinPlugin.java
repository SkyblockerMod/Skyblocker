package de.hysky.skyblocker.compatibility;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class MixinPlugin implements IMixinConfigPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(MixinPlugin.class);
	private static final boolean OPTIFABRIC_LOADED = FabricLoader.getInstance().isModLoaded("optifabric");
	private static final String YACL_VERSION = FabricLoader.getInstance().getModContainer("yet_another_config_lib_v3").get().getMetadata().getVersion().getFriendlyString();

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
		
		//YACL#103 Patch
		if (mixinClassName.endsWith("DoubleFieldControllerMixin") || mixinClassName.endsWith("FloatFieldControllerMixin") || mixinClassName.endsWith("IntegerFieldControllerMixin") || mixinClassName.endsWith("LongFieldControllerMixin") || mixinClassName.endsWith("NumberFieldControllerMixin")) {
			if (YACL_VERSION.equals("3.2.1+1.20.2")) {
				LOGGER.info("[Skyblocker] Applying patch for " + targetClassName + " from " + mixinClassName);
			} else {
				LOGGER.info("[Skyblocker] Skipping patch on " + targetClassName + " due to an Unknown YACL version being found! Version: {}", YACL_VERSION);
				
				return false;
			}
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
