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
		Assertions.assertEquals("empty[siblings=[literal{[}[style={color=gray}], empty[siblings=[literal{S}[style={color=#00FF4C}], literal{k}[style={color=#00FB6D}], literal{y}[style={color=#00F686}], literal{b}[style={color=#00F29C}], literal{l}[style={color=#00EDAF}], literal{o}[style={color=#00E8C0}], literal{c}[style={color=#00E2D1}], literal{k}[style={color=#00DCE1}], literal{e}[style={color=#02D6F0}], literal{r}[style={color=#14D0FF}]]], literal{] }[style={color=gray}]]]", Constants.PREFIX.get().toString());
	}
}
