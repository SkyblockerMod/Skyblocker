package de.hysky.skyblocker.skyblock.hunting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AttributesTest {
	@Test
	void testToShardName() {
		Assertions.assertEquals("Grove", Attributes.toShardName("Grove"));
		Assertions.assertEquals("Carrot King", Attributes.toShardName("Carrot King Shard"));
		Assertions.assertEquals("Scarf", Attributes.toShardName("Scarf Shard x2"));
		Assertions.assertEquals("Cavernfish", Attributes.toShardName("BUY Cavernfish Shard"));
		Assertions.assertEquals("Wetwing", Attributes.toShardName("SELL Wetwing Shard"));
		Assertions.assertEquals("Shard", Attributes.toShardName("Shard"));
	}
}
