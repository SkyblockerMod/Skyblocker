package de.hysky.skyblocker.skyblock.barn;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class HungryHiker extends ChatPatternListener {

    private static final Map<String, String> foods;

    public HungryHiker() { super("^\\[NPC] Hungry Hiker: (The food I want is|(I asked for) food that is) ([a-zA-Z, '\\-]*\\.)$"); }

    @Override
    public ChatFilterResult state() {
    return SkyblockerConfigManager.get().locations.barn.solveHungryHiker ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        String foodDescription = matcher.group(3);
        String food = foods.get(foodDescription);
        if (food == null) return false;
        String middlePartOfTheMessageToSend = matcher.group(2) != null ? matcher.group(2) : matcher.group(1);
        Utils.sendMessageToBypassEvents(Text.of("§e[NPC] Hungry Hiker§f: " + middlePartOfTheMessageToSend + " " + food + "."));
        return true;
    }

    static {
        foods = new HashMap<>();
        foods.put("from a cow.", Text.translatable("item.minecraft.cooked_beef").getString());
        foods.put("meat from a fowl.", Text.translatable("item.minecraft.cooked_chicken").getString());
        foods.put("red on the inside, green on the outside.", Text.translatable("item.minecraft.melon_slice").getString());
        foods.put("a cooked potato.", Text.translatable("item.minecraft.baked_potato").getString());
        foods.put("a stew.", Text.translatable("item.minecraft.rabbit_stew").getString());
        foods.put("a grilled meat.", Text.translatable("item.minecraft.cooked_porkchop").getString());
        foods.put("red and crunchy.", Text.translatable("item.minecraft.apple").getString());
        foods.put("made of wheat.", Text.translatable("item.minecraft.bread").getString());
    }
}
