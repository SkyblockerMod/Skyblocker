package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.hysky.skyblocker.mixins.accessors.OutlineBufferSourceAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Util;

public class GlowRenderer implements AutoCloseable {
	private static GlowRenderer instance = null;
	private final Minecraft minecraft;
	private final OutlineBufferSource glowBufferSource;
	private GpuTexture glowDepthTexture;
	private GpuTextureView glowDepthTextureView;
	private boolean isRenderingGlow = false;

	private GlowRenderer() {
		this.minecraft = Minecraft.getInstance();
		this.glowBufferSource = Util.make(new OutlineBufferSource(), outlineBufferSource -> {
			((OutlineBufferSourceAccessor) outlineBufferSource).setOutlineBufferSource(new GlowBufferSource(new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE)));
		});
	}

	public static GlowRenderer getInstance() {
		if (instance == null) {
			instance = new GlowRenderer();
		}

		return instance;
	}

	public OutlineBufferSource getGlowBufferSource() {
		return this.glowBufferSource;
	}

	public void updateGlowDepthTexDepth() {
		tryUpdateDepthTexture();
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(this.minecraft.getMainRenderTarget().getDepthTexture(), this.glowDepthTexture, 0, 0, 0, 0, 0, this.glowDepthTexture.getWidth(0), this.glowDepthTexture.getHeight(0));
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
		int neededWidth = this.minecraft.getWindow().getWidth();
		int neededHeight = this.minecraft.getWindow().getHeight();

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
		}
	}

	@Override
	public void close() {
		if (this.glowDepthTexture != null) {
			this.glowDepthTexture.close();
			this.glowDepthTextureView.close();
		}
	}

	private static class GlowBufferSource extends MultiBufferSource.BufferSource {

		protected GlowBufferSource(ByteBufferBuilder sharedBuffer) {
			super(sharedBuffer, Object2ObjectSortedMaps.emptyMap());
		}

		@Override
		public VertexConsumer getBuffer(RenderType renderType) {
			if (this.startedBuilders.get(renderType) != null && !renderType.canConsolidateConsecutiveGeometry()) {
				getInstance().startRenderingGlow();
				VertexConsumer buffer = super.getBuffer(renderType);
				getInstance().stopRenderingGlow();

				return buffer;
			}

			return super.getBuffer(renderType);
		}

		@Override
		public void endBatch(RenderType type) {
			getInstance().startRenderingGlow();
			super.endBatch(type);
			getInstance().stopRenderingGlow();
		}
	}
}
