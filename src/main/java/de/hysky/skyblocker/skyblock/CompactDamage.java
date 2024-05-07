package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.CustomArmorAnimatedDyes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;

import java.text.DecimalFormat;


public class CompactDamage {

	private CompactDamage() {
	}

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(SkyblockerConfigManager.get().general.compactDamage.format);

	public static void compactDamage(ArmorStandEntity entity) {
		if (!entity.isInvisible() || !entity.hasCustomName() || !entity.isCustomNameVisible() || entity.getFireTicks() != -1 || !entity.shouldHideBasePlate()) return;
		//Dmg armor stands have no base plate and have a fire time of -1 (just one of these isn't enough to determine if it's a dmg armor stand)
		//In fact, even this much checking might not be accurate. Needs testing, or just waiting until someone reports it as an issue
		JsonObject json = JsonParser.parseString(Text.Serialization.toJsonString(entity.getCustomName(), entity.getRegistryManager())).getAsJsonObject();
		JsonElement extra = json.get("extra");
		if (extra == null || !extra.isJsonArray()) return;
		JsonArray extraArray = extra.getAsJsonArray();
		if (extra.getAsJsonArray().isEmpty()) return;

		if (extraArray.size() == 1) { //Non-crit damage, just formatting with no color changes
			JsonElement first = extraArray.get(0);
			if (!first.isJsonObject()) return;
			JsonElement text = first.getAsJsonObject().get("text");
			if (text == null || !text.isJsonPrimitive()) return;
			long damage = Long.parseLong(text.getAsString().replace(",", ""));
			first.getAsJsonObject().addProperty("text", prettifyDamageNumber(damage));
			first.getAsJsonObject().addProperty("color", "#" + Integer.toHexString(SkyblockerConfigManager.get().general.compactDamage.normalDamageColor.getRGB() & 0x00FFFFFF));
		} else { //Crit damage
			//Already checked that the entity has a custom name above, ignore NPE warnings
			String text = entity.getCustomName().getString().replace(",", "").replace("✧", "");
			String prettyText = "✧" + prettifyDamageNumber(Long.parseLong(text)) + "✧";
			extra.getAsJsonArray().asList().clear();
			int length = prettyText.length();
			for (int i = 0; i < length; i++) {
				JsonObject obj = new JsonObject();
				obj.addProperty("text", prettyText.charAt(i));
				obj.addProperty("color", "#" + Integer.toHexString(
						CustomArmorAnimatedDyes.interpolate(
								SkyblockerConfigManager.get().general.compactDamage.critDamageGradientStart.getRGB() & 0x00FFFFFF,
								SkyblockerConfigManager.get().general.compactDamage.critDamageGradientEnd.getRGB() & 0x00FFFFFF,
								i / (length - 1.0)
						)
				));
				extraArray.add(obj);
			}
		}

		entity.setCustomName(Text.Serialization.fromJsonTree(json, entity.getRegistryManager()));
	}

	private static String prettifyDamageNumber(long damage) {
		if (damage < 1_000) return String.valueOf(damage);
		if (damage < 1_000_000) return DECIMAL_FORMAT.format(damage / 1_000.0) + "k";
		if (damage < 1_000_000_000) return DECIMAL_FORMAT.format(damage / 1_000_000.0) + "m";
		if (damage < 1_000_000_000_000L) return DECIMAL_FORMAT.format(damage / 1_000_000_000.0) + "b";
		return DECIMAL_FORMAT.format(damage / 1_000_000_000_000.0) + "t"; //This will probably never be reached
	}
}
