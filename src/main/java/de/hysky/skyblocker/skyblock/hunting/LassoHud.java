package de.hysky.skyblocker.skyblock.hunting;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@RegisterWidget
public class LassoHud extends ElementBasedWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final String LASSO_COUNT_DOWN_NAME = "                    ";

	private static @Nullable LassoHud instance;
	private static int percentage = 0;
	/**
	 * When the reel message appears for the held lasso
	 */
	private static int reelValue = 0;
	private static @Nullable Entity lassoEntity = null;

	public static LassoHud getInstance() {
		return Objects.requireNonNull(instance, "LassoHud not initialized");
	}

	public LassoHud() {
		super(Component.literal("Lasso").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), ChatFormatting.DARK_AQUA.getColor(), new Information("hud_lasso", Component.literal("Lasso HUD"), Location.GALATEA));
		instance = this;
	}

	public static void onEntityUpdate(ArmorStand entity) {
		//check to see if close to end of players lasso
		if (!WidgetManager.isWidgetInCurrentLayer(getInstance()) || lassoEntity == null || entity.distanceToSqr(lassoEntity) > 16) return;

		//see if it's the name we are looking for
		Component name = entity.getCustomName();
		if (name != null) {
			//percentage bar armor stand when it's not 0
			if (name.getString().equals(LASSO_COUNT_DOWN_NAME) && name.getSiblings().size() == 2) {
				int newPercentage = (int) (((name.getSiblings().getFirst().getString().length() - reelValue) / (20f - reelValue)) * 100);
				percentage = Math.max(newPercentage, 0);
			}
		}
	}

	public static void onEntityAttach(ClientboundSetEntityLinkPacket packet) {
		if (!WidgetManager.isWidgetInCurrentLayer(getInstance()) || CLIENT.level == null) return;
		//see if lasso is coming from this player
		if (CLIENT.level.getEntity(packet.getDestId()) instanceof Player player) {
			if (player.equals(CLIENT.player)) {
				//save lasso end entity
				lassoEntity = CLIENT.level.getEntity(packet.getSourceId());

				//get rarity of lasso used
				String usedItemId = player.getMainHandItem().getSkyblockId();

				reelValue = switch (usedItemId) {
					case "ABYSMAL_LASSO" -> 2;
					case "VINERIP_LASSO", "ENTANGLER_LASSO" -> 3;
					case "EVERSTRETCH_LASSO" -> 4;
					//Moody Grappleshot is a thing and we don't want to throw (crash) when it is used.
					default -> 0;
				};

				//reset percentage
				percentage = -1;
			}
		}
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	public void updateContent() {
		//if 0 percent now otherwise wait
		if (percentage == 0) {
			addComponent(Elements.progressComponent(Ico.LEAD, Component.translatable("skyblocker.config.hunting.lassoHud.reel"), Component.translatable("skyblocker.config.hunting.lassoHud.now").withStyle(ChatFormatting.GREEN), percentage));
			return;
		}
		addComponent(Elements.progressComponent(Ico.LEAD, Component.translatable("skyblocker.config.hunting.lassoHud.reel"), Component.translatable("skyblocker.config.hunting.lassoHud.wait"), percentage));

	}

	@Override
	public boolean shouldRender() {
		//forget entity if it has died
		if (lassoEntity != null && !lassoEntity.isAlive()) {
			lassoEntity = null;
		}

		return percentage != -1 && lassoEntity != null;
	}
}
