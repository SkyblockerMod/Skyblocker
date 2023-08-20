package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArmorTrimIdSerializationTest {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Identifier.class, new Identifier.Serializer()).create();

    @Test
    void serialize() {
        CustomArmorTrims.ArmorTrimId armorTrimId = new CustomArmorTrims.ArmorTrimId(new Identifier("material_id"), new Identifier("pattern_id"));
        String json = gson.toJson(armorTrimId);
        String expectedJson = "{\"material\":\"minecraft:material_id\",\"pattern\":\"minecraft:pattern_id\"}";
        Assertions.assertEquals(expectedJson, json);
    }

    @Test
    void deserialize() {
        String json = "{\"material\":\"minecraft:material_id\",\"pattern\":\"minecraft:pattern_id\"}";
        CustomArmorTrims.ArmorTrimId armorTrimId = gson.fromJson(json, CustomArmorTrims.ArmorTrimId.class);
        CustomArmorTrims.ArmorTrimId expectedArmorTrimId = new CustomArmorTrims.ArmorTrimId(new Identifier("material_id"), new Identifier("pattern_id"));
        Assertions.assertEquals(expectedArmorTrimId, armorTrimId);
    }
}
