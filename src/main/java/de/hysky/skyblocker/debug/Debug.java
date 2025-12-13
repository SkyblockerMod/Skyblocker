package de.hysky.skyblocker.debug;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.InGameHudInvoker;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import net.azureaaron.networth.Calculation;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.TagValueOutput;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Debug {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("skyblocker.debug", "false"));
	//This is necessary to not spam the chat with 20 messages per second
	private static boolean keyDown = false;

	public static boolean debugEnabled() {
		return DEBUG_ENABLED || FabricLoader.getInstance().isDevelopmentEnvironment() || SnapshotDebug.isInSnapshot();
	}

	/**
	 * Used for checking if unit tests are being run.
	 */
	public static boolean isTestEnvironment() {
		return Boolean.getBoolean("IS_TEST_ENV");
	}

	public static boolean webSocketDebug() {
		return SkyblockerConfigManager.get().debug.webSocketDebug;
	}

	@Init
	public static void init() {
		if (!debugEnabled()) return;
		SnapshotDebug.init();
		KeyMapping dumpNearbyEntitiesKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.skyblocker.debug.dumpNearbyEntities", GLFW.GLFW_KEY_I, SkyblockerMod.KEYBINDING_CATEGORY));
		KeyMapping dumpHoveredItemKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.skyblocker.debug.dumpHoveredItem", GLFW.GLFW_KEY_U, SkyblockerMod.KEYBINDING_CATEGORY));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				literal(SkyblockerMod.NAMESPACE).then(literal("debug")
						.then(dumpPlayersCommand())
						.then(ItemUtils.dumpHeldItemCommand())
						.then(ItemUtils.dumpHeldItemNetworthCalculationsCommand())
						.then(toggleShowingInvisibleArmorStands())
						.then(dumpArmorStandHeadTextures())
						.then(toggleWebSocketDebug())
						.then(EventNotifications.debugToasts())
						.then(dumpBiome())
						.then(dumpActionBar())
						.then(auditMixins())
				)
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) return;
			if (dumpNearbyEntitiesKey.consumeClick() && !keyDown) {
				client.level.getEntities(client.player, client.player.getBoundingBox().inflate(SkyblockerConfigManager.get().debug.dumpRange))
						.stream()
						.map(entity -> {
							TagValueOutput writeView = TagValueOutput.createWithContext(new ProblemReporter.ScopedCollector(LOGGER), Utils.getRegistryWrapperLookup());
							entity.saveWithoutId(writeView);

							return writeView.buildResult();
						})
						.map(NbtUtils::toPrettyComponent)
						.forEach(text -> client.player.displayClientMessage(text, false));
				keyDown = true;
			} else if (!dumpNearbyEntitiesKey.consumeClick() && keyDown) {
				keyDown = false;
			}
		});
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof AbstractContainerScreen<?> handledScreen)) return;
			ScreenKeyboardEvents.afterKeyPress(screen).register((_screen, keyInput) -> {
				Slot focusedSlot = ((HandledScreenAccessor) handledScreen).getFocusedSlot();
				if (dumpHoveredItemKey.matches(keyInput) && client.player != null && focusedSlot != null && focusedSlot.hasItem()) {
					if (!keyInput.hasShiftDown()) {
						client.player.displayClientMessage(Constants.PREFIX.get().append("Hovered Item: ").append(SkyblockerConfigManager.get().debug.dumpFormat.format(focusedSlot.getItem())), false);
					} else {
						client.player.displayClientMessage(Constants.PREFIX.get().append("Held Item NW Calcs: ").append(Component.literal(SkyblockerMod.GSON_COMPACT.toJson(Calculation.LIST_CODEC.encodeStart(JsonOps.INSTANCE, NetworthCalculator.getItemNetworth(focusedSlot.getItem()).calculations()).getOrThrow()))), false);
					}
				}
			});
		});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpPlayersCommand() {
		return literal("dumpPlayers")
				.executes(context -> {
					context.getSource().getWorld().players().forEach(player -> context.getSource().sendFeedback(Component.nullToEmpty("'" + player.getName().getString() + "'")));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> toggleShowingInvisibleArmorStands() {
		return literal("toggleShowingInvisibleArmorStands")
				.executes(context -> {
					SkyblockerConfigManager.update(config -> config.debug.showInvisibleArmorStands = !config.debug.showInvisibleArmorStands);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.debug.toggledShowingInvisibleArmorStands", SkyblockerConfigManager.get().debug.showInvisibleArmorStands)));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> toggleWebSocketDebug() {
		return literal("toggleWebSocketDebug")
				.executes(context -> {
					SkyblockerConfigManager.update(config -> config.debug.webSocketDebug = !SkyblockerConfigManager.get().debug.webSocketDebug);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.debug.toggledWebSocketDebug", SkyblockerConfigManager.get().debug.webSocketDebug)));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpArmorStandHeadTextures() {
		return literal("dumpArmorStandHeadTextures")
				.executes(context -> {
					List<ArmorStand> armorStands = context.getSource().getWorld().getEntitiesOfClass(ArmorStand.class, context.getSource().getPlayer().getBoundingBox().inflate(8d), EntitySelector.ENTITY_NOT_BEING_RIDDEN);

					for (ArmorStand armorStand : armorStands) {
						Iterable<ItemStack> equippedItems = ItemUtils.getArmor(armorStand);

						for (ItemStack stack : equippedItems) {
							ItemUtils.getHeadTextureOptional(stack).ifPresent(texture -> context.getSource().sendFeedback(Component.nullToEmpty(texture)));
						}
					}

					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpBiome() {
		return literal("dumpBiome")
				.executes(context -> {
					FabricClientCommandSource source = context.getSource();
					Holder<Biome> biome = source.getWorld().getBiome(source.getPlayer().blockPosition());

					if (biome != null && biome.value() != null) {
						String biomeData = Biome.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, biome.value())
								.map(JsonElement::toString)
								.setPartial("")
								.getPartialOrThrow();
						source.sendFeedback(Constants.PREFIX.get().append(Component.literal(String.format("Biome ID: %s, Data: %s", biome.getRegisteredName(), biomeData))));
					}

					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpActionBar() {
		return literal("dumpActionBar")
				.executes(context -> {
					FabricClientCommandSource source = context.getSource();
					Component actionBar = ((InGameHudInvoker) (source.getClient().gui)).getOverlayMessageString();

					if (actionBar != null) {
						Component pretty = NbtUtils.toPrettyComponent(ComponentSerialization.CODEC.encodeStart(Utils.getRegistryWrapperLookup().createSerializationContext(NbtOps.INSTANCE), actionBar).getOrThrow());
						source.sendFeedback(Constants.PREFIX.get().append("Action Bar: ").append(pretty));
					}

					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> auditMixins() {
		return literal("auditMixins")
				.executes(context -> {
					MixinEnvironment.getCurrentEnvironment().audit();

					return Command.SINGLE_SUCCESS;
				});
	}

	public enum DumpFormat {
		JSON {
			@Override
			public Component format(ItemStack stack) {
				return Component.literal(SkyblockerMod.GSON_COMPACT.toJson(ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.encodeStart(Utils.getRegistryWrapperLookup().createSerializationContext(JsonOps.INSTANCE), stack).getOrThrow()));
			}
		},
		SNBT {
			@Override
			public Component format(ItemStack stack) {
				return NbtUtils.toPrettyComponent(ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.encodeStart(Minecraft.getInstance().player.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow());
			}
		};

		public abstract Component format(ItemStack stack);
	}
}
