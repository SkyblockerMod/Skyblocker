package de.hysky.skyblocker.skyblock.foraging;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SweepOverlayTest {

	@Test
	void testSweepCalculations() {
		List<Integer> figSweepValues = SweepOverlay.REQUIRED_SWEEP_VALUES.get("FIG");
		List<Integer> mangroveSweepValues = SweepOverlay.REQUIRED_SWEEP_VALUES.get("MANGROVE");
		List<Integer> helixSweepValues = SweepOverlay.REQUIRED_SWEEP_VALUES.get("HELIX");

		testSweepCalculations(figSweepValues);
		testSweepCalculations(mangroveSweepValues);
		testSweepCalculations(helixSweepValues);
	}

	static void testSweepCalculations(List<Integer> sweepValues) {
		for (int i = 0; i < sweepValues.size(); i++) {
			int requiredSweep = sweepValues.get(i);
			int expectedLogs = i + 1;

			Assertions.assertEquals(expectedLogs, SweepOverlay.calculateMaxWood(requiredSweep, sweepValues));
		}
	}
}
