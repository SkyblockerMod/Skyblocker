package de.hysky.skyblocker.skyblock.chat;

import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.utils.Location;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.stream.Stream;

class ChatRuleTest {

	@Test
	void isMatch() {
		ChatRule testRule = new ChatRule();
		//test enabled check
		testRule.setFilter("test");
		testRule.setEnabled(false);
		Assertions.assertFalse(testRule.isMatch("test").matches());
		//test simple filter works
		testRule.setEnabled(true);
		Assertions.assertTrue(testRule.isMatch("test").matches());
		//test partial match works
		Assertions.assertFalse(testRule.isMatch("test extra").matches());
		testRule.setPartialMatch(true);
		Assertions.assertTrue(testRule.isMatch("test extra").matches());
		//test ignore case works
		Assertions.assertTrue(testRule.isMatch("TEST").matches());
		testRule.setIgnoreCase(false);
		Assertions.assertFalse(testRule.isMatch("TEST").matches());

		//test regex
		testRule = new ChatRule();
		testRule.setRegex(true);
		testRule.setFilter("[0-9]+");
		Assertions.assertTrue(testRule.isMatch("1234567").matches());
		Assertions.assertFalse(testRule.isMatch("1234567 test").matches());
	}

	@Test
	void replaceMessage() {
		ChatRule testRule = new ChatRule();
		testRule.setRegex(true);
		testRule.setIgnoreCase(false);
		testRule.setFilter("(\\d+)\\D+(\\d+)");
		testRule.setPartialMatch(true);
		testRule.setChatMessage("Number: $1; Another number: $2");
		Assertions.assertEquals("Number: 1234567890; Another number: 123", testRule.isMatch("this is a number 1234567890 and some more text and 123 and even more text").insertCaptureGroups(testRule.getChatMessage()));
	}

	@Test
	void codecParseLegacy() {
		// Testing to see if the string/enum set decoding codec works properly
		// Encoding is left to the actual enum set codec, and that's beyond the scope of this test.
		Assertions.assertEquals(
				EnumSet.of(Location.DWARVEN_MINES, Location.WINTER_ISLAND, Location.THE_PARK),
				ChatRule.LOCATION_FIXING_CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("Dwarven Mines, Jerry's Workshop, The Park")).getOrThrow()
		);

		Assertions.assertEquals(
				EnumSet.of(Location.HUB),
				ChatRule.LOCATION_FIXING_CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("hub")).getOrThrow()
		);
	}

	@Test
	void codecParseLegacyExclusion() {
		Assertions.assertEquals(
				EnumSet.complementOf(EnumSet.of(Location.WINTER_ISLAND, Location.DEEP_CAVERNS)),
				ChatRule.LOCATION_FIXING_CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("!Jerry's Workshop, !Deep Caverns")).getOrThrow()
		);

		Assertions.assertEquals(
				EnumSet.complementOf(EnumSet.of(Location.DWARVEN_MINES)),
				ChatRule.LOCATION_FIXING_CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("!Dwarven Mines, Jerry's Workshop, The Park")).getOrThrow()
		);
	}

	@Test
	void codecParseEnumSet() {
		Assertions.assertEquals(
				EnumSet.of(Location.DWARVEN_MINES, Location.WINTER_ISLAND, Location.THE_PARK),
				ChatRule.LOCATION_FIXING_CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createList(Stream.of(Location.DWARVEN_MINES, Location.WINTER_ISLAND, Location.THE_PARK)
						.map(Location::getSerializedName)
						.map(JsonOps.INSTANCE::createString)
				)).getOrThrow()
		);
	}

	@Test
	void codecParseEmptySet() {
		Assertions.assertEquals(
				EnumSet.noneOf(Location.class),
				ChatRule.LOCATION_FIXING_CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createList(Stream.empty())).getOrThrow()
		);
	}

	@Test
	void codecEncode() {
		Assertions.assertEquals(
				JsonOps.INSTANCE.createList(Stream.empty()),
				ChatRule.LOCATION_FIXING_CODEC.encodeStart(JsonOps.INSTANCE, EnumSet.noneOf(Location.class)).getOrThrow()
		);

		Assertions.assertEquals(
				JsonOps.INSTANCE.createList(Stream.of(Location.DWARVEN_MINES, Location.WINTER_ISLAND)
						.map(Location::getSerializedName)
						.map(JsonOps.INSTANCE::createString)
				),
				ChatRule.LOCATION_FIXING_CODEC.encodeStart(JsonOps.INSTANCE, EnumSet.of(Location.DWARVEN_MINES, Location.WINTER_ISLAND)).getOrThrow()
		);
	}
}
