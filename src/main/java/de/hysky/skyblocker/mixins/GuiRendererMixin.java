package de.hysky.skyblocker.mixins;

import java.util.Iterator;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.utils.render.gui.special.InstancedGuiElementRenderer;
import de.hysky.skyblocker.utils.render.gui.state.InstancedGuiElementRenderState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
	@Shadow
	@Final
	GuiRenderState state;
	@Shadow
	@Final
	private VertexConsumerProvider.Immediate vertexConsumers;
	@Unique
	private final Map<InstancedGuiElementRenderState, InstancedGuiElementRenderer<?>> instancedRenderers = new Object2ObjectOpenHashMap<>();

	@Inject(method = "prepareSpecialElement", at = @At("HEAD"), cancellable = true)
	private <T extends SpecialGuiElementRenderState> void skyblocker$instancedGuiElementRendering(SpecialGuiElementRenderState specialGuiElementRenderState, int windowScaleFactor, CallbackInfo ci) {
		if (specialGuiElementRenderState instanceof InstancedGuiElementRenderState instanced) {
			@SuppressWarnings("unchecked")
			InstancedGuiElementRenderer<InstancedGuiElementRenderState> renderer = (InstancedGuiElementRenderer<InstancedGuiElementRenderState>) this.instancedRenderers.computeIfAbsent(instanced, ignored -> instanced.newRenderer(this.vertexConsumers));
			renderer.render(instanced, this.state, windowScaleFactor);

			ci.cancel();
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void skyblocker$clearUnusedRenderers(CallbackInfo ci) {
		this.closeUnusedRenderers();
	}

	@Inject(method = "close", at = @At("TAIL"))
	public void skyblocker$closeInstancedRenderers(CallbackInfo ci) {
		this.instancedRenderers.values().forEach(SpecialGuiElementRenderer::close);
	}

	@Unique
	private void closeUnusedRenderers() {
		Iterator<Map.Entry<InstancedGuiElementRenderState, InstancedGuiElementRenderer<?>>> iterator = this.instancedRenderers.entrySet().iterator();

		while (iterator.hasNext()) {
			InstancedGuiElementRenderer<?> renderer = iterator.next().getValue();

			if (!renderer.usedThisFrame()) {
				renderer.close();
				iterator.remove();
			} else {
				renderer.resetUsedThisFrame();
			}
		}
	}
}
