package de.hysky.skyblocker.skyblock.hunting;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

public class AttributeSerializationTest {

	@Test
	void testAttributeDeserialization() throws Exception {
		try (InputStream stream = this.getClass().getResourceAsStream("/assets/skyblocker/hunting/attributes.json")) {
			Attribute.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(new String(stream.readAllBytes()))).getOrThrow();
		}
	}
}
