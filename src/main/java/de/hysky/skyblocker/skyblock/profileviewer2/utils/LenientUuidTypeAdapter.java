package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.UUIDUtil;

public class LenientUuidTypeAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {

	@Override
	public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return UUIDUtil.AUTHLIB_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
	}

	@Override
	public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
		return UUIDUtil.AUTHLIB_CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
	}
}
