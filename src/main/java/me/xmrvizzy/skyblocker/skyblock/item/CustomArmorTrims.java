package me.xmrvizzy.skyblocker.skyblock.item;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.SkyblockEvents;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;

public class CustomArmorTrims {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomArmorTrims.class);
	public static final Object2ObjectOpenHashMap<String, Optional<ArmorTrim>> TRIMS_CACHE = new Object2ObjectOpenHashMap<>();
	private static final String[] TRIM_MATERIALS = { "quartz", "iron", "netherite", "redstone", "copper", "gold", "emerald", "diamond", "lapis", "amethyst" };
	private static final String[] TRIM_PATTERNS = { "sentry", "dune", "coast", "wild", "ward", "eye", "vex", "tide", "snout", "rib", "spire", "wayfinder", "shaper", "silence", "raiser", "host" };
	
	private static boolean trimsInitialized = false;
	
	public static void init() {
		SkyblockEvents.JOIN.register(CustomArmorTrims::initializeTrimCache);
		ClientCommandRegistrationCallback.EVENT.register(CustomArmorTrims::registerCommand);
	}
	
	private static void initializeTrimCache() {
		if (!trimsInitialized) {
			MinecraftClient client = MinecraftClient.getInstance();
			DynamicRegistryManager registryManager = client.player.networkHandler.getRegistryManager();
			
			for (String material: TRIM_MATERIALS) {
				for (String pattern: TRIM_PATTERNS) {
					String key = material + "+" + pattern;
					
					NbtCompound compound = new NbtCompound();
					compound.put("material", NbtString.of("minecraft:" + material));
					compound.put("pattern", NbtString.of("minecraft:" + pattern));
					
					ArmorTrim trim = ArmorTrim.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE, registryManager), compound).resultOrPartial(LOGGER::error).orElse(null);
					
					//Something went terribly wrong
					if (trim == null) throw new IllegalStateException("Trim shouldn't be null! [" + "\"" + material + "\",\"" + pattern + "\"]");
					
					TRIMS_CACHE.put(key, Optional.of(trim));
				}
			}
			
			LOGGER.info("[Skyblocker] Successfully cached all armor trims!");
			trimsInitialized = true;
		}
	}
	
	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("custom")
						.then(ClientCommandManager.literal("armorTrim")
								.executes(context -> customizeTrim(context.getSource(), null, null))
								.then(ClientCommandManager.argument("material", StringArgumentType.word())
										.suggests((context, builder) -> CommandSource.suggestMatching(TRIM_MATERIALS, builder))
										.then(ClientCommandManager.argument("pattern", StringArgumentType.word())
												.suggests((context, builder) -> CommandSource.suggestMatching(TRIM_PATTERNS, builder))
												.executes(context -> customizeTrim(context.getSource(), StringArgumentType.getString(context, "material").toLowerCase(), StringArgumentType.getString(context, "pattern").toLowerCase())))))));
	}
	
	private static int customizeTrim(FabricClientCommandSource source, String material, String pattern) {
		ItemStack heldItem = source.getPlayer().getMainHandStack();
		NbtCompound nbt = (heldItem != null) ? heldItem.getNbt() : null;
		
		if (Utils.isOnSkyblock() && heldItem != null) {
			if (heldItem.getItem() instanceof ArmorItem) {
				if (nbt != null && nbt.contains("ExtraAttributes")) {
					NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
					String itemUuid = extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;

					if (itemUuid != null) {
						Object2ObjectOpenHashMap<String, String> customArmorTrims = SkyblockerConfig.get().general.customArmorTrims;
						
						if (material == null && pattern == null) {
							if (customArmorTrims.containsKey(itemUuid)) {
								customArmorTrims.remove(itemUuid);
								SkyblockerConfig.save();
								source.sendFeedback(Text.translatable("skyblocker.customArmorTrims.removed"));
							} else {
								source.sendFeedback(Text.translatable("skyblocker.customArmorTrims.neverHad"));
							}
						} else {
							
							//Ensure that the material & trim are valid
							if (!Arrays.stream(TRIM_MATERIALS).anyMatch(material::equals) || !Arrays.stream(TRIM_PATTERNS).anyMatch(pattern::equals)) {
								source.sendError(Text.translatable("skyblocker.customArmorTrims.invalidMaterialOrPattern"));
								
								return Command.SINGLE_SUCCESS;
							}
							
							customArmorTrims.put(itemUuid, material + "+" + pattern);
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
}
