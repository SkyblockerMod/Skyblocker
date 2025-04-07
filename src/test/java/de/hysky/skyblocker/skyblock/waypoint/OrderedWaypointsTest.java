package de.hysky.skyblocker.skyblock.waypoint;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.OrderedNamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class OrderedWaypointsTest {
	private static final List<NamedWaypoint> RIFT_PARKOUR_WAYPOINTS = List.of(
			new OrderedNamedWaypoint(new BlockPos(46, 169, 44), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(46, 170, 47), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(47, 170, 51), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(48, 171, 54), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(47, 171, 58), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(45, 170, 62), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(41, 167, 69), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(49, 167, 79), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(51, 168, 79), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(40, 167, 88), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(39, 168, 88), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(38, 169, 88), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(44, 163, 91), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(43, 164, 91), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(42, 165, 91), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(41, 166, 91), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(40, 167, 91), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(39, 168, 91), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(38, 169, 91), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(38, 175, 96), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(51, 175, 105), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(28, 175, 108), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(32, 175, 105), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(29, 176, 104), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(42, 175, 96), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(40, 161, 116), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(40, 161, 120), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(40, 160, 123), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(34, 128, 137), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(34, 123, 143), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(34, 123, 147), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(34, 97, 166), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(35, 97, 166), "", new float[]{0, 1, 0})
	);

	private static final List<NamedWaypoint> DARK_MONOLITHS_WAYPOINTS = List.of(
			new OrderedNamedWaypoint(new BlockPos(-16, 236, -93), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(49, 202, -162), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(56, 214, -25), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(129, 187, 59), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(150, 196, 190), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(61, 204, 181), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(91, 187, 131), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(77, 160, 162), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(-9, 162, 109), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(1, 183, 25), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(1, 170, -1), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(-94, 201, -30), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(-91, 221, -53), "", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(-65, 206, -65), "", new float[]{0, 1, 0})
	);

	@BeforeAll
	public static void setup() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void testFromSkyblockerOrderedRiftParkour() {
		String waypointGroup = "[Skyblocker::OrderedWaypoints::v1]H4sIAAAAAAAA/43US2rEMAwG4LtorYXfj2x7jJJFOuNCIROHdMpQQu5e2ZlCV5UWIRA+ZH5Jzg7vc32UDYYdlulWYPj9gFCW6W0uVxju21dBeEzfa/1Y7p8wvO6w1vZ2AXXI6NyIcKlz3V7qba1LOdV44F8YFbrIwtih1yxMBDV6/uh4wsRC348OhoWaUkcMmYW5w8hC3yomAXSqV0xsGJt7RQFMfYQ8dI6gxcxPxhJ0EmgIegls7QkSeLaHh8/2CODZHhmMFCZIZk1QK89Jk56SH6IR16TgkS6j4u+NEQbqTadQWiyNEkl6jGWj01q2Rln219Il7aaT1WxSUjPHtpws9P/D8Th+AGteDz6MBQAA";
		List<WaypointGroup> waypointGroups = OrderedWaypoints.toWaypointGroups(OrderedWaypoints.SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(decode(waypointGroup))).getOrThrow().values());
		List<WaypointGroup> expected = List.of(new WaypointGroup("flower", Location.DWARVEN_MINES, RIFT_PARKOUR_WAYPOINTS, true), new WaypointGroup("flower", Location.CRYSTAL_HOLLOWS, RIFT_PARKOUR_WAYPOINTS, true));

		Assertions.assertEquals(expected, waypointGroups);
	}

	@Test
	void testFromSkyblockerOrderedDarkMonoliths() {
		String waypointGroup = "[Skyblocker::OrderedWaypoints::v1]H4sIAAAAAAAA/42SzQrCMBCE32XPG8jmr6ZXzz6B9FA1YKFNilZESt/drSKeZD2EQPgyOzPJDLuSS99NZ6hnyO2QoP4eIaTcHvp0gnq63BLCvX2MpcvTFer9DGNZd0UBjQ2oom0QjqUvl20ZxpLTm2sW/KAuotEG+YaRUM+i5FAZL5FkItKmQh9F0mukGHhpCQ3ETh3rkkRGek0nK5IVU4EdyOFVXCkkLUfi4Rb/KAmp0ly8PNlxcEJlxYoUJzeGUS++uwqeVfmLhF9Gm2V5AvVdxiCLAgAA";
		List<WaypointGroup> waypointGroups = OrderedWaypoints.toWaypointGroups(OrderedWaypoints.SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(decode(waypointGroup))).getOrThrow().values());
		List<WaypointGroup> expected = List.of(new WaypointGroup("Monolith", Location.DWARVEN_MINES, DARK_MONOLITHS_WAYPOINTS, true), new WaypointGroup("Monolith", Location.CRYSTAL_HOLLOWS, DARK_MONOLITHS_WAYPOINTS, true));

		Assertions.assertEquals(expected, waypointGroups);
	}

	private String decode(String input) {
		try {
			String encoded = input.replace(OrderedWaypoints.PREFIX, "");
			byte[] decoded = Base64.getDecoder().decode(encoded);

			return new String(new GZIPInputStream(new ByteArrayInputStream(decoded)).readAllBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
