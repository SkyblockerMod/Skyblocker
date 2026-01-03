package de.hysky.skyblocker.skyblock.item.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomArmorTrims {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomArmorTrims.class);
	public static final Object2ObjectOpenHashMap<ArmorTrimId, ArmorTrim> TRIMS_CACHE = new Object2ObjectOpenHashMap<>();
	private static boolean trimsInitialized = false;

	@Init
	public static void init() {
		SkyblockEvents.JOIN.register(CustomArmorTrims::initializeTrimCache);
		ClientCommandRegistrationCallback.EVENT.register(CustomArmorTrims::registerCommand);
	}

	private static void initializeTrimCache() {
		Minecraft client = Minecraft.getInstance();
		if (trimsInitialized || (client == null && !Debug.debugEnabled())) {
			return;
		}
		try {
			TRIMS_CACHE.clear();
			HolderLookup.Provider wrapperLookup = Utils.getRegistryWrapperLookup();
			for (Reference<TrimMaterial> material : wrapperLookup.lookupOrThrow(Registries.TRIM_MATERIAL).listElements().toList()) {
				for (Reference<TrimPattern> pattern : wrapperLookup.lookupOrThrow(Registries.TRIM_PATTERN).listElements().toList()) {
					ArmorTrim trim = new ArmorTrim(material, pattern);

					TRIMS_CACHE.put(new ArmorTrimId(material.key().location(), pattern.key().location()), trim);
				}
			}

			LOGGER.info("[Skyblocker] Successfully cached all armor trims!");
			trimsInitialized = true;
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Encountered an exception while caching armor trims", e);
		}
	}

	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("custom")
						.then(ClientCommandManager.literal("armorTrim")
								.executes(context -> customizeTrim(context.getSource(), null, null))
								.then(ClientCommandManager.argument("material", IdentifierArgument.id())
										.suggests(getIdSuggestionProvider(Registries.TRIM_MATERIAL))
										.executes(context -> customizeTrim(context.getSource(), context.getArgument("material", Identifier.class), null))
										.then(ClientCommandManager.argument("pattern", IdentifierArgument.id())
												.suggests(getIdSuggestionProvider(Registries.TRIM_PATTERN))
												.executes(context -> customizeTrim(context.getSource(), context.getArgument("material", Identifier.class), context.getArgument("pattern", Identifier.class))))))));
	}

	private static SuggestionProvider<FabricClientCommandSource> getIdSuggestionProvider(ResourceKey<? extends Registry<?>> registryKey) {
		return (context, builder) -> context.getSource().suggestRegistryElements(registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, builder, context);
	}

	@SuppressWarnings("SameReturnValue")
	private static int customizeTrim(FabricClientCommandSource source, Identifier material, Identifier pattern) {
		ItemStack heldItem = source.getPlayer().getMainHandItem();

		if (Utils.isOnSkyblock() && heldItem != null) {
			if (heldItem.is(ItemTags.TRIMMABLE_ARMOR)) {
				String itemUuid = heldItem.getUuid();

				if (!itemUuid.isEmpty()) {
					Object2ObjectOpenHashMap<String, ArmorTrimId> customArmorTrims = SkyblockerConfigManager.get().general.customArmorTrims;

					if (material == null && pattern == null) {
						if (customArmorTrims.containsKey(itemUuid)) {
							SkyblockerConfigManager.update(config -> config.general.customArmorTrims.remove(itemUuid));
							source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.customArmorTrims.removed")));
						} else {
							source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.customArmorTrims.neverHad")));
						}
					} else {
						// Ensure that the material & trim are valid
						ArmorTrimId trimId = new ArmorTrimId(material, pattern);
						if (TRIMS_CACHE.get(trimId) == null) {
							source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customArmorTrims.invalidMaterialOrPattern")));

							return Command.SINGLE_SUCCESS;
						}

						SkyblockerConfigManager.update(config -> config.general.customArmorTrims.put(itemUuid, trimId));
						source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.customArmorTrims.added")));
					}
				} else {
					source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customArmorTrims.noItemUuid")));
				}
			} else {
				source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customArmorTrims.notAnArmorPiece")));
				return Command.SINGLE_SUCCESS;
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customArmorTrims.unableToSetTrim")));
		}

		return Command.SINGLE_SUCCESS;
	}

	public record ArmorTrimId(Identifier material, Identifier pattern) implements Pair<Identifier, Identifier> {
		public static final Codec<ArmorTrimId> CODEC = RecordCodecBuilder.create(instance -> instance.group(
						Identifier.CODEC.fieldOf("material").forGetter(ArmorTrimId::material),
						Identifier.CODEC.fieldOf("pattern").forGetter(ArmorTrimId::pattern))
				.apply(instance, ArmorTrimId::new));

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
