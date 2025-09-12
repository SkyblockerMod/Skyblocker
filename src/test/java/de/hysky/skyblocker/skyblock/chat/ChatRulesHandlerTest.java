package de.hysky.skyblocker.skyblock.chat;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class ChatRulesHandlerTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void formatText() {
		//generate test text
		MutableText testText = Text.empty();
		Text.of("test").getWithStyle(Style.EMPTY.withFormatting(Formatting.DARK_BLUE)).forEach(testText::append);
		Text.of("line").getWithStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE)).forEach(testText::append);
		Text.of("dark green").getWithStyle(Style.EMPTY.withFormatting(Formatting.DARK_GREEN)).forEach(testText::append);
		Text.of("italic").getWithStyle(Style.EMPTY.withFormatting(Formatting.ITALIC)).forEach(testText::append);

		//generated text
		MutableText text = ChatRulesHandler.formatText("&1test&nline&2dark green&oitalic");

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
