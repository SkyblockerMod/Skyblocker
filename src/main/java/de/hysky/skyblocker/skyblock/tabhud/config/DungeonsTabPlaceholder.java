package de.hysky.skyblocker.skyblock.tabhud.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.ArrayList;
import java.util.List;

public final class DungeonsTabPlaceholder {

	private static List<Text> placeholder = null;

	public static List<Text> get() {
		if (placeholder != null) return placeholder;
		List<Text> l = new ArrayList<>();
		List<String> json = List.of(
				"{\"text\":\"\",\"extra\":[\"         \",{\"text\":\"Party \",\"color\":\"aqua\",\"bold\":true},{\"text\":\"(1)\",\"color\":\"white\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"303\",\"color\":\"blue\"},{\"text\":\"] \",\"color\":\"dark_gray\"},{\"text\":\"AzureAaron \",\"color\":\"aqua\"},{\"text\":\"⚡ \",\"color\":\"gold\",\"bold\":true},{\"text\":\"(\",\"color\":\"white\"},{\"text\":\"\",\"color\":\"light_purple\"},{\"text\":\"EMPTY\",\"color\":\"gray\"},{\"text\":\")\",\"color\":\"white\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Ultimate: \",{\"text\":\"\",\"color\":\"yellow\"},{\"text\":\"N/A\",\"color\":\"red\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Revive Stones: \",{\"text\":\"0\",\"color\":\"red\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\"       \",{\"text\":\"Player Stats\",\"color\":\"dark_green\",\"bold\":true}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Downed: \",\"color\":\"green\",\"bold\":true},{\"text\":\"NONE\",\"color\":\"gray\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Time: \",{\"text\":\"N/A\",\"color\":\"yellow\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Revive: \",{\"text\":\"N/A\",\"color\":\"red\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Team Deaths: \",\"color\":\"green\",\"bold\":true},{\"text\":\"0\",\"color\":\"white\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Team Damage Dealt: \",{\"text\":\"0❤\",\"color\":\"red\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Team Healing Done: \",{\"text\":\"0❤\",\"color\":\"red\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Your Milestone: \",{\"text\":\"?\",\"color\":\"yellow\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Discoveries: \",\"color\":\"green\",\"bold\":true},{\"text\":\"0\",\"color\":\"white\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Secrets Found: \",{\"text\":\"0\",\"color\":\"aqua\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Crypts: \",{\"text\":\"0\",\"color\":\"gold\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\"       \",{\"text\":\"Dungeon Stats\",\"color\":\"dark_aqua\",\"bold\":true}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Dungeon: \",\"color\":\"aqua\",\"bold\":true},{\"text\":\"Catacombs\",\"color\":\"gray\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Opened Rooms: \",{\"text\":\"0\",\"color\":\"dark_purple\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Completed Rooms: \",{\"text\":\"0\",\"color\":\"light_purple\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Secrets Found: \",{\"text\":\"0%\",\"color\":\"yellow\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Time: \",{\"text\":\"Soon!\",\"color\":\"gold\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Puzzles: \",\"color\":\"aqua\",\"bold\":true},{\"text\":\"(2)\",\"color\":\"white\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" ???: \",{\"text\":\"[\",\"color\":\"gray\"},{\"text\":\"✦\",\"color\":\"gold\",\"bold\":true},{\"text\":\"]\",\"color\":\"gray\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" ???: \",{\"text\":\"[\",\"color\":\"gray\"},{\"text\":\"✦\",\"color\":\"gold\",\"bold\":true},{\"text\":\"]\",\"color\":\"gray\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\"       \",{\"text\":\"Account Info\",\"color\":\"gold\",\"bold\":true}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Profile: \",\"color\":\"yellow\",\"bold\":true},{\"text\":\"Blueberry\",\"color\":\"green\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Pet Sitter: \",{\"text\":\"N/A\",\"color\":\"aqua\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Bank: \",{\"text\":\"1B\",\"color\":\"gold\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Interest: \",{\"text\":\"27 Hours\",\"color\":\"yellow\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Skills: \",\"color\":\"yellow\",\"bold\":true},{\"text\":\"Foraging 23: \",\"color\":\"green\"},{\"text\":\"11.4%\",\"color\":\"dark_aqua\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Speed: \",{\"text\":\"✦319\",\"color\":\"white\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Strength: \",{\"text\":\"❁5222\",\"color\":\"red\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Crit Chance: \",{\"text\":\"☣168\",\"color\":\"blue\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Crit Damage: \",{\"text\":\"☠3588\",\"color\":\"blue\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Attack Speed: \",{\"text\":\"⚔100\",\"color\":\"yellow\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Event: \",\"color\":\"yellow\",\"bold\":true},{\"text\":\"Election Booth Opens\",\"color\":\"aqua\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" Starts In: \",{\"text\":\"26h\",\"color\":\"yellow\"}],\"italic\":false}",
				"{\"text\":\"\",\"italic\":false}",
				"{\"text\":\"\",\"extra\":[{\"text\":\"Election: \",\"color\":\"yellow\",\"bold\":true},{\"text\":\"Over!\",\"color\":\"red\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" \",{\"text\":\"Winner: \",\"color\":\"white\"},{\"text\":\"Marina\",\"color\":\"green\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" \",{\"text\":\"Participants: \",\"color\":\"white\"},{\"text\":\"84,448\",\"color\":\"aqua\"}],\"italic\":false}",
				"{\"text\":\"\",\"extra\":[\" \",{\"text\":\"Year: \",\"color\":\"white\"},{\"text\":\"358\",\"color\":\"light_purple\"}],\"italic\":false}");

		for (String s : json) {
			l.add(TextCodecs.CODEC.decode(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(s, JsonElement.class)).getOrThrow().getFirst());
		}

		return placeholder = ImmutableList.copyOf(l);
	}
}
