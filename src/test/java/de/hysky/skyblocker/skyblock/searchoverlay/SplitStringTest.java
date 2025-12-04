package de.hysky.skyblocker.skyblock.searchoverlay;

import it.unimi.dsi.fastutil.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SplitStringTest {
	@Test
	void testSplitString1() {
		String input = "aaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaa a";
		Pair<String, String> split = SearchOverManager.splitString(input);
		Assertions.assertEquals("aaaaaaaaaaaaaaa", split.left());
		Assertions.assertEquals("aaaaaaaaaaaaaaa", split.right());
	}

	@Test
	void testSplitString2() {
		String input = "a a a a aaa aa aa aaaa aaa aa aa aa a a aa aaa a aaa aa";
		Pair<String, String> split = SearchOverManager.splitString(input);
		Assertions.assertEquals("a a a a aaa aa", split.left());
		Assertions.assertEquals("aa aaaa aaa aa", split.right());
	}

	@Test
	void testSplitString3() {
		String input = "aaaaa aaaaa aaaaa";
		Pair<String, String> split = SearchOverManager.splitString(input);
		Assertions.assertEquals("aaaaa aaaaa", split.left());
		Assertions.assertEquals("aaaaa", split.right());
	}
}
