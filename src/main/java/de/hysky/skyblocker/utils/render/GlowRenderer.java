package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.hysky.skyblocker.mixins.accessors.OutlineBufferSourceAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;

public class GlowRenderer implements AutoCloseable {
	private static GlowRenderer instance = null;
	private final Minecraft client;
	private final OutlineBufferSource glowOutlineVertexConsumers;
	private GpuTexture glowDepthTexture;
	private GpuTextureView glowDepthTextureView;
	private boolean isRenderingGlow = false;

	private GlowRenderer() {
		this.client = Minecraft.getInstance();
		this.glowOutlineVertexConsumers = Util.make(new OutlineBufferSource(), outlineVertexConsumers -> {
			((OutlineBufferSourceAccessor) outlineVertexConsumers).setOutlineBufferSource(new GlowVertexConsumerProvider(new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE)));
		});
	}

	public static GlowRenderer getInstance() {
		if (instance == null) {
			instance = new GlowRenderer();
		}

		return instance;
	}

	public OutlineBufferSource getGlowVertexConsumers() {
		return this.glowOutlineVertexConsumers;
	}

	public void updateGlowDepthTexDepth() {
		tryUpdateDepthTexture();
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(this.client.getMainRenderTarget().getDepthTexture(), this.glowDepthTexture, 0, 0, 0, 0, 0, this.glowDepthTexture.getWidth(0), this.glowDepthTexture.getHeight(0));
	}

	private void startRenderingGlow() {
		this.isRenderingGlow = true;
		RenderSystem.outputDepthTextureOverride = this.glowDepthTextureView;
	}

	private void stopRenderingGlow() {
		this.isRenderingGlow = false;
		RenderSystem.outputDepthTextureOverride = null;
	}

	public static boolean isRenderingGlow() {
		//Iris can load this class very early, so this is a static method that does not initialize the instance
		//to avoid crashing with it.
		return instance != null ? instance.isRenderingGlow : false;
	}

	private void tryUpdateDepthTexture() {
		int neededWidth = this.client.getWindow().getWidth();
		int neededHeight = this.client.getWindow().getHeight();

		//If the texture hasn't been created or needs resizing
		if (this.glowDepthTexture == null || this.glowDepthTexture.getWidth(0) != neededWidth || this.glowDepthTexture.getHeight(0) != neededHeight) {
			GpuDevice device = RenderSystem.getDevice();

			//Delete the textures if they exist
			if (this.glowDepthTexture != null) {
				this.glowDepthTexture.close();
				this.glowDepthTextureView.close();
			}

			this.glowDepthTexture = device.createTexture(() -> "Skyblocker Glow Depth Tex", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.DEPTH32, neededWidth, neededHeight, 1, 1);
			this.glowDepthTextureView = device.createTextureView(this.glowDepthTexture);
			this.glowDepthTexture.setTextureFilter(FilterMode.NEAREST, false);
			this.glowDepthTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
		}
	}

	@Override
	public void close() {
		if (this.glowDepthTexture != null) {
			this.glowDepthTexture.close();
			this.glowDepthTextureView.close();
		}
	}

	private static class GlowVertexConsumerProvider extends MultiBufferSource.BufferSource {

		protected GlowVertexConsumerProvider(ByteBufferBuilder allocator) {
			super(allocator, Object2ObjectSortedMaps.emptyMap());
		}

		@Override
		public VertexConsumer getBuffer(RenderType renderLayer) {
			if (this.startedBuilders.get(renderLayer) != null && !renderLayer.canConsolidateConsecutiveGeometry()) {
				getInstance().startRenderingGlow();
				VertexConsumer buffer = super.getBuffer(renderLayer);
				getInstance().stopRenderingGlow();

				return buffer;
			}

			return super.getBuffer(renderLayer);
		}

		@Override
		public void endBatch(RenderType layer) {
			getInstance().startRenderingGlow();
			super.endBatch(layer);
			getInstance().stopRenderingGlow();
		}
	}
}
