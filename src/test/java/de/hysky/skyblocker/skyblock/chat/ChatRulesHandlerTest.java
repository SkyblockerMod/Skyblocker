package de.hysky.skyblocker.skyblock.chat;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.TextTransformer;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class ChatRulesHandlerTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}
	@Test
	void styleFromComponent() {
		// these are real message components sent by hypixel, figured no better way to test
		MutableComponent newBuff = Component.empty().withStyle(style -> style.withItalic(false));
		newBuff.append(Component.literal("New buff").withStyle(ChatFormatting.YELLOW));
		newBuff.append(Component.empty());
		newBuff.append(Component.empty().append(Component.literal(": ").withStyle(style -> style.withBold(false).withItalic(false).withUnderlined(false).withStrikethrough(false).withObfuscated(false))));
		newBuff.append(Component.literal("Gain ").withStyle(ChatFormatting.WHITE));
		newBuff.append(Component.literal("+5% ").withStyle(ChatFormatting.GREEN));
		newBuff.append(Component.literal("∮ Sweep").withStyle(ChatFormatting.DARK_GREEN));
		newBuff.append(Component.literal(".").withStyle(ChatFormatting.WHITE));

		MutableComponent watchdog = Component.literal("Watchdog has banned ").withStyle(ChatFormatting.WHITE);
		watchdog.append(Component.literal("5,565").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
		watchdog.append(Component.literal(" players in the last 7 days.").withStyle(style -> style.withColor(ChatFormatting.WHITE).withBold(false)));

		MutableComponent pressure = Component.empty().withStyle(style -> style.withItalic(false));
		pressure.append(Component.literal(" ☠ ").withStyle(ChatFormatting.RED));
		pressure.append(Component.empty().withStyle(ChatFormatting.GRAY));
		pressure.append(Component.literal("NOT_LEGEND_").withStyle(ChatFormatting.GREEN));
		pressure.append(Component.literal(" fainted from pressure").withStyle(ChatFormatting.GRAY));
		pressure.append(Component.literal(".").withStyle(ChatFormatting.GRAY));

		// yes, even this one... wtf hypixel
		MutableComponent spacer = Component.empty().withStyle(style -> style.withItalic(false));
		spacer.append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY));
		spacer.append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY));
		spacer.append(Component.literal(" ").withStyle(ChatFormatting.DARK_BLUE));
		spacer.append(Component.literal(" ").withStyle(ChatFormatting.DARK_AQUA));
		spacer.append(Component.literal(" ").withStyle(ChatFormatting.DARK_AQUA));
		spacer.append(Component.literal(" ").withStyle(ChatFormatting.GRAY));
		spacer.append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY));

		Assertions.assertEquals("&eNew buff&r: &fGain &a+5% &2∮ Sweep&f.", TextTransformer.toLegacy(newBuff));
		Assertions.assertEquals("&fWatchdog has banned &c&l5,565&f players in the last 7 days.", TextTransformer.toLegacy(watchdog));
		Assertions.assertEquals("&c ☠ &aNOT_LEGEND_&7 fainted from pressure.", TextTransformer.toLegacy(pressure));
		Assertions.assertEquals("      &8 ", TextTransformer.toLegacy(spacer));
	}

	@Test
	void formatText() {
		//generate test text
		MutableComponent testText = Component.empty();
		Component.nullToEmpty("test").toFlatList(Style.EMPTY.applyFormat(ChatFormatting.DARK_BLUE)).forEach(testText::append);
		Component.nullToEmpty("line").toFlatList(Style.EMPTY.applyFormat(ChatFormatting.UNDERLINE)).forEach(testText::append);
		Component.nullToEmpty("dark green").toFlatList(Style.EMPTY.applyFormat(ChatFormatting.DARK_GREEN)).forEach(testText::append);
		Component.nullToEmpty("italic").toFlatList(Style.EMPTY.applyFormat(ChatFormatting.ITALIC)).forEach(testText::append);

		//generated text
		MutableComponent text = ChatRulesHandler.formatText("&1test&nline&2dark green&oitalic");

		Assertions.assertEquals(text, testText);
	}

	@Test
	void codecParseObjectOld() {
		var object = SkyblockerMod.GSON.fromJson("{\"rules\":[{\"showAnnouncement\":false,\"replaceMessage\":\"\",\"validLocations\":\"hub\",\"hideMessage\":true,\"showActionBar\":false,\"isRegex\":true,\"isIgnoreCase\":true,\"filter\":\"(selling)|(buying)|(lowb)|(visit)|(/p)|(/ah)|(my ah)\",\"name\":\"Clean Hub Chat\",\"enabled\":false,\"isPartialMatch\":true},{\"showAnnouncement\":true,\"replaceMessage\":\"&1Ability\",\"customSound\":{\"sound_id\":\"minecraft:entity.arrow.hit_player\"},\"validLocations\":\"Crystal Hollows, Dwarven Mines\",\"hideMessage\":false,\"showActionBar\":false,\"isRegex\":false,\"isIgnoreCase\":true,\"filter\":\"is now available!\",\"name\":\"Mining Ability Alert\",\"enabled\":false,\"isPartialMatch\":true}]}", JsonObject.class);
		var parsed = ChatRulesHandler.UNBOXING_CODEC.parse(JsonOps.INSTANCE, object).getOrThrow();

		Assertions.assertEquals(ChatRulesHandler.getDefaultChatRules(), parsed);
	}

	@Test
	void codecParseObjectNew() {
		var object = SkyblockerMod.GSON.fromJson("{\"rules\":[{\"showAnnouncement\":false,\"replaceMessage\":\"\",\"validLocations\":[\"hub\"],\"hideMessage\":true,\"showActionBar\":false,\"isRegex\":true,\"isIgnoreCase\":true,\"filter\":\"(selling)|(buying)|(lowb)|(visit)|(/p)|(/ah)|(my ah)\",\"name\":\"Clean Hub Chat\",\"enabled\":false,\"isPartialMatch\":true},{\"showAnnouncement\":true,\"replaceMessage\":\"&1Ability\",\"customSound\":{\"sound_id\":\"minecraft:entity.arrow.hit_player\"},\"validLocations\":[\"mining_3\",\"crystal_hollows\"],\"hideMessage\":false,\"showActionBar\":false,\"isRegex\":false,\"isIgnoreCase\":true,\"filter\":\"is now available!\",\"name\":\"Mining Ability Alert\",\"enabled\":false,\"isPartialMatch\":true}]}", JsonObject.class);
		var parsed = ChatRulesHandler.UNBOXING_CODEC.parse(JsonOps.INSTANCE, object).getOrThrow();

		Assertions.assertEquals(ChatRulesHandler.getDefaultChatRules(), parsed);
		Assertions.assertDoesNotThrow(parsed::removeLast);
	}

	@Test
	void codecParseList() {
		List<ChatRule> rules = ChatRulesHandler.getDefaultChatRules();
		var unboxedList = ChatRule.LIST_CODEC.encodeStart(JsonOps.INSTANCE, rules).getOrThrow();
		var parsed = ChatRulesHandler.UNBOXING_CODEC.parse(JsonOps.INSTANCE, unboxedList).getOrThrow();

		Assertions.assertEquals(rules, parsed);
		Assertions.assertDoesNotThrow(parsed::removeLast);
	}

	@Test
	void codecEncode() {
		List<ChatRule> rules = ChatRulesHandler.getDefaultChatRules();
		var object = new JsonObject();
		object.add("rules", ChatRule.LIST_CODEC.encodeStart(JsonOps.INSTANCE, rules).getOrThrow());
		var encodedObject = ChatRulesHandler.UNBOXING_CODEC.encodeStart(JsonOps.INSTANCE, rules).getOrThrow();

		Assertions.assertEquals(object, encodedObject);
	}
}
