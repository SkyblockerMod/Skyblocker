package me.xmrvizzy.skyblocker.skyblock.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.SkyblockEvents;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CustomArmorTrims {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomArmorTrims.class);
	public static final Object2ObjectOpenHashMap<ArmorTrimId, Optional<ArmorTrim>> TRIMS_CACHE = new Object2ObjectOpenHashMap<>();
	private static boolean trimsInitialized = false;

	public static void init() {
		SkyblockEvents.JOIN.register(CustomArmorTrims::initializeTrimCache);
		ClientCommandRegistrationCallback.EVENT.register(CustomArmorTrims::registerCommand);
	}

	private static void initializeTrimCache() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (trimsInitialized || player == null) {
			return;
		}
		try {
			TRIMS_CACHE.clear();
			DynamicRegistryManager registryManager = player.networkHandler.getRegistryManager();
			for (Identifier material : registryManager.get(RegistryKeys.TRIM_MATERIAL).getIds()) {
				for (Identifier pattern : registryManager.get(RegistryKeys.TRIM_PATTERN).getIds()) {
					NbtCompound compound = new NbtCompound();
					compound.putString("material", material.toString());
					compound.putString("pattern", pattern.toString());

					ArmorTrim trim = ArmorTrim.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE, registryManager), compound).resultOrPartial(LOGGER::error).orElse(null);

					// Something went terribly wrong
					if (trim == null) throw new IllegalStateException("Trim shouldn't be null! [" + "\"" + material + "\",\"" + pattern + "\"]");

					TRIMS_CACHE.put(new ArmorTrimId(material, pattern), Optional.of(trim));
				}
			}

			LOGGER.info("[Skyblocker] Successfully cached all armor trims!");
			trimsInitialized = true;
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Encountered an exception while caching armor trims", e);
		}
	}

	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("custom")
						.then(ClientCommandManager.literal("armorTrim")
								.executes(context -> customizeTrim(context.getSource(), null, null))
								.then(ClientCommandManager.argument("material", IdentifierArgumentType.identifier())
										.suggests(getIdSuggestionProvider(RegistryKeys.TRIM_MATERIAL))
										.executes(context -> customizeTrim(context.getSource(), context.getArgument("material", Identifier.class), null))
										.then(ClientCommandManager.argument("pattern", IdentifierArgumentType.identifier())
												.suggests(getIdSuggestionProvider(RegistryKeys.TRIM_PATTERN))
												.executes(context -> customizeTrim(context.getSource(), context.getArgument("material", Identifier.class), context.getArgument("pattern", Identifier.class))))))));
	}

	@NotNull
	private static SuggestionProvider<FabricClientCommandSource> getIdSuggestionProvider(RegistryKey<? extends Registry<?>> registryKey) {
		return (context, builder) -> context.getSource().listIdSuggestions(registryKey, CommandSource.SuggestedIdType.ELEMENTS, builder, context);
	}

	@SuppressWarnings("SameReturnValue")
	private static int customizeTrim(FabricClientCommandSource source, Identifier material, Identifier pattern) {
		ItemStack heldItem = source.getPlayer().getMainHandStack();
		NbtCompound nbt = (heldItem != null) ? heldItem.getNbt() : null;

		if (Utils.isOnSkyblock() && heldItem != null) {
			if (heldItem.getItem() instanceof ArmorItem) {
				if (nbt != null && nbt.contains("ExtraAttributes")) {
					NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
					String itemUuid = extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;

					if (itemUuid != null) {
						Object2ObjectOpenHashMap<String, ArmorTrimId> customArmorTrims = SkyblockerConfig.get().general.customArmorTrims;

						if (material == null && pattern == null) {
							if (customArmorTrims.containsKey(itemUuid)) {
								customArmorTrims.remove(itemUuid);
								SkyblockerConfig.save();
								source.sendFeedback(Text.translatable("skyblocker.customArmorTrims.removed"));
							} else {
								source.sendFeedback(Text.translatable("skyblocker.customArmorTrims.neverHad"));
							}
						} else {
							// Ensure that the material & trim are valid
							ArmorTrimId trimId = new ArmorTrimId(material, pattern);
							if (TRIMS_CACHE.get(trimId) == null) {
								source.sendError(Text.translatable("skyblocker.customArmorTrims.invalidMaterialOrPattern"));

								return Command.SINGLE_SUCCESS;
							}

							customArmorTrims.put(itemUuid, trimId);
							SkyblockerConfig.save();
							source.sendFeedback(Text.translatable("skyblocker.customArmorTrims.added"));
						}
					} else {
						source.sendError(Text.translatable("skyblocker.customArmorTrims.noItemUuid"));
					}
				}
			} else {
				source.sendError(Text.translatable("skyblocker.customArmorTrims.notAnArmorPiece"));
				return Command.SINGLE_SUCCESS;
			}
		} else {
			source.sendError(Text.translatable("skyblocker.customArmorTrims.unableToSetTrim"));
		}

		return Command.SINGLE_SUCCESS;
	}

	public record ArmorTrimId(Identifier material, Identifier pattern) implements Pair<Identifier, Identifier> {
		@Override
		public Identifier left() {
			return material();
		}

		@Override
		public Identifier right() {
			return pattern();
		}
	}
}
