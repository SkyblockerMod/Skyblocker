package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class LividColor {
	private static final Map<Block, ChatFormatting> WOOL_TO_FORMATTING = Map.of(
			Blocks.RED_WOOL, ChatFormatting.RED,
			Blocks.YELLOW_WOOL, ChatFormatting.YELLOW,
			Blocks.LIME_WOOL, ChatFormatting.GREEN,
			Blocks.GREEN_WOOL, ChatFormatting.DARK_GREEN,
			Blocks.BLUE_WOOL, ChatFormatting.BLUE,
			Blocks.MAGENTA_WOOL, ChatFormatting.LIGHT_PURPLE,
			Blocks.PURPLE_WOOL, ChatFormatting.DARK_PURPLE,
			Blocks.GRAY_WOOL, ChatFormatting.GRAY,
			Blocks.WHITE_WOOL, ChatFormatting.WHITE
	);
	private static final Map<String, ChatFormatting> LIVID_TO_FORMATTING = Map.of(
			"Hockey Livid", ChatFormatting.RED,
			"Arcade Livid", ChatFormatting.YELLOW,
			"Smile Livid", ChatFormatting.GREEN,
			"Frog Livid", ChatFormatting.DARK_GREEN,
			"Scream Livid", ChatFormatting.BLUE,
			"Crossed Livid", ChatFormatting.LIGHT_PURPLE,
			"Purple Livid", ChatFormatting.DARK_PURPLE,
			"Doctor Livid", ChatFormatting.GRAY,
			"Vendetta Livid", ChatFormatting.WHITE
	);
	private static final Supplier<DungeonsConfig.Livid> CONFIG = () -> SkyblockerConfigManager.get().dungeons.livid;
	private static ChatFormatting color = ChatFormatting.AQUA;
	private static Block lastColor = Blocks.AIR;

	private static boolean isInitialized = false;
	/**
	 * The correct livid may change color in M5, so we use the entity id to track the correct original livid.
	 */
	private static boolean correctLividIdFound = false;
	private static int correctLividId = 0;
	private static final long OFFSET_DURATION = 2000;
	private static long toggleTime = 0;

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> LividColor.reset());
		WorldRenderExtractionCallback.EVENT.register(LividColor::update);
	}

	private static void update(PrimitiveCollector collector) {
		DungeonsConfig.Livid config = SkyblockerConfigManager.get().dungeons.livid;
		if (!(config.enableLividColorText || config.enableLividColorTitle || config.enableLividColorGlow || config.enableLividColorBoundingBox)) return;

		Minecraft client = Minecraft.getInstance();

		if (!(Utils.isInDungeons() && DungeonManager.isInBoss() && client.player != null && client.level != null)) return;

		Block currentColor = client.level.getBlockState(new BlockPos(5, 110, 42)).getBlock();
		if (!(WOOL_TO_FORMATTING.containsKey(currentColor) && !currentColor.equals(lastColor))) return;

		if (!isInitialized && client.player.hasEffect(MobEffects.BLINDNESS)) {
			toggleTime = System.currentTimeMillis();
			isInitialized = true;
		} else if (isInitialized && System.currentTimeMillis() - toggleTime >= OFFSET_DURATION) {
			onLividColorFound(client, currentColor);
			if (!correctLividIdFound) {
				String lividName = LIVID_TO_FORMATTING.entrySet().stream()
						.filter(entry -> entry.getValue() == color)
						.map(Map.Entry::getKey)
						.findFirst()
						.orElse("unknown");
				client.level.players().stream()
						.filter(entity -> entity.getName().getString().equals(lividName))
						.findFirst()
						.ifPresent(entity -> correctLividId = entity.getId());
				correctLividIdFound = true;
			}
			lastColor = currentColor;
		}

	}

	private static void onLividColorFound(Minecraft client, Block color) {
		LividColor.color = WOOL_TO_FORMATTING.get(color);
		String colorString = BuiltInRegistries.BLOCK.getKey(color).getPath();
		colorString = colorString.substring(0, colorString.length() - 5).toUpperCase(Locale.ENGLISH);
		Component message = Component.literal(CONFIG.get().lividColorText.replaceAll("\\[color]", colorString)).withStyle(LividColor.color);
		if (CONFIG.get().enableLividColorText) {
			MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().append(message).getString(), true);
		}
		if (CONFIG.get().enableLividColorTitle) {
			client.gui.resetTitleTimes();
			client.gui.setTitle(message);
		}
	}

	public static boolean allowGlow() {
		return !SkyblockerConfigManager.get().dungeons.livid.enableLividColorGlow || !DungeonManager.getBoss().isFloor(5);
	}

	public static boolean shouldGlow(String name) {
		return SkyblockerConfigManager.get().dungeons.livid.enableLividColorGlow && color == LIVID_TO_FORMATTING.get(name);
	}

	public static boolean shouldDrawBoundingBox(String name) {
		return SkyblockerConfigManager.get().dungeons.livid.enableLividColorBoundingBox && color == LIVID_TO_FORMATTING.get(name);
	}

	@SuppressWarnings("DataFlowIssue")
	public static int getGlowColor(String name) {
		if (SkyblockerConfigManager.get().dungeons.livid.enableSolidColor) return SkyblockerConfigManager.get().dungeons.livid.customColor.getRGB();
		if (LIVID_TO_FORMATTING.containsKey(name)) return LIVID_TO_FORMATTING.get(name).getColor();
		return ChatFormatting.WHITE.getColor();
	}

	public static int getCorrectLividId() {
		return correctLividId;
	}

	private static void reset() {
		lastColor = Blocks.AIR;
		toggleTime = 0;
		isInitialized = false;
		correctLividIdFound = false;
		correctLividId = 0;
		color = ChatFormatting.AQUA;
	}
}
