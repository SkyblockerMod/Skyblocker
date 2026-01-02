package de.hysky.skyblocker.utils.render;

import java.util.Arrays;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldTerrainRenderContext;

/**
 * Creates a pool of {@code GpuTexture}s and {@code GpuTextureView}s, useful if you are blitting textures or copying them.
 * Textures are automatically closed if they aren't used for a frame.
 */
public record TexturePool(String name, int size, @GpuTexture.Usage int usage, TextureFormat format, @Nullable GpuTexture[] textures, @Nullable GpuTextureView[] textureViews, boolean[] usedSlots) implements AutoCloseable {

	public TexturePool {
		WorldRenderEvents.START_MAIN.register(this::clearUnusedTextures);
	}

	/**
	 * Creates a new texture pool, the size is recommended to be double the amount of slots that you
	 * expect you will need in a single frame case the texture sizes do not match.
	 */
	public static TexturePool create(String name, int size, @GpuTexture.Usage int usage, TextureFormat format) {
		return new TexturePool(name, size, usage, format, new GpuTexture[size], new GpuTextureView[size], new boolean[size]);
	}

	private void clearUnusedTextures(WorldTerrainRenderContext context) {
		// Close textures if they were unused for a frame
		for (int i = 0; i < this.size(); i++) {
			if (!this.usedSlots()[i]) {
				GpuTexture texture = this.textures()[i];
				GpuTextureView textureView = this.textureViews()[i];

				if (texture != null && textureView != null) {
					texture.close();
					textureView.close();

					this.textures()[i] = null;
					this.textureViews()[i] = null;
				}
			}
		}

		Arrays.fill(this.usedSlots(), false);
	}

	public int getNextAvailableIndex(int requiredWidth, int requiredHeight) {
		for (int i = 0; i < this.size(); i++) {
			if (!this.usedSlots()[i]) {
				GpuTexture texture = this.textures()[i];
				GpuTextureView textureView = this.textureViews()[i];
				boolean textureExists = texture != null && textureView != null;
				boolean textureSizesMatch = textureExists && texture.getWidth(0) == requiredWidth && texture.getHeight(0) == requiredHeight;

				if (textureSizesMatch) {
					this.usedSlots()[i] = true;
					return i;
				} else if (!textureExists) {
					GpuTexture newTexture = RenderSystem.getDevice().createTexture(this.name() + " " + i, this.usage(), this.format(), requiredWidth, requiredHeight, 1, 1);
					GpuTextureView newTextureView = RenderSystem.getDevice().createTextureView(newTexture);

					this.textures()[i] = newTexture;
					this.textureViews()[i] = newTextureView;
					this.usedSlots()[i] = true;

					return i;
				}
			}
		}

		throw new UnsupportedOperationException("Trying to use too many textures than are allocated for the pool");
	}

	/**
	 * Retrieves the {@code GpuTexture} at the {@code index}. You must use {@link #nextAvailableIndex(int, int)} first.
	 */
	public GpuTexture getTexture(int index) {
		return Objects.requireNonNull(this.textures()[index]);
	}

	/**
	 * Retrieves the {@code GpuTextureView} at the {@code index}. You must use {@link #nextAvailableIndex(int, int)} first.
	 */
	public GpuTextureView getTextureView(int index) {
		return Objects.requireNonNull(this.textureViews()[index]);
	}

	/**
	 * Closes all textures.
	 */
	@Override
	public void close() {
		for (GpuTexture texture : this.textures()) {
			if (texture != null) {
				texture.close();
			}
		}

		for (GpuTextureView textureView : this.textureViews()) {
			if (textureView != null) {
				textureView.close();
			}
		}

		Arrays.fill(this.usedSlots(), false);
	}
}
