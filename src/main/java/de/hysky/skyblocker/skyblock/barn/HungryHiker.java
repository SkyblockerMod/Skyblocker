package de.hysky.skyblocker.skyblock.barn;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class HungryHiker extends ChatPatternListener {

	private static final Map<String, String> foods;

	public HungryHiker() { super("^\\[NPC] Hungry Hiker: (The food I want is|(I asked for) food that is) ([a-zA-Z, '\\-]*\\.)$"); }

	@Override
	public ChatFilterResult state() {
	return SkyblockerConfigManager.get().otherLocations.barn.solveHungryHiker ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	public boolean onMatch(Component message, Matcher matcher) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return false;
		String foodDescription = matcher.group(3);
		String food = foods.get(foodDescription);
		if (food == null) return false;
		String middlePartOfTheMessageToSend = matcher.group(2) != null ? matcher.group(2) : matcher.group(1);
		Utils.sendMessageToBypassEvents(Component.nullToEmpty("§e[NPC] Hungry Hiker§f: " + middlePartOfTheMessageToSend + " " + food + "."));
		return true;
	}

	static {
		foods = new HashMap<>();
		foods.put("from a cow.", Component.translatable("item.minecraft.cooked_beef").getString());
		foods.put("meat from a fowl.", Component.translatable("item.minecraft.cooked_chicken").getString());
		foods.put("red on the inside, green on the outside.", Component.translatable("item.minecraft.melon_slice").getString());
		foods.put("a cooked potato.", Component.translatable("item.minecraft.baked_potato").getString());
		foods.put("a stew.", Component.translatable("item.minecraft.rabbit_stew").getString());
		foods.put("a grilled meat.", Component.translatable("item.minecraft.cooked_porkchop").getString());
		foods.put("red and crunchy.", Component.translatable("item.minecraft.apple").getString());
		foods.put("made of wheat.", Component.translatable("item.minecraft.bread").getString());
	}
}
