package de.hysky.skyblocker.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.List;

public class Debug {
	private static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("skyblocker.debug", "false"));

	private static boolean showInvisibleArmorStands = false;

	public static boolean debugEnabled() {
		return DEBUG_ENABLED || FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static void init() {
		if (debugEnabled()) {
			SnapshotDebug.init();
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("debug")
					.then(dumpPlayersCommand())
					.then(ItemUtils.dumpHeldItemCommand())
					.then(toggleShowingInvisibleArmorStands())
					.then(dumpArmorStandHeadTextures())
			)));
		}
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpPlayersCommand() {
		return literal("dumpPlayers")
				.executes(context -> {
					context.getSource().getWorld().getPlayers().forEach(player -> context.getSource().sendFeedback(Text.of("'" + player.getName().getString() + "'")));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> toggleShowingInvisibleArmorStands() {
		return literal("toggleShowingInvisibleArmorStands")
				.executes(context -> {
					showInvisibleArmorStands = !showInvisibleArmorStands;
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.debug.toggledShowingInvisibleArmorStands", showInvisibleArmorStands)));
					return Command.SINGLE_SUCCESS;
				});
	}
	
	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpArmorStandHeadTextures() {
		return literal("dumpArmorStandHeadTextures")
				.executes(context -> {
					List<ArmorStandEntity> armorStands = context.getSource().getWorld().getEntitiesByClass(ArmorStandEntity.class, context.getSource().getPlayer().getBoundingBox().expand(8d), EntityPredicates.NOT_MOUNTED);

					for (ArmorStandEntity armorStand : armorStands) {
						Iterable<ItemStack> equippedItems = armorStand.getEquippedItems();

						for (ItemStack stack : equippedItems) {
							String texture = ItemUtils.getHeadTexture(stack);

							if (!texture.isEmpty()) context.getSource().sendFeedback(Text.of(texture));
						}
					}

					return Command.SINGLE_SUCCESS;
				});
	}

	public static boolean shouldShowInvisibleArmorStands() {
		return showInvisibleArmorStands;
	}
}
