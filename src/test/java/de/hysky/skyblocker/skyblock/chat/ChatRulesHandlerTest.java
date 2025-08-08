package de.hysky.skyblocker.skyblock.chat;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ChatRulesHandlerTest {

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
	void codecParseLegacy() {
		List<ChatRule> rules = List.of(
				new ChatRule(),
				new ChatRule()
		);
		var oldObject = new JsonObject();
		oldObject.add("rules", ChatRule.LIST_CODEC.encodeStart(JsonOps.INSTANCE, rules).getOrThrow());

		var parsed = ChatRulesHandler.UNBOXING_CODEC.parse(JsonOps.INSTANCE, oldObject).getOrThrow();
		Assertions.assertEquals(rules, parsed);
	}

	@Test
	void codecParseNew() {
		List<ChatRule> rules = List.of(
				new ChatRule(),
				new ChatRule()
		);
		var unboxedList = ChatRule.LIST_CODEC.encodeStart(JsonOps.INSTANCE, rules).getOrThrow();

		var parsed = ChatRulesHandler.UNBOXING_CODEC.parse(JsonOps.INSTANCE, unboxedList).getOrThrow();
		Assertions.assertEquals(rules, parsed);
	}

	@Test
	void codecEncode() {
		List<ChatRule> rules = List.of(
				new ChatRule(),
				new ChatRule()
		);

		var list = ChatRule.LIST_CODEC.encodeStart(JsonOps.INSTANCE, rules).getOrThrow();
		var encodedList = ChatRulesHandler.UNBOXING_CODEC.encodeStart(JsonOps.INSTANCE, rules).getOrThrow();
		Assertions.assertEquals(list, encodedList);
	}
}
