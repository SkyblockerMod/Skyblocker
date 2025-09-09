package de.hysky.skyblocker.skyblock.hunting;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;

@RegisterWidget
public class LassoHud extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final String LASSO_COUNT_DOWN_NAME = "                    ";

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
		super(Text.literal("Lasso").formatted(Formatting.DARK_AQUA, Formatting.BOLD), Formatting.DARK_AQUA.getColorValue(), "Lasso HUD");
		instance = this;
	}

	public static void onEntityUpdate(ArmorStandEntity entity) {
		//check to see if close to end of players lasso
		if (lassoEntity == null || entity.squaredDistanceTo(lassoEntity) > 10) return;

		//see if it's the name we are looking for
		Text name = entity.getCustomName();
		if (name != null) {
			//percentage bar amor stand when it's not 0
			if (name.getString().equals(LASSO_COUNT_DOWN_NAME) && name.getSiblings().size() == 2) {
				int newPercentage = (int) (((name.getSiblings().getFirst().getString().length() - reelValue) / (20f - reelValue)) * 100);
				percentage = Math.max(newPercentage, 0);
			}
		}
	}

	public static void onEntityAttach(EntityAttachS2CPacket packet) {
		if (CLIENT.world == null) return;
		//see if lasso is coming from this player
		if (CLIENT.world.getEntityById(packet.getHoldingEntityId()) instanceof PlayerEntity player) {
			if (player.equals(CLIENT.player)) {
				//save lasso end entity
				lassoEntity = CLIENT.world.getEntityById(packet.getAttachedEntityId());

				//get rarity of lasso used
				String usedItemId = ItemUtils.getItemId(player.getMainHandStack());

				reelValue = switch (usedItemId) {
					case "ABYSMAL_LASSO" -> 2;
					case "VINERIP_LASSO", "ENTANGLER_LASSO" -> 3;
					case "EVERSTRETCH_LASSO" -> 4;
					default -> throw new IllegalStateException("Unexpected value: " + usedItemId);
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
			addComponent(Components.progressComponent(Items.LEAD.getDefaultStack(), Text.translatable("skyblocker.config.hunting.lassoHud.reel"), Text.translatable("skyblocker.config.hunting.lassoHud.now").formatted(Formatting.GREEN), percentage));
			return;
		}
		addComponent(Components.progressComponent(Items.LEAD.getDefaultStack(), Text.translatable("skyblocker.config.hunting.lassoHud.reel"), Text.translatable("skyblocker.config.hunting.lassoHud.wait"), percentage));

	}

	@Override
	public boolean shouldRender(Location location) {

		//forget entity if it has died
		if (lassoEntity != null && !lassoEntity.isAlive()) {
			System.out.println(lassoEntity);
			lassoEntity = null;
		}

		return percentage != -1 && lassoEntity != null && super.shouldRender(location);
	}

	@Override
	public Set<Location> availableLocations() {
		return Set.of(Location.GALATEA);
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		SkyblockerConfigManager.get().hunting.lassoHud.enabled = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return SkyblockerConfigManager.get().hunting.lassoHud.enabled;
	}
}
