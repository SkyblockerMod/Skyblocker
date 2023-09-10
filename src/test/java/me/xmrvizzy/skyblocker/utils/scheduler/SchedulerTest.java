package me.xmrvizzy.skyblocker.utils.scheduler;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SchedulerTest {
    @SuppressWarnings("deprecation")
    private final Scheduler scheduler = new Scheduler();
    private final MutableInt currentTick = new MutableInt(0);
    private final MutableInt cycleCount1 = new MutableInt(1);
    private final MutableInt cycleCount2 = new MutableInt(0);
    private final MutableInt cycleCount3 = new MutableInt(0);
    private final MutableInt cycleCount4 = new MutableInt(0);
    private final MutableInt cycleCount5 = new MutableInt(0);
    private final MutableInt cycleCount6 = new MutableInt(0);
    private final MutableInt cycleCount7 = new MutableInt(0);
    private final MutableInt cycleCount8 = new MutableInt(0);

    @Test
    public void testSchedule() {
        scheduler.schedule(() -> Assertions.assertEquals(1, currentTick.intValue()), 0);
        scheduler.schedule(() -> Assertions.assertEquals(1, currentTick.intValue()), 1);
        scheduler.schedule(() -> Assertions.assertEquals(2, currentTick.intValue()), 2);
        scheduler.schedule(() -> Assertions.assertEquals(10, currentTick.intValue()), 10);
        scheduler.schedule(() -> Assertions.assertEquals(20, currentTick.intValue()), 20);
        scheduler.schedule(() -> Assertions.assertEquals(50, currentTick.intValue()), 50);
        scheduler.schedule(() -> Assertions.assertEquals(100, currentTick.intValue()), 100);
        scheduler.schedule(() -> Assertions.assertEquals(123, currentTick.intValue()), 123);
        scheduler.scheduleCyclic(() -> {}, 1);
        scheduler.scheduleCyclic(() -> {}, 1);
        scheduler.scheduleCyclic(() -> {}, 1);
        scheduler.scheduleCyclic(() -> {}, 1);
        scheduler.scheduleCyclic(() -> {
            Assertions.assertEquals(cycleCount1.intValue(), currentTick.intValue());
            cycleCount1.increment();
        }, 1);
        scheduler.scheduleCyclic(() -> {
            Assertions.assertEquals(1, currentTick.intValue() % 10);
            Assertions.assertEquals(cycleCount2.intValue(), currentTick.intValue() / 10);
            cycleCount2.increment();
        }, 10);
        scheduler.scheduleCyclic(() -> {
            Assertions.assertEquals(1, currentTick.intValue() % 55);
            Assertions.assertEquals(cycleCount3.intValue(), currentTick.intValue() / 55);
            cycleCount3.increment();
        }, 55);
        scheduler.schedule(() -> scheduler.scheduleCyclic(() -> {
            Assertions.assertEquals(7, currentTick.intValue() % 10);
            Assertions.assertEquals(cycleCount4.intValue(), currentTick.intValue() / 10);
            cycleCount4.increment();
        }, 10), 7);
        scheduler.schedule(() -> scheduler.scheduleCyclic(() -> {
            Assertions.assertEquals(1, currentTick.intValue() % 75);
            Assertions.assertEquals(cycleCount5.intValue(), currentTick.intValue() / 75);
            cycleCount5.increment();
        }, 75), 0);
        scheduler.schedule(() -> scheduler.scheduleCyclic(() -> {
            Assertions.assertEquals(1, currentTick.intValue() % 99);
            Assertions.assertEquals(cycleCount6.intValue(), currentTick.intValue() / 99);
            cycleCount6.increment();
        }, 99), 1);
        scheduler.scheduleCyclic(() -> scheduler.schedule(() -> {
            Assertions.assertEquals(6, currentTick.intValue() % 10);
            Assertions.assertEquals(cycleCount7.intValue(), currentTick.intValue() / 10);
            cycleCount7.increment();
        }, 5), 10);
        scheduler.scheduleCyclic(() -> scheduler.schedule(() -> {
            Assertions.assertEquals(11, currentTick.intValue() % 55);
            Assertions.assertEquals(cycleCount8.intValue(), currentTick.intValue() / 55);
            cycleCount8.increment();
        }, 10), 55);
        while (currentTick.intValue() < 10_000_000) {
            tick();
        }
        Assertions.assertEquals(10000001, cycleCount1.intValue());
        Assertions.assertEquals(1000000, cycleCount2.intValue());
        Assertions.assertEquals(181819, cycleCount3.intValue());
        Assertions.assertEquals(1000000, cycleCount4.intValue());
        Assertions.assertEquals(133334, cycleCount5.intValue());
        Assertions.assertEquals(101011, cycleCount6.intValue());
        Assertions.assertEquals(1000000, cycleCount7.intValue());
        Assertions.assertEquals(181818, cycleCount8.intValue());
    }

    private void tick() {
        currentTick.increment();
        scheduler.tick();
    }
}
