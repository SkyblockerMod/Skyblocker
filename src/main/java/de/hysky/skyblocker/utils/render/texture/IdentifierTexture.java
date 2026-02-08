package de.hysky.skyblocker.utils.render.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

class IdentifierTexture implements FallbackedTexture<Identifier> {

	final Identifier texture;
	final Identifier fallback;
	private final @Nullable Identifier atlas;
	private Identifier currentTexture;

	IdentifierTexture(Identifier texture, Identifier fallback, @Nullable Identifier atlas) {
		this.texture = texture;
		this.fallback = fallback;
		this.atlas = atlas;
		this.currentTexture = texture;
		if (Minecraft.getInstance().isGameLoadFinished()) update();
	}

	@Override
	public boolean isUsingFallback() {
		return currentTexture == fallback;
	}

	@Override
	public Identifier get() {
		return currentTexture;
	}

	void update() {
		Minecraft minecraft = Minecraft.getInstance();
		if (atlas != null) {
			TextureAtlas textureAtlas = minecraft.getAtlasManager().getAtlasOrThrow(atlas);
			if (textureAtlas.missingSprite().equals(textureAtlas.getSprite(texture))) currentTexture = fallback;
			else currentTexture = texture;
		} else {
			Optional<Resource> resource = minecraft.getResourceManager().getResource(texture);
			if (resource.isEmpty())  currentTexture = fallback;
			else currentTexture = texture;
		}
	}
}
