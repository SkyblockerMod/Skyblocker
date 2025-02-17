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
		Assertions.assertFalse(testRule.isMatch("test"));
		//test simple filter works
		testRule.setEnabled(true);
		Assertions.assertTrue(testRule.isMatch("test"));
		//test partial match works
		Assertions.assertFalse(testRule.isMatch("test extra"));
		testRule.setPartialMatch(true);
		Assertions.assertTrue(testRule.isMatch("test extra"));
		//test ignore case works
		Assertions.assertTrue(testRule.isMatch("TEST"));
		testRule.setIgnoreCase(false);
		Assertions.assertFalse(testRule.isMatch("TEST"));

		//test regex
		testRule = new ChatRule();
		testRule.setRegex(true);
		testRule.setFilter("[0-9]+");
		Assertions.assertTrue(testRule.isMatch("1234567"));
		Assertions.assertFalse(testRule.isMatch("1234567 test"));
	}

	@Test
	void codecFixTest() {
		// Testing to see if the string/enum set decoding codec works properly
		// Encoding is left to the actual enum set codec, and that's beyond the scope of this test.
		Assertions.assertEquals(ChatRule.LOCATION_FIXING_CODEC.decode(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("Dwarven Mines, Jerry's Workshop, The Park")).getOrThrow().getFirst(), EnumSet.of(Location.DWARVEN_MINES, Location.WINTER_ISLAND, Location.THE_PARK));
		Assertions.assertEquals(ChatRule.LOCATION_FIXING_CODEC.decode(JsonOps.INSTANCE,
						JsonOps.INSTANCE.createList(Stream.of(Location.DWARVEN_MINES, Location.WINTER_ISLAND, Location.THE_PARK)
														  .map(Location::asString)
														  .map(JsonOps.INSTANCE::createString))).getOrThrow().getFirst(),
				EnumSet.of(Location.DWARVEN_MINES, Location.WINTER_ISLAND, Location.THE_PARK));
	}
}