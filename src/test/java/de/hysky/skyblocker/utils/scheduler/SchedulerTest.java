package de.hysky.skyblocker.utils.scheduler;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;

public class SchedulerTest {
	private final MutableInt currentTick = new MutableInt(0);
	private final MutableInt cycleCount1 = new MutableInt(0);
	private final MutableInt cycleCount2 = new MutableInt(0);
	private final MutableInt cycleCount3 = new MutableInt(0);
	private final MutableInt cycleCount4 = new MutableInt(0);
	private final MutableInt cycleCount5 = new MutableInt(0);
	private final MutableInt cycleCount6 = new MutableInt(0);
	private final MutableInt cycleCount7 = new MutableInt(0);
	private final MutableInt cycleCount8 = new MutableInt(0);

	//@Test
	public void testSchedule() {
		Scheduler.INSTANCE.schedule(() -> Assertions.assertEquals(0, currentTick.intValue()), 0);
		Scheduler.INSTANCE.schedule(() -> Assertions.assertEquals(1, currentTick.intValue()), 1);
		Scheduler.INSTANCE.schedule(() -> Assertions.assertEquals(2, currentTick.intValue()), 2);
		Scheduler.INSTANCE.schedule(() -> Assertions.assertEquals(10, currentTick.intValue()), 10);
		Scheduler.INSTANCE.schedule(() -> Assertions.assertEquals(20, currentTick.intValue()), 20);
		Scheduler.INSTANCE.schedule(() -> Assertions.assertEquals(50, currentTick.intValue()), 50);
		Scheduler.INSTANCE.schedule(() -> Assertions.assertEquals(100, currentTick.intValue()), 100);
		Scheduler.INSTANCE.schedule(() -> Assertions.assertEquals(123, currentTick.intValue()), 123);
		Scheduler.INSTANCE.scheduleCyclic(() -> {}, 1);
		Scheduler.INSTANCE.scheduleCyclic(() -> {}, 1);
		Scheduler.INSTANCE.scheduleCyclic(() -> {}, 1);
		Scheduler.INSTANCE.scheduleCyclic(() -> {}, 1);
		Scheduler.INSTANCE.scheduleCyclic(() -> {
			Assertions.assertEquals(cycleCount1.intValue(), currentTick.intValue());
			cycleCount1.increment();
		}, 1);
		Scheduler.INSTANCE.scheduleCyclic(() -> {
			Assertions.assertEquals(0, currentTick.intValue() % 10);
			Assertions.assertEquals(cycleCount2.intValue(), currentTick.intValue() / 10);
			cycleCount2.increment();
		}, 10);
		Scheduler.INSTANCE.scheduleCyclic(() -> {
			Assertions.assertEquals(0, currentTick.intValue() % 55);
			Assertions.assertEquals(cycleCount3.intValue(), currentTick.intValue() / 55);
			cycleCount3.increment();
		}, 55);
		Scheduler.INSTANCE.schedule(() -> Scheduler.INSTANCE.scheduleCyclic(() -> {
			Assertions.assertEquals(7, currentTick.intValue() % 10);
			Assertions.assertEquals(cycleCount4.intValue(), currentTick.intValue() / 10);
			cycleCount4.increment();
		}, 10), 7);
		Scheduler.INSTANCE.schedule(() -> Scheduler.INSTANCE.scheduleCyclic(() -> {
			Assertions.assertEquals(0, currentTick.intValue() % 75);
			Assertions.assertEquals(cycleCount5.intValue(), currentTick.intValue() / 75);
			cycleCount5.increment();
		}, 75), 0);
		Scheduler.INSTANCE.schedule(() -> Scheduler.INSTANCE.scheduleCyclic(() -> {
			Assertions.assertEquals(1, currentTick.intValue() % 99);
			Assertions.assertEquals(cycleCount6.intValue(), currentTick.intValue() / 99);
			cycleCount6.increment();
		}, 99), 1);
		Scheduler.INSTANCE.scheduleCyclic(() -> Scheduler.INSTANCE.schedule(() -> {
			Assertions.assertEquals(5, currentTick.intValue() % 10);
			Assertions.assertEquals(cycleCount7.intValue(), currentTick.intValue() / 10);
			cycleCount7.increment();
		}, 5), 10);
		Scheduler.INSTANCE.scheduleCyclic(() -> Scheduler.INSTANCE.schedule(() -> {
			Assertions.assertEquals(10, currentTick.intValue() % 55);
			Assertions.assertEquals(cycleCount8.intValue(), currentTick.intValue() / 55);
			cycleCount8.increment();
		}, 10), 55);
		while (currentTick.intValue() < 100_000) {
			tick();
		}
		Assertions.assertEquals(100000, cycleCount1.intValue());
		Assertions.assertEquals(10000, cycleCount2.intValue());
		Assertions.assertEquals(1819, cycleCount3.intValue());
		Assertions.assertEquals(10000, cycleCount4.intValue());
		Assertions.assertEquals(1334, cycleCount5.intValue());
		Assertions.assertEquals(1011, cycleCount6.intValue());
		Assertions.assertEquals(10000, cycleCount7.intValue());
		Assertions.assertEquals(1818, cycleCount8.intValue());
	}

	private void tick() {
		Scheduler.INSTANCE.tick();
		currentTick.increment();
	}
}
