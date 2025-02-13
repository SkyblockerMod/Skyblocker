package de.hysky.skyblocker.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import net.azureaaron.networth.Calculation;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Debug {
	private static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("skyblocker.debug", "false"));
	//This is necessary to not spam the chat with 20 messages per second
	private static boolean keyDown = false;

	public static boolean debugEnabled() {
		return DEBUG_ENABLED || FabricLoader.getInstance().isDevelopmentEnvironment() || SnapshotDebug.isInSnapshot();
	}

	public static boolean webSocketDebug() {
		return SkyblockerConfigManager.get().debug.webSocketDebug;
	}

	@Init
	public static void init() {
		if (!debugEnabled()) return;
		SnapshotDebug.init();
		KeyBinding dumpNearbyEntitiesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.skyblocker.debug.dumpNearbyEntities", GLFW.GLFW_KEY_I, "key.categories.skyblocker"));
		KeyBinding dumpHoveredItemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.skyblocker.debug.dumpHoveredItem", GLFW.GLFW_KEY_U, "key.categories.skyblocker"));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				literal(SkyblockerMod.NAMESPACE).then(literal("debug")
						.then(dumpPlayersCommand())
						.then(ItemUtils.dumpHeldItemCommand())
						.then(ItemUtils.dumpHeldItemNetworthCalculationsCommand())
						.then(toggleShowingInvisibleArmorStands())
						.then(dumpArmorStandHeadTextures())
						.then(toggleWebSocketDebug())
						.then(EventNotifications.debugToasts())
				)
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) return;
			if (dumpNearbyEntitiesKey.wasPressed() && !keyDown) {
				client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(SkyblockerConfigManager.get().debug.dumpRange))
						.stream()
						.map(entity -> entity.writeNbt(new NbtCompound()))
						.map(NbtHelper::toPrettyPrintedText)
						.forEach(text -> client.player.sendMessage(text, false));
				keyDown = true;
			} else if (!dumpNearbyEntitiesKey.wasPressed() && keyDown) {
				keyDown = false;
			}
		});
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof HandledScreen<?> handledScreen)) return;
			ScreenKeyboardEvents.afterKeyPress(screen).register((_screen, key, scancode, modifier) -> {
				Slot focusedSlot = ((HandledScreenAccessor) handledScreen).getFocusedSlot();
				if (dumpHoveredItemKey.matchesKey(key, scancode) && client.player != null && focusedSlot != null && focusedSlot.hasStack()) {
					if (!Screen.hasShiftDown()) {
						client.player.sendMessage(Constants.PREFIX.get().append("Hovered Item: ").append(SkyblockerConfigManager.get().debug.dumpFormat.format(focusedSlot.getStack())), false);
					} else {
						client.player.sendMessage(Constants.PREFIX.get().append("Held Item NW Calcs: ").append(Text.literal(SkyblockerMod.GSON_COMPACT.toJson(Calculation.LIST_CODEC.encodeStart(JsonOps.INSTANCE, NetworthCalculator.getItemNetworth(focusedSlot.getStack()).calculations()).getOrThrow()))), false);
					}
				}
			});
		});
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
					SkyblockerConfigManager.get().debug.showInvisibleArmorStands = !SkyblockerConfigManager.get().debug.showInvisibleArmorStands;
					SkyblockerConfigManager.save();
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.debug.toggledShowingInvisibleArmorStands", SkyblockerConfigManager.get().debug.showInvisibleArmorStands)));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> toggleWebSocketDebug() {
		return literal("toggleWebSocketDebug")
				.executes(context -> {
					SkyblockerConfigManager.get().debug.webSocketDebug = !SkyblockerConfigManager.get().debug.webSocketDebug;
					SkyblockerConfigManager.save();
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.debug.toggledWebSocketDebug", SkyblockerConfigManager.get().debug.webSocketDebug)));
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

	public enum DumpFormat {
		JSON {
			@Override
			public Text format(ItemStack stack) {
				return Text.literal(SkyblockerMod.GSON_COMPACT.toJson(ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.encodeStart(ItemStackComponentizationFixer.getRegistryLookup().getOps(JsonOps.INSTANCE), stack).getOrThrow()));
			}
		},
		SNBT {
			@Override
			public Text format(ItemStack stack) {
				return NbtHelper.toPrettyPrintedText(ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.encodeStart(MinecraftClient.getInstance().player.getRegistryManager().getOps(NbtOps.INSTANCE), stack).getOrThrow());
			}
		};

		public abstract Text format(ItemStack stack);
	}
}
