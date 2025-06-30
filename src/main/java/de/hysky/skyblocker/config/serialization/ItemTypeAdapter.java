package de.hysky.skyblocker.config.serialization;

import java.lang.reflect.Type;
import java.util.Locale;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemTypeAdapter implements JsonSerializer<Item>, JsonDeserializer<Item> {

	@Override
	public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return Registries.ITEM.get(Identifier.of(json.getAsString().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public JsonElement serialize(Item src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(Registries.ITEM.getId(src).toString());
	}
}
