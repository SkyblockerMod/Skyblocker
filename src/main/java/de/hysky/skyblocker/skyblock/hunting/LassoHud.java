package de.hysky.skyblocker.skyblock.hunting;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

@RegisterWidget
public class LassoHud extends ComponentBasedWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final String LASSO_COUNT_DOWN_NAME = "                    ";
	private static final Set<Location> AVAILABLE_LOCATION = Set.of(Location.GALATEA);

	private static LassoHud instance;
	private static int percentage = 0;
	/**
	 * When the reel message appears for the held lasso
	 */
	private static int reelValue = 0;
	private static Entity lassoEntity = null;

	public static LassoHud getInstance() {
		return instance;
	}

	public LassoHud() {
		super(Component.literal("Lasso").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), ChatFormatting.DARK_AQUA.getColor(), "Lasso HUD");
		instance = this;
	}

	public static void onEntityUpdate(ArmorStand entity) {
		//check to see if close to end of players lasso
		if (!getInstance().isEnabledIn(Utils.getLocation()) || lassoEntity == null || entity.distanceToSqr(lassoEntity) > 16) return;

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
		if (!getInstance().isEnabledIn(Utils.getLocation()) || CLIENT.level == null) return;
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
			addComponent(Components.progressComponent(Items.LEAD.getDefaultInstance(), Component.translatable("skyblocker.config.hunting.lassoHud.reel"), Component.translatable("skyblocker.config.hunting.lassoHud.now").withStyle(ChatFormatting.GREEN), percentage));
			return;
		}
		addComponent(Components.progressComponent(Items.LEAD.getDefaultInstance(), Component.translatable("skyblocker.config.hunting.lassoHud.reel"), Component.translatable("skyblocker.config.hunting.lassoHud.wait"), percentage));

	}

	@Override
	public boolean shouldRender(Location location) {
		//forget entity if it has died
		if (lassoEntity != null && !lassoEntity.isAlive()) {
			lassoEntity = null;
		}

		return percentage != -1 && lassoEntity != null && super.shouldRender(location);
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATION;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!AVAILABLE_LOCATION.contains(location)) return;
		SkyblockerConfigManager.get().hunting.lassoHud.enabled = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		if (!AVAILABLE_LOCATION.contains(location)) return false;
		return SkyblockerConfigManager.get().hunting.lassoHud.enabled;
	}
}
