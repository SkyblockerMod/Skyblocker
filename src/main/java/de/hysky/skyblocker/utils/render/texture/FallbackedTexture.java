package de.hysky.skyblocker.utils.render.texture;

import com.mojang.datafixers.util.Unit;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A texture reference that will default back to another one if it doesn't exist.
 */
public interface FallbackedTexture<T> extends Supplier<T> {
	List<Runnable> UPDATE_CALLBACKS = new ArrayList<>();

	boolean isUsingFallback();

	/**
	 * @param id the texture that may or may not be specified by a resource pack
	 * @param fallback the fallback if {@code id} doesn't exist
	 * @param atlas the to look in, pass null if the texture isn't in any atlas.
	 * @return a fallbacked texture supplier
	 */
	static FallbackedTexture<Identifier> of(Identifier id, Identifier fallback, @Nullable Identifier atlas) {
		IdentifierTexture identifierTexture = new IdentifierTexture(id, fallback, atlas);
		UPDATE_CALLBACKS.add(identifierTexture::update);
		return identifierTexture;
	}

	/**
	 * For textures in the {@code gui/sprites} folder.
	 */
	static FallbackedTexture<Identifier> ofGuiSprite(Identifier id, Identifier fallback) {
		return of(id, fallback, AtlasIds.GUI);
	}

	/**
	 * For textures that aren't in any atlas (usually backgrounds)
	 */
	static FallbackedTexture<Identifier> ofTexture(Identifier id, Identifier fallback) {
		return of(id, fallback, null);
	}

	/**
	 * WidgetSprites, used for buttons.
	 */
	static FallbackedTexture<WidgetSprites> ofWidgetSprites(WidgetSprites sprites, WidgetSprites fallback) {
		List<IdentifierTexture> textures = new ArrayList<>(4);
		WidgetSpritesFallback.Helper helper = new WidgetSpritesFallback.Helper(sprites, fallback);
		helper.populate(textures, WidgetSprites::enabled);
		helper.populate(textures, WidgetSprites::disabled);
		helper.populate(textures, WidgetSprites::enabledFocused);
		helper.populate(textures, WidgetSprites::disabledFocused);
		WidgetSpritesFallback spritesFallback = new WidgetSpritesFallback(textures.get(0), textures.get(1), textures.get(2), textures.get(3));
		UPDATE_CALLBACKS.add(spritesFallback::update);
		return spritesFallback;
	}


	@Init
	static void init() {
		Identifier id = SkyblockerMod.id("fallback_texture_reloader");
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(id,
				(sharedState, executor, preparationBarrier, executor2) -> CompletableFuture
						.supplyAsync(() -> Unit.INSTANCE, executor)
						.thenCompose(preparationBarrier::wait)
						.thenAcceptAsync(unit -> UPDATE_CALLBACKS.forEach(Runnable::run)));
		ResourceLoader.get(PackType.CLIENT_RESOURCES).addReloaderOrdering(ResourceReloaderKeys.AFTER_VANILLA, id);
	}
}
