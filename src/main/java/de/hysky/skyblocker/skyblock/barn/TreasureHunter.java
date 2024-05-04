package de.hysky.skyblocker.skyblock.barn;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class TreasureHunter extends ChatPatternListener {

    private static final Map<String, String> locations;

    public TreasureHunter() { super("^\\[NPC] Treasure Hunter: ([a-zA-Z, '\\-\\.]*)$"); }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfigManager.get().locations.barn.solveTreasureHunter ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        String hint = matcher.group(1);
        String location = locations.get(hint);
        if (location == null) return false;
        client.player.sendMessage(Text.of("§e[NPC] Treasure Hunter§f: Go mine around " + location + "."), false);
        return true;
    }

    static {
        locations = new HashMap<>();
        locations.put("There's a treasure chest somewhere in a small cave in the gorge.", "258 70 -492");
        locations.put("I was in the desert earlier, and I saw something near a red sand rock.", "357 82 -319");
        locations.put("There's this guy who collects animals to experiment on, I think I saw something near his house.", "259 184 -564");
        locations.put("There's a small house in the gorge, I saw some treasure near there.", "297 87 -562");
        locations.put("There's this guy who says he has the best sheep in the world. I think I saw something around his hut.", "392 85 -372");
        locations.put("I spotted something by an odd looking mushroom on one of the ledges in the Mushroom Gorge, you should check it out.", "305 73 -557");
        locations.put("There are some small ruins out in the desert, might want to check them out.", "320 102 -471");
        locations.put("Some dirt was kicked up by the water pool in the overgrown Mushroom Cave. Have a look over there.", "234 56 -410");
        locations.put("There are some old stone structures in the Mushroom Gorge, give them a look.", "223 54 -503");
        locations.put("In the Mushroom Gorge where blue meets the ceiling and floor, you will find what you are looking for.", "205 42 -527");
        locations.put("There was a haystack with a crop greener than usual around it, I think there is something near there.", "334 82 -389");
        locations.put("There's a single piece of tall grass growing in the desert, I saw something there.", "283 76 -363");
        locations.put("I saw some treasure by a cow skull near the village.", "141 77 -397");
        locations.put("Near a melon patch inside a tunnel in the mountain I spotted something.", "257 100 -569");
        locations.put("I saw something near a farmer's cart, you should check it out.", "155 90 -591");
        locations.put("I remember there was a stone pillar made only of cobblestone in the oasis, could be something there.", "122 66 -409");
        locations.put("I thought I saw something near the smallest stone pillar in the oasis.", "94 65 -455");
        locations.put("I found something by a mossy stone pillar in the oasis, you should take a look.", "179 93 -537");
        locations.put("Down in the glowing Mushroom Cave, there was a weird looking mushroom, check it out.", "182 44 -451");
        locations.put("Something caught my eye by the red sand near the bridge over the gorge.", "306 105 -489");
        locations.put("I seem to recall seeing something near the well in the village.", "170 77 -375");
        locations.put("I was down near the lower oasis yesterday, I think I saw something under the bridge.", "142 69 -448");
        locations.put("I was at the upper oasis today, I recall seeing something on the cobblestone stepping stones.", "188 77 -459");
    }
}
