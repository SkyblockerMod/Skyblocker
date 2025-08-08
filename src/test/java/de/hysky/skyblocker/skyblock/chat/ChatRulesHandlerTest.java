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
		Style style = Style.EMPTY.withFormatting(Formatting.DARK_BLUE);
		Text.of("test").getWithStyle(style).forEach(testText::append);
		style = style.withFormatting(Formatting.UNDERLINE);
		Text.of("line").getWithStyle(style).forEach(testText::append);
		style = style.withFormatting(Formatting.DARK_GREEN);
		Text.of("dark green").getWithStyle(style).forEach(testText::append);
		style = style.withFormatting(Formatting.ITALIC);
		Text.of("italic").getWithStyle(style).forEach(testText::append);

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
