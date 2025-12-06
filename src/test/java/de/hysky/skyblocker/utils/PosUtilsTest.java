package de.hysky.skyblocker.utils;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PosUtilsTest {
	@Test
	void testParsePosString() {
		Assertions.assertEquals(PosUtils.parsePosString("-1,0,1"), new BlockPos(-1, 0, 1));
	}

	@Test
	void testParsePosJson() {
		Assertions.assertEquals(PosUtils.parsePosJson(SkyblockerMod.GSON.fromJson("{\"x\":-1,\"y\":0,\"z\":1}", JsonObject.class)), new BlockPos(-1, 0, 1));
	}

	@Test
	void testGetPosString() {
		Assertions.assertEquals(PosUtils.getPosString(new BlockPos(-1, 0, 1)), "-1,0,1");
	}
}
