package de.hysky.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.skyblock.item.CustomArmorTrims.ArmorTrimId;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArmorTrimIdSerializationTest {
    private final Gson gson = new Gson();

    @Test
    void serialize() {
        ArmorTrimId armorTrimId = new ArmorTrimId(new Identifier("material_id"), new Identifier("pattern_id"));
        JsonElement json = ArmorTrimId.CODEC.encodeStart(JsonOps.INSTANCE, armorTrimId).getOrThrow();
        String expectedJson = "{\"material\":\"minecraft:material_id\",\"pattern\":\"minecraft:pattern_id\"}";

        Assertions.assertEquals(expectedJson, json.toString());
    }

    @Test
    void deserialize() {
        String json = "{\"material\":\"minecraft:material_id\",\"pattern\":\"minecraft:pattern_id\"}";
        ArmorTrimId armorTrimId = ArmorTrimId.CODEC.parse(JsonOps.INSTANCE, gson.fromJson(json, JsonElement.class)).getOrThrow();
        ArmorTrimId expectedArmorTrimId = new ArmorTrimId(new Identifier("material_id"), new Identifier("pattern_id"));

        Assertions.assertEquals(expectedArmorTrimId, armorTrimId);
    }
}
