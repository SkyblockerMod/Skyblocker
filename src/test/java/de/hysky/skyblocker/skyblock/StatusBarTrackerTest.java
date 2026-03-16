package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StatusBarTrackerTest {

	void assertStats(int hp, int maxHp, int def, int mana, int maxMana, int overflowMana) {
		int absorption = 0;
		if (hp > maxHp) {
			absorption = Math.min(hp - maxHp, maxHp);
			hp = maxHp;
		}
		assertEquals(new StatusBarTracker.Resource(hp, maxHp, absorption), StatusBarTracker.getHealth());
		if (def != -1) {
			assertEquals(def, StatusBarTracker.getDefense());
		}
		assertEquals(new StatusBarTracker.Resource(mana, maxMana, overflowMana), StatusBarTracker.getMana());
	}

	@Test
	void normalStatusBar() {
		String res = StatusBarTracker.update("§c934/1086❤     §a159§a❈ Defense     §b562/516✎ Mana", false);
		assertNull(res);
		assertStats(934, 1086, 159, 562, 516, 0);
	}

	@Test
	void overflowMana() {
		String res = StatusBarTracker.update("§61605/1305❤     §a270§a❈ Defense     §b548/548✎ §3200ʬ", false);
		assertNull(res);
		assertStats(1605, 1305, 270, 548, 548, 200);
	}

	@Test
	void regeneration() {
		String res = StatusBarTracker.update("§c2484/2484❤+§c120▄     §a642§a❈ Defense     §b2557/2611✎ Mana", false);
		assertEquals("§c❤+§c120▄", res);
	}

	@Test
	void instantTransmission() {
		String actionBar = "§c2259/2259❤     §b-20 Mana (§6Instant Transmission§b)     §b549/2676✎ Mana";
		assertEquals("§b-20 Mana (§6Instant Transmission§b)", StatusBarTracker.update(actionBar, false));
		assertNull(StatusBarTracker.update(actionBar, true));
	}

	@Test
	void rapidFire() {
		String actionBar = "§c2509/2509❤     §b-48 Mana (§6Rapid-fire§b)     §b2739/2811✎ Mana";
		assertEquals("§b-48 Mana (§6Rapid-fire§b)", StatusBarTracker.update(actionBar, false));
		assertNull(StatusBarTracker.update(actionBar, true));
	}

	@Test
	void zombieSword() {
		String actionBar = "§c2509/2509❤     §b-56 Mana (§6Instant Heal§b)     §b2674/2821✎ Mana    §e§lⓩⓩⓩⓩ§6§lⓄ";
		assertEquals("§b-56 Mana (§6Instant Heal§b)     §e§lⓩⓩⓩⓩ§6§lⓄ", StatusBarTracker.update(actionBar, false));
		assertEquals("§e§lⓩⓩⓩⓩ§6§lⓄ", StatusBarTracker.update(actionBar, true));
	}

	@Test
	void campfire() {
		String res = StatusBarTracker.update("§c17070/25565❤+§c170▃   §65,625 DPS   §c1 second     §b590/626✎ §3106ʬ", false);
		assertEquals("§c❤+§c170▃   §65,625 DPS   §c1 second", res);
	}

	@Test
	void inTheRift() {
		Location located = Utils.getLocation();
		Utils.setTestLocation(Location.THE_RIFT);
		String res = StatusBarTracker.update("§771m31sф Left     §7⏣ §dWizard Tower     §b209/209✎ Mana", false);
		assertEquals("§7⏣ §dWizard Tower", res);
		Utils.setTestLocation(located);
	}

	@Test
	void cropFeverPartial() {
		String res = StatusBarTracker.update("§64,652/4,277❤     §3+7.4 Farming (390,447,412/§f§l0§e§l)     §a§l5§b§l3§c§l2§d§l/§e§l5§f§l3§e§l2§d§l✎ §b§lM§a§la§9§ln§a§la", false);
		assertEquals("§3+7.4 Farming (390,447,412/§f§l0§e§l)", res);
		assertStats(4652, 4277, -1, 532, 532, 0);
	}

	@Test
	void cropFeverFull() {
		String res = StatusBarTracker.update("§e§l4§f§l,§e§l6§d§l5§c§l2§b§l/§a§l4§9§l,§a§l2§b§l7§c§l7§d§l❤§e§l+§f§l1§e§l7§d§l0§c§l▆     §c§l+§d§l7§e§l.§f§l5 §d§lF§c§la§b§lr§a§lm§9§li§a§ln§b§lg §d§l(§e§l3§f§l9§e§l3§d§l,§c§l3§b§l0§a§l0§9§l,§a§l8§b§l6§c§l3§d§l/§e§l0§f§l)     §9§l4§a§l3§b§l5§c§l/§d§l5§e§l3§b2✎ §3400ʬ", false);
		assertEquals("§d§l❤§e§l+§f§l1§e§l7§d§l0§c§l▆     §c§l+§d§l7§e§l.§f§l5 §d§lF§c§la§b§lr§a§lm§9§li§a§ln§b§lg §d§l(§e§l3§f§l9§e§l3§d§l,§c§l3§b§l0§a§l0§9§l,§a§l8§b§l6§c§l3§d§l/§e§l0§f§l)", res);
		assertStats(4652, 4277, -1, 435, 532, 400);
	}
}
