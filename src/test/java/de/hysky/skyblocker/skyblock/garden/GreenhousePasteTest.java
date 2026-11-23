package de.hysky.skyblocker.skyblock.garden;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class GreenhousePasteTest {
	private static final BlockPos CORNER = new BlockPos(-5, 73, -101);

	@Test
	void plantBoyCell() {
		// A Plantboy covering cells 3-4 along x and 6-7 along z stands where those cells meet
		Assertions.assertArrayEquals(new int[]{3, 6}, GreenhousePaste.plantBoyCell(new Vec3(-5 + 4, 74, -101 + 7), CORNER));

		// On the south east edge, covering cells 8-9 along both axes
		Assertions.assertArrayEquals(new int[]{8, 8}, GreenhousePaste.plantBoyCell(new Vec3(-5 + 9, 74, -101 + 9), CORNER));

		// A 2x2 that doesn't fit in the grid is skipped instead of writing past it
		Assertions.assertNull(GreenhousePaste.plantBoyCell(new Vec3(-5 + 10, 74, -101 + 9), CORNER));
		Assertions.assertNull(GreenhousePaste.plantBoyCell(new Vec3(-5 + 9, 74, -101 + 10), CORNER));
		Assertions.assertNull(GreenhousePaste.plantBoyCell(new Vec3(-5, 74, -101 + 4), CORNER));
	}

	@Test
	void parsePastedLayout() {
		// SkyLayouts links reach their importer whole, since long links hold their boards after a ~
		String longLink = "https://skylayouts.io/l/1AF~p3KAB~1~1";
		Assertions.assertEquals(new GreenhousePaste.PastedLayout(GreenhousePaste.LayoutSite.SKY_LAYOUTS, longLink), GreenhousePaste.parsePastedLayout("  " + longLink + " "));
		Assertions.assertEquals(new GreenhousePaste.PastedLayout(GreenhousePaste.LayoutSite.SKY_LAYOUTS, "https://skylayouts.io/l/s1zmd9hGL38uvpNOY"),
				GreenhousePaste.parsePastedLayout("https://skylayouts.io/l/s1zmd9hGL38uvpNOY"));
		Assertions.assertEquals(new GreenhousePaste.PastedLayout(GreenhousePaste.LayoutSite.SKY_LAYOUTS, "skylayouts.io/layout.html?m=DUSTGRAIN&i=5&ranked=dustgrain_s5_plot1"),
				GreenhousePaste.parsePastedLayout("skylayouts.io/layout.html?m=DUSTGRAIN&i=5&ranked=dustgrain_s5_plot1"));

		Assertions.assertEquals(new GreenhousePaste.PastedLayout(GreenhousePaste.LayoutSite.SKY_SHARDS, "ABC"), GreenhousePaste.parsePastedLayout("https://greenhouse.skyshards.com/designer?layout=ABC"));
		Assertions.assertEquals(new GreenhousePaste.PastedLayout(GreenhousePaste.LayoutSite.SKY_MUTATIONS, "ABC"), GreenhousePaste.parsePastedLayout("https://skymutations.eu/?layout=ABC"));
		Assertions.assertEquals(new GreenhousePaste.PastedLayout(GreenhousePaste.LayoutSite.UNKNOWN, "ABC"), GreenhousePaste.parsePastedLayout("ABC"));
	}

	@Test
	void extractLayoutCode() {
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?layout=ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/?layout=ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://api.skyshards.com/share/ABC"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("  ABC  "));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?tab=1&layout=ABC&mode=x"));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://api.skyshards.com/share/ABC?foo=bar"));
		Assertions.assertEquals("a-b_c=", GreenhousePaste.extractLayoutCode("https://greenhouse.skyshards.com/designer?layout=a-b_c="));
		Assertions.assertEquals("ABC", GreenhousePaste.extractLayoutCode("https://skymutations.eu/?layout=ABC"));
		Assertions.assertEquals("NrBMBoIIgLQewLYCMCuATK4AMBdcZwAWcWRVDbPAgNhPmXU13wgA46zHL9jp7ymVYsVIMKzYMVqiB3SeHYyuE2n07iqtEf2Wbw0nRvy1FhwfnZqx54O23qb7Aw7ntTLiQEZw3qABEAUwBDNAACAAUAGyCAOwAXAGdMTypvaECQiOj4pJ9U8ABmEgBhAEsYtACAJwSACxCA5PyRDLCo2MSm-G8AVhIAMUiAuriqoIAHLuBvaVasjtyU7vAAdhLyypr6yqnvRTn2nN3wAE4SA+zOvJYfc+C2y8WqCCL-e-mj64I+t8zDq6WBDWvweCymEDOII+AKoRV8g2GtVGEymRWgCJGY0mXyKrzKFWqdQaqKIdz+jxJPwxSKxJNm73+T3wRWB+M2RJ2OIUAyGmJRXMh1OR2MBxF8FzBX2Irwln1F4B+sphPFWZNBcqEpzV0KZwD64oZFK+fXShslgL6eI2hO2jWNpKhjKmfSpvJp-It+m1TvtrOtW2J9v2Zo1+D6kKVutoBvJ5r0MpDyuAtEViajqsdRsBtAjaama3hbuF+cgPMRxa+aytBIDnMBaxaecrCrLfJFVDW9NjoeAaz9NY5dvr3KgQtpzcFRfHgPYMfVSfY1fZtqm7Fd5enVHY-eXgZnWszcfwZznOqmZ1N3aTZwTV91Z0bd-PLcPPbOXfn94zkefwafXzOXN-xSHAgA",
				GreenhousePaste.extractLayoutCode("https://skymutations.eu/greenhouse?layout=NrBMBoIIgLQewLYCMCuATK4AMBdcZwAWcWRVDbPAgNhPmXU13wgA46zHL9jp7ymVYsVIMKzYMVqiB3SeHYyuE2n07iqtEf2Wbw0nRvy1FhwfnZqx54O23qb7Aw7ntTLiQEZw3qABEAUwBDNAACAAUAGyCAOwAXAGdMTypvaECQiOj4pJ9U8ABmEgBhAEsYtACAJwSACxCA5PyRDLCo2MSm-G8AVhIAMUiAuriqoIAHLuBvaVasjtyU7vAAdhLyypr6yqnvRTn2nN3wAE4SA+zOvJYfc+C2y8WqCCL-e-mj64I+t8zDq6WBDWvweCymEDOII+AKoRV8g2GtVGEymRWgCJGY0mXyKrzKFWqdQaqKIdz+jxJPwxSKxJNm73+T3wRWB+M2RJ2OIUAyGmJRXMh1OR2MBxF8FzBX2Irwln1F4B+sphPFWZNBcqEpzV0KZwD64oZFK+fXShslgL6eI2hO2jWNpKhjKmfSpvJp-It+m1TvtrOtW2J9v2Zo1+D6kKVutoBvJ5r0MpDyuAtEViajqsdRsBtAjaama3hbuF+cgPMRxa+aytBIDnMBaxaecrCrLfJFVDW9NjoeAaz9NY5dvr3KgQtpzcFRfHgPYMfVSfY1fZtqm7Fd5enVHY-eXgZnWszcfwZznOqmZ1N3aTZwTV91Z0bd-PLcPPbOXfn94zkefwafXzOXN-xSHAgA&fake_param=true")
		);
	}
}
