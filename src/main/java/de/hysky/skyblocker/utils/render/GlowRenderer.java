package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.mixins.accessors.OutlineVertexConsumerProviderAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Util;

public class GlowRenderer {
	public static final OutlineVertexConsumerProvider GLOW_OUTLINE_VERTEX_CONSUMERS = Util.make(new OutlineVertexConsumerProvider(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers()), outlineVertexConsumers -> {
		((OutlineVertexConsumerProviderAccessor) outlineVertexConsumers).setPlainDrawer(new GlowVertexConsumerProvider(new BufferAllocator(RenderLayer.DEFAULT_BUFFER_SIZE)));
	});
	private static boolean isRenderingGlow = false;

	private static void startRenderingGlow() {
		isRenderingGlow = true;
		RenderSystem.outputDepthTextureOverride = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
	}

	private static void stopRenderingGlow() {
		isRenderingGlow = false;
		RenderSystem.outputDepthTextureOverride = null;
	}

	public static boolean isRenderingGlow() {
		return isRenderingGlow;
	}

	private static class GlowVertexConsumerProvider extends VertexConsumerProvider.Immediate {

		protected GlowVertexConsumerProvider(BufferAllocator allocator) {
			super(allocator, Object2ObjectSortedMaps.emptyMap());
		}

		@Override
		public VertexConsumer getBuffer(RenderLayer renderLayer) {
			if (this.pending.get(renderLayer) != null && !renderLayer.areVerticesNotShared()) {
				startRenderingGlow();
				VertexConsumer buffer = super.getBuffer(renderLayer);
				stopRenderingGlow();

				return buffer;
			}

			return super.getBuffer(renderLayer);
		}

		@Override
		public void draw(RenderLayer layer) {
			startRenderingGlow();
			super.draw(layer);
			stopRenderingGlow();
		}
	}
}
