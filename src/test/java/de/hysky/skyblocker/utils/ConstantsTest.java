package de.hysky.skyblocker.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ConstantsTest {
	@BeforeAll
	static void beforeAll() {
		System.setProperty("skyblocker.iAmABoringPersonAndHateFun", "true");
	}

	@AfterAll
	static void afterAll() {
		System.setProperty("skyblocker.iAmABoringPersonAndHateFun", "");
	}

	@Test
	void testPrefix() {
		Assertions.assertEquals("empty[siblings=[literal{[}[style={color=gray}], empty[siblings=[literal{S}[style={color=#00FE4B}], literal{k}[style={color=#00FA6D}], literal{y}[style={color=#00F686}], literal{b}[style={color=#00F19B}], literal{l}[style={color=#00ECAE}], literal{o}[style={color=#00E7C0}], literal{c}[style={color=#00E2D0}], literal{k}[style={color=#00DCE0}], literal{e}[style={color=#01D6F0}], literal{r}[style={color=#14CFFE}]]], literal{] }[style={color=gray}]]]", Constants.PREFIX.get().toString());
	}
}
