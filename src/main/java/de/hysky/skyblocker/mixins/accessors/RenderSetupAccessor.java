package de.hysky.skyblocker.mixins.accessors;

import java.util.Map;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.rendertype.RenderSetup;

@Mixin(RenderSetup.class)
public interface RenderSetupAccessor {
	@Accessor
	RenderPipeline getPipeline();

	@Accessor
	Map<String, RenderSetup.TextureBinding> getTextures();

	@Accessor
	RenderSetup.OutlineProperty getOutlineProperty();
}
