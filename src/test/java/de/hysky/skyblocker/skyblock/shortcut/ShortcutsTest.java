package de.hysky.skyblocker.skyblock.shortcut;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.InputUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ShortcutsTest {
	private static final String SHORTCUTS_JSON_OLD = "{\"commands\":{\"/s\":\"/skyblock\"},\"commandArgs\":{\"/pa\":\"/p accept\"}}";
	private static final String SHORTCUTS_JSON = "{\"commands\":{\"/s\":\"/skyblock\"},\"commandArgs\":{\"/pa\":\"/p accept\"},\"keyBindings\":{\"key.keyboard.k\":\"/skyblock\"}}";

	@BeforeAll
	public static void setup() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void testShortcutsParse() {
		Shortcuts.ShortcutsRecord expected = new Shortcuts.ShortcutsRecord(Object2ObjectMaps.singleton("/s", "/skyblock"), Object2ObjectMaps.singleton("/pa", "/p accept"), Object2ObjectMaps.singleton(new ShortcutKeyBinding(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_K, -1)), "/skyblock"));
		Shortcuts.ShortcutsRecord shortcuts = Shortcuts.ShortcutsRecord.CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(SHORTCUTS_JSON, JsonObject.class)).getOrThrow();

		Assertions.assertEquals(expected, shortcuts);
	}

	@Test
	void testShortcutsParseOld() {
		Shortcuts.ShortcutsRecord shortcuts = Shortcuts.ShortcutsRecord.CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(SHORTCUTS_JSON_OLD, JsonObject.class)).getOrThrow();
		shortcuts.keyBindings().put(new ShortcutKeyBinding(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_K, -1)), "/skyblock");
		String shortcutsJson = SkyblockerMod.GSON_COMPACT.toJson(Shortcuts.ShortcutsRecord.CODEC.encodeStart(JsonOps.INSTANCE, shortcuts).getOrThrow());

		Assertions.assertEquals(SHORTCUTS_JSON, shortcutsJson);
	}

	@Test
	void testShortcutsEncode() {
		Shortcuts.ShortcutsRecord shortcuts = new Shortcuts.ShortcutsRecord(Object2ObjectMaps.singleton("/s", "/skyblock"), Object2ObjectMaps.singleton("/pa", "/p accept"), Object2ObjectMaps.singleton(new ShortcutKeyBinding(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_K, -1)), "/skyblock"));
		String shortcutsJson = SkyblockerMod.GSON_COMPACT.toJson(Shortcuts.ShortcutsRecord.CODEC.encodeStart(JsonOps.INSTANCE, shortcuts).getOrThrow());

		Assertions.assertEquals(SHORTCUTS_JSON, shortcutsJson);
	}
}
