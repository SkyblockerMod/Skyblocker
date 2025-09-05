package de.hysky.skyblocker.skyblock.shortcut;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.InputUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ShortcutsTest {
	private static final String SHORTCUTS_JSON_OLD = "{\"commands\":{\"/s\":\"/skyblock\"},\"commandArgs\":{\"/pa\":\"/p accept\"}}";
	private static final String SHORTCUTS_JSON = "{\"commands\":{\"/s\":\"/skyblock\"},\"commandArgs\":{\"/pa\":\"/p accept\"},\"keyBindings\":{\"key.keyboard.k\":\"/skyblock\"}}";
	private static final String SHORTCUTS_JSON_KEY_COMBO = "{\"commands\":{\"/s\":\"/skyblock\"},\"commandArgs\":{\"/pa\":\"/p accept\"},\"keyBindings\":{\"key.keyboard.s + key.keyboard.k\":\"/skyblock\",\"key.keyboard.p + key.keyboard.v\":\"/pv\"}}";

	@BeforeAll
	public static void setup() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void testShortcutsParse() {
		Shortcuts.ShortcutsRecord expected = new Shortcuts.ShortcutsRecord(Object2ObjectMaps.singleton("/s", "/skyblock"), Object2ObjectMaps.singleton("/pa", "/p accept"), Object2ObjectMaps.singleton(new ShortcutKeyBinding(List.of(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_K, -1))), "/skyblock"));
		Shortcuts.ShortcutsRecord shortcuts = Shortcuts.ShortcutsRecord.CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(SHORTCUTS_JSON, JsonObject.class)).getOrThrow();

		Assertions.assertEquals(expected, shortcuts);
	}

	@Test
	void testShortcutsParseKeyCombo() {
		Shortcuts.ShortcutsRecord expected = new Shortcuts.ShortcutsRecord(Object2ObjectMaps.singleton("/s", "/skyblock"), Object2ObjectMaps.singleton("/pa", "/p accept"), new Object2ObjectOpenHashMap<>(Map.of(new ShortcutKeyBinding(List.of(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_S, -1), InputUtil.fromKeyCode(InputUtil.GLFW_KEY_K, -1))), "/skyblock", new ShortcutKeyBinding(List.of(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_P, -1), InputUtil.fromKeyCode(InputUtil.GLFW_KEY_V, -1))), "/pv")));
		Shortcuts.ShortcutsRecord shortcuts = Shortcuts.ShortcutsRecord.CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(SHORTCUTS_JSON_KEY_COMBO, JsonObject.class)).getOrThrow();

		Assertions.assertEquals(expected, shortcuts);
	}

	@Test
	void testShortcutsEncode() {
		Shortcuts.ShortcutsRecord shortcuts = new Shortcuts.ShortcutsRecord(Object2ObjectMaps.singleton("/s", "/skyblock"), Object2ObjectMaps.singleton("/pa", "/p accept"), Object2ObjectMaps.singleton(new ShortcutKeyBinding(List.of(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_K, -1))), "/skyblock"));
		String shortcutsJson = SkyblockerMod.GSON_COMPACT.toJson(Shortcuts.ShortcutsRecord.CODEC.encodeStart(JsonOps.INSTANCE, shortcuts).getOrThrow());

		Assertions.assertEquals(SHORTCUTS_JSON, shortcutsJson);
	}

	@Test
	void testShortcutsEncodeKeyCombo() {
		Shortcuts.ShortcutsRecord shortcuts = new Shortcuts.ShortcutsRecord(Object2ObjectMaps.singleton("/s", "/skyblock"), Object2ObjectMaps.singleton("/pa", "/p accept"), new Object2ObjectOpenHashMap<>(Map.of(new ShortcutKeyBinding(List.of(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_S, -1), InputUtil.fromKeyCode(InputUtil.GLFW_KEY_K, -1))), "/skyblock", new ShortcutKeyBinding(List.of(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_P, -1), InputUtil.fromKeyCode(InputUtil.GLFW_KEY_V, -1))), "/pv")));
		JsonElement shortcutsJson = Shortcuts.ShortcutsRecord.CODEC.encodeStart(JsonOps.INSTANCE, shortcuts).getOrThrow();

		Assertions.assertEquals(SkyblockerMod.GSON.fromJson(SHORTCUTS_JSON_KEY_COMBO, JsonObject.class), shortcutsJson); // Convert to json to prevent issues with hash map iteration order
	}

	@Test
	void testShortcutsModify() {
		Shortcuts.ShortcutsRecord shortcuts = Shortcuts.ShortcutsRecord.CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(SHORTCUTS_JSON_OLD, JsonObject.class)).getOrThrow();
		shortcuts.keyBindings().put(new ShortcutKeyBinding(List.of(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_S, -1), InputUtil.fromKeyCode(InputUtil.GLFW_KEY_K, -1))), "/skyblock");
		shortcuts.keyBindings().put(new ShortcutKeyBinding(List.of(InputUtil.fromKeyCode(InputUtil.GLFW_KEY_P, -1), InputUtil.fromKeyCode(InputUtil.GLFW_KEY_V, -1))), "/pv");
		JsonElement shortcutsJson = Shortcuts.ShortcutsRecord.CODEC.encodeStart(JsonOps.INSTANCE, shortcuts).getOrThrow();

		Assertions.assertEquals(SkyblockerMod.GSON.fromJson(SHORTCUTS_JSON_KEY_COMBO, JsonObject.class), shortcutsJson); // Convert to json to prevent issues with hash map iteration order
	}
}
