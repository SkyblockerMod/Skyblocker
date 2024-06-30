package de.hysky.skyblocker.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import net.azureaaron.networth.Calculation;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Debug {
	private static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("skyblocker.debug", "false"));

	private static boolean showInvisibleArmorStands = false;
	private static boolean webSocketDebug = false;
	private static boolean stpGlobal = false;

	public static boolean debugEnabled() {
		return DEBUG_ENABLED || FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static boolean shouldShowInvisibleArmorStands() {
		return showInvisibleArmorStands;
	}

	public static boolean webSocketDebug() {
		return webSocketDebug;
	}

	public static boolean stpGlobal() {
		return stpGlobal;
	}

	@Init
	public static void init() {
		if (debugEnabled()) {
			SnapshotDebug.init();
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("debug")
					.then(dumpPlayersCommand())
					.then(ItemUtils.dumpHeldItemCommand())
					.then(ItemUtils.dumpHeldItemNetworthCalculationsCommand())
					.then(toggleShowingInvisibleArmorStands())
					.then(dumpArmorStandHeadTextures())
					.then(toggleWebSocketDebug())
					.then(toggleSTPGlobal())
			)));
			ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
				if (screen instanceof HandledScreen<?> handledScreen) {
					ScreenKeyboardEvents.afterKeyPress(screen).register((_screen, key, scancode, modifier) -> {
						Slot focusedSlot = ((HandledScreenAccessor) handledScreen).getFocusedSlot();
						if (key == GLFW.GLFW_KEY_U && client.player != null && focusedSlot != null && focusedSlot.hasStack()) {
							if (!Screen.hasShiftDown()) {
								client.player.sendMessage(Text.literal("[Skyblocker Debug] Hovered Item: " + SkyblockerMod.GSON_COMPACT.toJson(ItemStack.CODEC.encodeStart(ItemStackComponentizationFixer.getRegistryLookup().getOps(JsonOps.INSTANCE), focusedSlot.getStack()).getOrThrow())));
							} else {
								client.player.sendMessage(Text.literal("[Skyblocker Debug] Held Item NW Calcs: " + SkyblockerMod.GSON_COMPACT.toJson(Calculation.LIST_CODEC.encodeStart(JsonOps.INSTANCE, NetworthCalculator.getItemNetworth(focusedSlot.getStack()).calculations()).getOrThrow())));
							}
						}
					});
				}
			});
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

	private static LiteralArgumentBuilder<FabricClientCommandSource> toggleWebSocketDebug() {
		return literal("toggleWebSocketDebug")
				.executes(context -> {
					webSocketDebug = !webSocketDebug;
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.debug.toggledWebSocketDebug", webSocketDebug)));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> toggleSTPGlobal() {
		return literal("toggleSTPGlobal")
				.executes(context -> {
					stpGlobal = !stpGlobal;
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
							ItemUtils.getHeadTextureOptional(stack).ifPresent(texture -> context.getSource().sendFeedback(Text.of(texture)));
						}
					}

					return Command.SINGLE_SUCCESS;
				});
	}
}