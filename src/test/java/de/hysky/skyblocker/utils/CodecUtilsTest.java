package de.hysky.skyblocker.utils;

import java.awt.Color;

import com.mojang.serialization.JavaOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodecUtilsTest {
	@Test
	void testColorCodec() {
		Assertions.assertEquals(new Color(255, 0, 0, 255), CodecUtils.COLOR_CODEC.parse(JavaOps.INSTANCE, "FFFF0000").getOrThrow());
		Assertions.assertEquals(new Color(255, 0, 0, 255), CodecUtils.COLOR_CODEC.parse(JavaOps.INSTANCE, -65536).getOrThrow());
		Assertions.assertEquals(new Color(0, 0, 255, 127), CodecUtils.COLOR_CODEC.parse(JavaOps.INSTANCE, "7F0000FF").getOrThrow());
		Assertions.assertEquals(new Color(0, 0, 255, 127), CodecUtils.COLOR_CODEC.parse(JavaOps.INSTANCE, 2130706687).getOrThrow());

		Assertions.assertEquals("FFFF0000", CodecUtils.COLOR_CODEC.encodeStart(JavaOps.INSTANCE, new Color(255, 0, 0, 255)).getOrThrow());
		Assertions.assertEquals("7F0000FF", CodecUtils.COLOR_CODEC.encodeStart(JavaOps.INSTANCE, new Color(0, 0, 255, 127)).getOrThrow());
	}
}
