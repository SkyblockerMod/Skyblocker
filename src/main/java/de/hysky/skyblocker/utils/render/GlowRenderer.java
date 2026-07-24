package de.hysky.skyblocker.utils.render;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

import net.minecraft.client.Minecraft;

public class GlowRenderer implements AutoCloseable {
	public static final GlowRenderer INSTANCE = new GlowRenderer();
	private final Minecraft minecraft;
	private @Nullable GpuTexture glowDepthTexture;
	private @Nullable GpuTextureView glowDepthTextureView;

	private GlowRenderer() {
		this.minecraft = Minecraft.getInstance();
	}

	public GpuTextureView getGlowDepthTexture() {
		return Objects.requireNonNull(this.glowDepthTextureView);
	}

	public void updateGlowDepthTexDepth() {
		tryUpdateDepthTexture();
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(this.minecraft.gameRenderer.mainRenderTarget().getDepthTexture(), this.glowDepthTexture, 0, 0, 0, 0, 0, this.glowDepthTexture.getWidth(0), this.glowDepthTexture.getHeight(0));
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

			this.glowDepthTexture = device.createTexture(() -> "Skyblocker Glow Depth Tex", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.D32_FLOAT, neededWidth, neededHeight, 1, 1);
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
}
