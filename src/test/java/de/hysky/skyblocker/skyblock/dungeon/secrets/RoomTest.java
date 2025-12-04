package de.hysky.skyblocker.skyblock.dungeon.secrets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RoomTest {
	@Test
	void onChatMessage() {
		Assertions.assertFalse(Room.isAllSecretsFound("§10,000/10,000❤     §a5,000§a❈ Defense     §b2,000/2,000✎ Mana    §70/1 Secrets"));
		Assertions.assertTrue(Room.isAllSecretsFound("§1,000,000/10,000❤     §3+1,000.5 Combat (33.33%)     §b4,000/2,000✎ Mana    §710/10 Secrets"));
		Assertions.assertTrue(Room.isAllSecretsFound("§1,000,000/10,000❤     §b-25 Mana (§6Instant Transmission§b)     §b2,000/2,000✎ Mana    §710/1 Secrets"));
	}
}
