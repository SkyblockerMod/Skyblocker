package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemAbility;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PickaxeAbility {
	private static final Pattern COOLDOWN = Pattern.compile("Your Pickaxe ability is on cooldown for (\\d+)s");
	private static final Set<String> PICKAXE_ABILITIES = ObjectSet.of(
			"Mining Speed Boost",
			"Pickobulus",
			"Tunnel Vision",
			"Maniac Miner",
			"Gemstone Infusion",
			"Sheer Force"
	);
	private static @Nullable String cachedAbility;
	private static int cooldown = -1;
	private static int cooldownTime = -1;

	@Init
	public static void init() {
		ClientTickEvents.END_WORLD_TICK.register(level -> {
			findPickaxeAbility(Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem());
			if (cachedAbility == null || cooldownTime >= cooldown || cooldownTime < 0) return;
			cooldownTime++;
		});
		ClientReceiveMessageEvents.ALLOW_GAME.register((component, overlay) -> {
			if (!overlay) onChatMessage(component.getString().trim());
			return true;
		});
	}

	private static void onChatMessage(String string) {
		if (cachedAbility == null) return;
		if (string.equals("You used your " + cachedAbility + " Pickaxe Ability!")) {
			cooldownTime = 0;
			return;
		} else if (string.equals(cachedAbility + " is now available!")) {
			cooldownTime = cooldown + 1;
		}
		Matcher matcher = COOLDOWN.matcher(string);
		if (matcher.find()) {
			int i = NumberUtils.toInt(matcher.group(1), 0);
			cooldownTime = cooldown - i;
		}
	}

	public static boolean canHavePickaxeAbility(ItemStack stack) {
		return ItemUtils.getLoreLineIf(stack, s -> s.startsWith("Breaking Power")) != null;
	}

	/**
	 * @return empty if we know for sure the cooldown is over from the "is now available!" message.
	 */
	public static OptionalDouble getCooldownPercentage() {
		if (cachedAbility == null || cooldownTime < 0 || cooldownTime > cooldown) return OptionalDouble.empty();
		return OptionalDouble.of((double) cooldownTime / cooldown);
	}

	private static void findPickaxeAbility(ItemStack stack) {
		if (!canHavePickaxeAbility(stack)) return;
		for (ItemAbility ability : stack.skyblocker$getAbilities()) {
			if (PICKAXE_ABILITIES.contains(ability.name()) && ability.cooldown().isPresent()) {
				cachedAbility = ability.name();
				cooldown = ability.cooldown().getAsInt();
				return;
			}
		}
		cachedAbility = null;
		cooldownTime = -1;
		cooldown = -1;
	}
}
