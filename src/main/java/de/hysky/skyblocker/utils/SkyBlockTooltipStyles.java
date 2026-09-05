package de.hysky.skyblocker.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.text.GridComponent;

public final class SkyBlockTooltipStyles {
	public static final Identifier COMMON = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "common");
	public static final Identifier UNCOMMON = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "uncommon");
	public static final Identifier RARE = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "rare");
	public static final Identifier EPIC = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "epic");
	public static final Identifier LEGENDARY = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "legendary");
	public static final Identifier MYTHIC = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "mythic");
	public static final Identifier DIVINE = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "supreme");
	public static final Identifier SPECIAL = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "special");
	public static final Identifier VERY_SPECIAL = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "very_special");
	public static final Identifier ULTIMATE = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "ultimate");
	public static final Identifier ADMIN = Identifier.fromNamespaceAndPath(Utils.HYPIXEL_SKYBLOCK_NAMESPACE, "admin");

	public static Component applyCoinStyle(Component label, Component value) {
		if (SkyblockerConfigManager.get().general.itemTooltip.oldNeuItemValueStyle) {
			MutableComponent legacyLabel = Component.literal(label.getString()).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
			MutableComponent legacyValue = Component.literal(value.getString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
			return Component.empty().append(legacyLabel).append(" ").append(legacyValue);
		}
		return GridComponent.of(label, value);
	}
}
