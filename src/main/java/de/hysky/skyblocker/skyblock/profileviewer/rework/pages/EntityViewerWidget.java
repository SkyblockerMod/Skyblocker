package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import com.mojang.authlib.GameProfile;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntityViewerWidget implements ProfileViewerWidget {

	public static final Identifier BACKGROUND = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/entity_widget.png");
	public static final int WIDTH = 82, HEIGHT = 110;

	sealed interface LoadedEntity {
		record Success(GameProfile profile, OtherClientPlayerEntity entity) implements LoadedEntity {
			public String name() {
				return profile.getName();
			}
		}

		record Loading() implements LoadedEntity {}

		record Failure() implements LoadedEntity {}
	}

	CompletableFuture<LoadedEntity> loadedEntity;
	TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

	public EntityViewerWidget(UUID uuid) {
		loadedEntity = SkullBlockEntity.fetchProfileByUuid(uuid)
				.<LoadedEntity>thenApply(profile -> {
					var profileUnwrapped = profile.get();
					var entity = new OtherClientPlayerEntity(MinecraftClient.getInstance().world, profileUnwrapped) {
						@Override
						public SkinTextures getSkinTextures() {
							PlayerListEntry playerListEntry = new PlayerListEntry(profileUnwrapped, false);
							return playerListEntry.getSkinTextures();
						}

						@Override
						public boolean isPartVisible(PlayerModelPart modelPart) {
							return !(modelPart.getName().equals(PlayerModelPart.CAPE.getName()));
						}

						@Override
						public boolean isInvisibleTo(PlayerEntity player) {
							return true;
						}
					};
					entity.setCustomNameVisible(false);
					return new LoadedEntity.Success(profileUnwrapped, entity);
				}).exceptionally(ex -> {
//					// "Player not found" doesn't fit on the screen lol
//					this.playerName = "User not found";
//					this.errorMessage = "Player skin not found";
//					this.profileNotFound = true;
					return new LoadedEntity.Failure();
				});
	}

	@Override
	public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, float deltaTicks) {
		drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
		switch (loadedEntity.getNow(new LoadedEntity.Loading())) {
			case LoadedEntity.Failure failure -> {
				// TODO: I should totally add an error message here
			}
			case LoadedEntity.Loading loading -> {
				// I should totally add a throbber here...
			}
			case LoadedEntity.Success success -> {
				var username = success.name();
				InventoryScreen.drawEntity(drawContext, x, y + 16, x + WIDTH, y + HEIGHT, 42, 0.0625F, mouseX, mouseY, success.entity);
				drawContext.drawCenteredTextWithShadow(textRenderer, username.length() > 15 ? username.substring(0, 15) : username, x + 40, y + 14, Color.WHITE.getRGB());
			}
		}
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}
}
