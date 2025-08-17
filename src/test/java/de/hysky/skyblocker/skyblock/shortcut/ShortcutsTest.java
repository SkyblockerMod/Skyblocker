package de.hysky.skyblocker.skyblock.shortcut;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ShortcutsTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void testShortcutsParse() {
		var expected = new Shortcuts.ShortcutsRecord(Object2ObjectMaps.singleton("/s", "/skyblock"), Object2ObjectMaps.singleton("/pa", "/p accept"), Object2ObjectMaps.emptyMap());
		var shortcuts = Shortcuts.ShortcutsRecord.CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson("{\"commands\":{\"/s\":\"/skyblock\"},\"commandArgs\":{\"/pa\":\"/p accept\"}}", JsonObject.class)).getOrThrow();

		Assertions.assertEquals(expected, shortcuts);
	}

	@Test
	void testShortcutsEncode() {
		var expected = "{\"commands\":{\"/s\":\"/skyblock\"},\"commandArgs\":{\"/pa\":\"/p accept\"}}";
		var shortcuts = new Shortcuts.ShortcutsRecord(Object2ObjectMaps.singleton("/s", "/skyblock"), Object2ObjectMaps.singleton("/pa", "/p accept"), Object2ObjectMaps.emptyMap());
		var shortcutsJson = SkyblockerMod.GSON_COMPACT.toJson(Shortcuts.ShortcutsRecord.CODEC.encodeStart(JsonOps.INSTANCE, shortcuts).getOrThrow());

		Assertions.assertEquals(expected, shortcutsJson);
	}
}
