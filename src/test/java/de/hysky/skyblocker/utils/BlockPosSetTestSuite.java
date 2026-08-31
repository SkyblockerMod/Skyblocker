package de.hysky.skyblocker.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import net.minecraft.core.BlockPos;

@NullMarked
public class BlockPosSetTestSuite {
	@TestFactory
	public List<DynamicTest> test() {
		TestSuite suite = SetTestSuiteBuilder
				.using(new TestSetGenerator<BlockPos>() {
					@Override
					public Set<BlockPos> create(Object... elements) {
						Set<BlockPos> customLongSet = new BlockPosSet();
						for (Object element : elements) {
							customLongSet.add((BlockPos) element);
						}
						return customLongSet;
					}

					@Override
					public SampleElements<BlockPos> samples() {
						return new SampleElements<>(
								new BlockPos(0, 0, 0),
								new BlockPos(1, 0, -1),
								new BlockPos(-1, 0, 1),
								new BlockPos(30_000_000, 0, 30_000_000),
								new BlockPos(-30_000_000, 0, -30_000_000)
						);
					}

					@Override
					public BlockPos[] createArray(int length) {
						return new BlockPos[length];
					}

					@Override
					public Iterable<BlockPos> order(List<BlockPos> insertionOrder) {
						return insertionOrder;
					}
				})
				.named("Block Pos Set Test Suite")
				.withFeatures(
						CollectionSize.ANY,
						CollectionFeature.RESTRICTS_ELEMENTS,
						CollectionFeature.GENERAL_PURPOSE
				)
				.createTestSuite();

		List<DynamicTest> dynamicTests = new ArrayList<>();

		extractTests(suite, dynamicTests);
		return dynamicTests;
	}

	private void extractTests(Test test, List<DynamicTest> dynamicTests) {
		if (test instanceof TestSuite suite) {
			for (Test t : Collections.list(suite.tests())) {
				extractTests(t, dynamicTests);
			}
		} else if (test instanceof TestCase testCase) {
			dynamicTests.add(DynamicTest.dynamicTest(testCase.getName(), testCase::runBare));
		}
	}
}
