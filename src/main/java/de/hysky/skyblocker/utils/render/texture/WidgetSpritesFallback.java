package de.hysky.skyblocker.utils.render.texture;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Function;

class WidgetSpritesFallback implements FallbackedTexture<WidgetSprites> {
	private final IdentifierTexture enabled;
	private final IdentifierTexture disabled;
	private final IdentifierTexture enabledFocused;
	private final IdentifierTexture disabledFocused;
	private WidgetSprites sprites;

	WidgetSpritesFallback(IdentifierTexture enabled, IdentifierTexture disabled, IdentifierTexture enabledFocused, IdentifierTexture disabledFocused) {
		this.enabled = enabled;
		this.disabled = disabled;
		this.enabledFocused = enabledFocused;
		this.disabledFocused = disabledFocused;
		sprites = new WidgetSprites(enabled.get(), disabled.get(), enabledFocused.get(), disabledFocused.get());
	}

	@Override
	public boolean isUsingFallback() {
		return enabled.isUsingFallback() || disabled.isUsingFallback() || enabledFocused.isUsingFallback() || disabledFocused.isUsingFallback();
	}

	@Override
	public WidgetSprites get() {
		return sprites;
	}

	void update() {
		enabled.update();
		disabled.update();
		enabledFocused.update();
		disabledFocused.update();
		sprites = new WidgetSprites(enabled.get(), disabled.get(), enabledFocused.get(), disabledFocused.get());
	}

	/**
	 * Helper class to avoid having duplicate IdentifierTextures.
	 */
	record Helper(WidgetSprites sprites, WidgetSprites fallback) {
		void populate(List<IdentifierTexture> textures, Function<WidgetSprites, Identifier> identifierFunction) {
			Identifier identifier = identifierFunction.apply(sprites);
			Identifier fallbackIdentifier = identifierFunction.apply(fallback);
			for (int i = 0; i < textures.size(); i++) {
				IdentifierTexture identifierTexture = textures.get(i);
				if (identifierTexture.texture == identifier && identifierTexture.fallback == fallbackIdentifier) {
					textures.add(i + 1, identifierTexture);
					return;
				}
			}
			textures.add(new IdentifierTexture(identifier, fallbackIdentifier, AtlasIds.GUI));
		}
	}

}
