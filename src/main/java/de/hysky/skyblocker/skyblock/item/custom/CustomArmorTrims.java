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
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
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
		MinecraftClient client = MinecraftClient.getInstance();
		if (trimsInitialized || (client == null && !Debug.debugEnabled())) {
			return;
		}
		try {
			TRIMS_CACHE.clear();
			RegistryWrapper.WrapperLookup wrapperLookup = Utils.getRegistryWrapperLookup();
			for (Reference<ArmorTrimMaterial> material : wrapperLookup.getOrThrow(RegistryKeys.TRIM_MATERIAL).streamEntries().toList()) {
				for (Reference<ArmorTrimPattern> pattern : wrapperLookup.getOrThrow(RegistryKeys.TRIM_PATTERN).streamEntries().toList()) {
					ArmorTrim trim = new ArmorTrim(material, pattern);

					TRIMS_CACHE.put(new ArmorTrimId(material.registryKey().getValue(), pattern.registryKey().getValue()), trim);
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

		if (Utils.isOnSkyblock() && heldItem != null) {
			if (heldItem.isIn(ItemTags.TRIMMABLE_ARMOR)) {
				String itemUuid = heldItem.getUuid();

				if (!itemUuid.isEmpty()) {
					Object2ObjectOpenHashMap<String, ArmorTrimId> customArmorTrims = SkyblockerConfigManager.get().general.customArmorTrims;

					if (material == null && pattern == null) {
						if (customArmorTrims.containsKey(itemUuid)) {
							SkyblockerConfigManager.update(config -> config.general.customArmorTrims.remove(itemUuid));
							source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.removed")));
						} else {
							source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.neverHad")));
						}
					} else {
						// Ensure that the material & trim are valid
						ArmorTrimId trimId = new ArmorTrimId(material, pattern);
						if (TRIMS_CACHE.get(trimId) == null) {
							source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.invalidMaterialOrPattern")));

							return Command.SINGLE_SUCCESS;
						}

						SkyblockerConfigManager.update(config -> config.general.customArmorTrims.put(itemUuid, trimId));
						source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.added")));
					}
				} else {
					source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.noItemUuid")));
				}
			} else {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.notAnArmorPiece")));
				return Command.SINGLE_SUCCESS;
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customArmorTrims.unableToSetTrim")));
		}

		return Command.SINGLE_SUCCESS;
	}

	public record ArmorTrimId(@SerialEntry Identifier material, @SerialEntry Identifier pattern) implements Pair<Identifier, Identifier> {
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
