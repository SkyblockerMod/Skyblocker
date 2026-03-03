package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.skyblock.waypoint.Waypoints;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WaypointsTest {
	private static final WaypointGroup SAPPHIRE_WAYPOINTS = new WaypointGroup("Coleweight", Location.CRYSTAL_HOLLOWS, List.of(
			new OrderedNamedWaypoint(new BlockPos(821, 137, 809), "1", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(821, 143, 809), "2", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(812, 154, 798), "3", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(817, 159, 803), "4", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(814, 168, 798), "5", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(814, 171, 809), "6", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(810, 177, 821), "7", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(803, 183, 821), "8", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(802, 178, 817), "9", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(803, 175, 811), "10", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(800, 167, 799), "11", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(787, 174, 809), "12", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(783, 177, 820), "13", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(766, 177, 822), "14", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(769, 175, 811), "15", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(775, 170, 810), "16", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(778, 161, 800), "17", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(787, 155, 792), "18", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(778, 153, 801), "19", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(789, 154, 809), "20", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(794, 159, 823), "21", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(804, 163, 816), "22", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(794, 164, 800), "23", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(801, 168, 795), "24", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(806, 161, 783), "25", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(801, 157, 778), "26", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(791, 161, 781), "27", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(796, 164, 776), "28", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(798, 167, 774), "29", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(803, 161, 764), "30", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(810, 159, 762), "31", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(817, 156, 767), "32", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(821, 149, 754), "33", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(814, 139, 742), "34", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(818, 137, 736), "35", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(818, 143, 736), "36", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(802, 140, 739), "37", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(804, 131, 730), "38", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(792, 121, 726), "39", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(788, 127, 727), "40", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(792, 127, 726), "41", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(783, 123, 731), "42", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(786, 122, 717), "43", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(785, 124, 707), "44", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(769, 129, 709), "45", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(764, 131, 716), "46", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(757, 131, 717), "47", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(755, 139, 727), "48", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(753, 134, 723), "49", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(768, 126, 723), "50", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(770, 122, 720), "51", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(777, 116, 720), "52", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(779, 113, 725), "53", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(786, 116, 742), "54", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(783, 123, 752), "55", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(778, 125, 762), "56", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(784, 131, 754), "57", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(789, 135, 760), "58", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(792, 138, 758), "59", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(802, 138, 769), "60", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(807, 142, 780), "61", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(805, 132, 775), "62", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(820, 123, 772), "63", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(813, 131, 766), "64", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(812, 127, 763), "65", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(804, 126, 753), "66", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(810, 125, 750), "67", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(821, 127, 751), "68", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(815, 124, 742), "69", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(815, 120, 732), "70", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(806, 115, 732), "71", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(796, 125, 741), "72", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(798, 119, 757), "73", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(799, 112, 763), "74", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(783, 110, 765), "75", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(804, 116, 777), "76", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(801, 116, 788), "77", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(793, 110, 798), "78", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(795, 107, 800), "79", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(805, 100, 803), "80", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(821, 105, 809), "81", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(818, 96, 816), "82", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(803, 92, 802), "83", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(793, 97, 813), "84", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(791, 94, 809), "85", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(787, 94, 810), "86", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(775, 92, 802), "87", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(771, 91, 799), "88", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(763, 89, 805), "89", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(768, 101, 806), "90", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(785, 105, 808), "91", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(791, 101, 805), "92", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(791, 103, 784), "93", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(771, 102, 778), "94", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(764, 99, 781), "95", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(758, 97, 792), "96", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(762, 93, 783), "97", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(778, 92, 775), "98", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(786, 90, 784), "99", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(790, 88, 792), "100", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(792, 82, 815), "101", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(783, 76, 811), "102", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(795, 69, 821), "103", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(800, 66, 807), "104", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(810, 65, 811), "105", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(817, 75, 813), "106", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(815, 75, 800), "107", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(822, 85, 800), "108", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(822, 85, 785), "109", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(811, 88, 778), "110", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(804, 86, 792), "111", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(792, 78, 790), "112", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(790, 77, 793), "113", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(785, 68, 800), "114", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(773, 75, 797), "115", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(773, 78, 794), "116", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(757, 83, 797), "117", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(742, 84, 791), "118", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(737, 85, 797), "119", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(731, 75, 813), "120", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(731, 81, 816), "121", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(743, 82, 821), "122", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(746, 96, 798), "123", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(742, 110, 788), "124", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(745, 110, 807), "125", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(752, 113, 805), "126", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(742, 123, 801), "127", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(736, 129, 801), "128", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(733, 138, 795), "129", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(737, 134, 792), "130", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(741, 131, 799), "131", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(743, 129, 802), "132", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(753, 134, 804), "133", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(755, 139, 808), "134", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(757, 131, 798), "135", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(772, 140, 803), "136", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(773, 144, 797), "137", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(784, 142, 797), "138", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(785, 141, 795), "139", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(793, 147, 801), "140", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(785, 137, 810), "141", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(790, 133, 800), "142", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(806, 131, 803), "143", new float[]{0, 1, 0}),
			new OrderedNamedWaypoint(new BlockPos(803, 131, 809), "144", new float[]{0, 1, 0})
	), true, true, WaypointGroup.DEFAULT_TYPE);
	private static final WaypointGroup RIFT_PARKOUR_WAYPOINTS = new WaypointGroup("flower", Location.THE_RIFT, List.of(
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
	), true, true, WaypointGroup.DEFAULT_TYPE);
	static final WaypointGroup DARK_MONOLITHS_WAYPOINTS = new WaypointGroup("Monolith", Location.DWARVEN_MINES, List.of(
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
	), true, true, WaypointGroup.DEFAULT_TYPE);

	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testFromSkytilsBase64() {
		String waypointGroupsSkytilsBase64 = "eyJjYXRlZ29yaWVzIjpbeyJuYW1lIjoiY2F0ZWdvcnkiLCJ3YXlwb2ludHMiOlt7Im5hbWUiOiJ3YXlwb2ludCIsIngiOjAsInkiOjAsInoiOjAsImVuYWJsZWQiOmZhbHNlLCJjb2xvciI6LTg3MjM4MjIwOSwiYWRkZWRBdCI6MX0seyJuYW1lIjoxLCJ4IjotMSwieSI6MCwieiI6MSwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOjAsImFkZGVkQXQiOjF9XSwiaXNsYW5kIjoiaHViIn1dfQ==";
		List<WaypointGroup> waypointGroups = Waypoints.fromSkytils(waypointGroupsSkytilsBase64, Location.UNKNOWN);
		List<WaypointGroup> expectedWaypointGroups = List.of(new WaypointGroup("category", Location.HUB, List.of(new NamedWaypoint(BlockPos.ZERO, "waypoint", new float[]{0f, 0.5019608f, 1f}, 0.8f, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "1", new float[]{0f, 0f, 0f}, true))));

		Assertions.assertEquals(expectedWaypointGroups, waypointGroups);
	}

	@Test
	void testToSkytilsBase64() {
		List<WaypointGroup> waypointGroups = List.of(new WaypointGroup("category", Location.HUB, List.of(new NamedWaypoint(BlockPos.ZERO, "waypoint", new float[]{0f, 0.5f, 1f}, 0.8f, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "1", new float[]{0f, 0f, 0f}, true))));
		String waypointGroupsSkytilsBase64 = Waypoints.toSkytilsBase64(waypointGroups);
		String expectedWaypointGroupsSkytilsBase64 = "eyJjYXRlZ29yaWVzIjpbeyJuYW1lIjoiY2F0ZWdvcnkiLCJpc2xhbmQiOiJodWIiLCJ3YXlwb2ludHMiOlt7IngiOjAsInkiOjAsInoiOjAsIm5hbWUiOiJ3YXlwb2ludCIsImNvbG9yIjotODcyMzgyNDY1LCJlbmFibGVkIjpmYWxzZX0seyJ4IjotMSwieSI6MCwieiI6MSwibmFtZSI6IjEiLCJjb2xvciI6MjEzMDcwNjQzMiwiZW5hYmxlZCI6dHJ1ZX1dfV19";

		Assertions.assertEquals(expectedWaypointGroupsSkytilsBase64, waypointGroupsSkytilsBase64);
	}

	//https://sharetext.me/gq22cbhdmo
	@Test
	void testFromSkytilsBase64GlacialCaveWaypoints() {
		String waypointGroupsSkytilsBase64 = "eyJjYXRlZ29yaWVzIjogW3sibmFtZSI6ICJGcm96ZW4gVHJlYXN1cmUgTG9jYXRpb25zIiwid2F5cG9pbnRzIjogW3sibmFtZSI6ICIyNCIsIngiOiA2NCwieSI6IDc4LCJ6IjogMjgsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwODU0MDY3MTksImFkZGVkQXQiOiAxNjY5OTk5NzUwNjc3fSx7Im5hbWUiOiAiOSIsIngiOiA0NSwieSI6IDc5LCJ6IjogNDksImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwODUyNzM1OTksImFkZGVkQXQiOiAxNjY5OTk5NTEwMTA3fSx7Im5hbWUiOiAiMjAiLCJ4IjogNjAsInkiOiA3NiwieiI6IDUxLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiA5NTMzNTE5MzUsImFkZGVkQXQiOiAxNjY5OTk5NzQ5MzI3fSx7Im5hbWUiOiAiMjMiLCJ4IjogNjMsInkiOiA3NiwieiI6IDk1LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDIwNDYxMDUyLCJhZGRlZEF0IjogMTY2OTk5OTc1MDQ3N30seyJuYW1lIjogIjIyIiwieCI6IDYzLCJ5IjogNzYsInoiOiA1MiwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA1MjQ0MjYxMSwiYWRkZWRBdCI6IDE2Njk5OTk3NTAyMjd9LHsibmFtZSI6ICI0MCIsIngiOiA5NCwieSI6IDc3LCJ6IjogNDIsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDk4NDYxMjg1NywiYWRkZWRBdCI6IDE2NzAwMDAyMjcwMjR9LHsibmFtZSI6ICIzOCIsIngiOiA5MSwieSI6IDc3LCJ6IjogMjcsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTI3NzAyOTIsImFkZGVkQXQiOiAxNjcwMDAwMjI2NjI1fSx7Im5hbWUiOiAiMTUiLCJ4IjogNTAsInkiOiA4MCwieiI6IDg4LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDcxMjUxMTk5LCJhZGRlZEF0IjogMTY2OTk5OTUxMTUwNH0seyJuYW1lIjogIjE0IiwieCI6IDUwLCJ5IjogNzksInoiOiAzNCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTEwNDkzNjcwMywiYWRkZWRBdCI6IDE2Njk5OTk1MTEzMDZ9LHsibmFtZSI6ICIxOSIsIngiOiA1OCwieSI6IDc5LCJ6IjogODksImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDExNTU2NjE4MTgsImFkZGVkQXQiOiAxNjY5OTk5NTE3ODEwfSx7Im5hbWUiOiAiMzAiLCJ4IjogNzgsInkiOiA3NCwieiI6IDk5LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTE5NDIwNDAzLCJhZGRlZEF0IjogMTY3MDAwMDAyMTgyM30seyJuYW1lIjogIjExIiwieCI6IDQ2LCJ5IjogODAsInoiOiA4NCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA3MTY0NDY2MiwiYWRkZWRBdCI6IDE2Njk5OTk1MTA3MDh9LHsibmFtZSI6ICI0MyIsIngiOiA5NywieSI6IDgxLCJ6IjogNzcsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTE5ODM4NjUsImFkZGVkQXQiOiAxNjcwMDAwMjI3Njc2fSx7Im5hbWUiOiAiMTciLCJ4IjogNTUsInkiOiA3OSwieiI6IDM0LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTA1MTk5MDk4LCJhZGRlZEF0IjogMTY2OTk5OTUxMTkwNX0seyJuYW1lIjogIjQiLCJ4IjogMzksInkiOiA4MCwieiI6IDczLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTUzMjM2NDc5LCJhZGRlZEF0IjogMTY2OTk5OTE5ODkyN30seyJuYW1lIjogIjQxIiwieCI6IDk1LCJ5IjogNzYsInoiOiA1OCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTE1MTk5MTgwMSwiYWRkZWRBdCI6IDE2NzAwMDAyMjcyMjV9LHsibmFtZSI6ICI0MiIsIngiOiA5NywieSI6IDc1LCJ6IjogNzAsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTE3MjE3MDYsImFkZGVkQXQiOiAxNjcwMDAwMjI3NDczfSx7Im5hbWUiOiAiMTAiLCJ4IjogNDUsInkiOiA3OSwieiI6IDcwLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDcyMzY1NTYxLCJhZGRlZEF0IjogMTY2OTk5OTUxMDUwOH0seyJuYW1lIjogIjI4IiwieCI6IDc1LCJ5IjogODIsInoiOiAyMCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA1MjkwMTM1OSwiYWRkZWRBdCI6IDE2Njk5OTk5ODY0MjZ9LHsibmFtZSI6ICIzIiwieCI6IDM2LCJ5IjogODAsInoiOiA4MCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogOTUyNjI5NTAzLCJhZGRlZEF0IjogMTY2OTk5OTE5ODcyN30seyJuYW1lIjogIjciLCJ4IjogNDMsInkiOiA3NywieiI6IDUwLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTIwNTM0MjcxLCJhZGRlZEF0IjogMTY2OTk5OTE5OTQ2N30seyJuYW1lIjogIjgiLCJ4IjogNDMsInkiOiA3OSwieiI6IDczLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTUyNzEwMzk5LCJhZGRlZEF0IjogMTY2OTk5OTMxMTAyOX0seyJuYW1lIjogIjIiLCJ4IjogMzUsInkiOiA4LCJ6IjogNzEsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTQzNDMxNjQsImFkZGVkQXQiOiAxNjY5OTk5MTk4NTY3fSx7Im5hbWUiOiAiMzQiLCJ4IjogODksInkiOiA3NywieiI6IDg0LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDg2NDU1Nzk3LCJhZGRlZEF0IjogMTY3MDAwMDAyMjUyOX0seyJuYW1lIjogIjI2IiwieCI6IDczLCJ5IjogNzYsInoiOiAzMSwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTExOTE1Nzc1OSwiYWRkZWRBdCI6IDE2Njk5OTk3NTEwNzd9LHsibmFtZSI6ICIxMiIsIngiOiA0NywieSI6IDc3LCJ6IjogNjUsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNzE4NDEyNzcsImFkZGVkQXQiOiAxNjY5OTk5NTEwOTA4fSx7Im5hbWUiOiAiMTYiLCJ4IjogNTIsInkiOiA3NSwieiI6IDQ1LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTM4NjIyNDU3LCJhZGRlZEF0IjogMTY2OTk5OTUxMTcwM30seyJuYW1lIjogIjMzIiwieCI6IDgyLCJ5IjogNzgsInoiOiAyNiwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTEwMzAzNDM2NywiYWRkZWRBdCI6IDE2NzAwMDAwMjIzNzl9LHsibmFtZSI6ICIyMSIsIngiOiA2MSwieSI6IDc4LCJ6IjogOTIsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwMjExODE5NDIsImFkZGVkQXQiOiAxNjY5OTk5NzQ5ODc3fSx7Im5hbWUiOiAiMjciLCJ4IjogNzMsInkiOiA3OSwieiI6IDUyLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDUxMDAwODI0LCJhZGRlZEF0IjogMTY2OTk5OTk4NjIzMH0seyJuYW1lIjogIjQ2IiwieCI6IDEwMywieSI6IDc0LCJ6IjogOTgsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDExMTg1Njg0MjUsImFkZGVkQXQiOiAxNjcwMDAwMjI4MjIzfSx7Im5hbWUiOiAiNDciLCJ4IjogMTA0LCJ5IjogNzgsInoiOiA2OCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogOTUzNDc5OTM1LCJhZGRlZEF0IjogMTY3MDAwMDM1Nzk3NH0seyJuYW1lIjogIjYiLCJ4IjogNDIsInkiOiA3NywieiI6IDU4LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDE4NDk0OTcwLCJhZGRlZEF0IjogMTY2OTk5OTE5OTMyNX0seyJuYW1lIjogIjUiLCJ4IjogNDEsInkiOiA3OSwieiI6IDgxLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTM2MzI4MTkxLCJhZGRlZEF0IjogMTY2OTk5OTE5OTEyMX0seyJuYW1lIjogIjM2IiwieCI6IDkwLCJ5IjogNzcsInoiOiA0NiwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTExOTA5MDQzMSwiYWRkZWRBdCI6IDE2NzAwMDAwMjI5Mjh9LHsibmFtZSI6ICIxIiwieCI6IDMyLCJ5IjogODAsInoiOiA3NCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTM4ODc3MzM3MSwiYWRkZWRBdCI6IDE2Njk5OTkxMDI4ODJ9LHsibmFtZSI6ICIzMSIsIngiOiA3OCwieSI6IDc3LCJ6IjogNDAsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwMTkyMTU4NjAsImFkZGVkQXQiOiAxNjcwMDAwMDIxOTc1fSx7Im5hbWUiOiAiMjkiLCJ4IjogNzYsInkiOiA3NiwieiI6IDU1LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTUzNTY0NjU1LCJhZGRlZEF0IjogMTY2OTk5OTk4NjYyN30seyJuYW1lIjogIjI1IiwieCI6IDY2LCJ5IjogODEsInoiOiAyOCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA1MjcwNDc1MCwiYWRkZWRBdCI6IDE2Njk5OTk3NTA5Mjd9LHsibmFtZSI6ICIzNSIsIngiOiA5MCwieSI6IDc3LCJ6IjogMzgsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwMTkzNDY5MzMsImFkZGVkQXQiOiAxNjcwMDAwMDIyNzI0fSx7Im5hbWUiOiAiMTgiLCJ4IjogNTUsInkiOiA4MCwieiI6IDM4LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDg4NjE4NDkzLCJhZGRlZEF0IjogMTY2OTk5OTUxMjE1N30seyJuYW1lIjogIjM5IiwieCI6IDkyLCJ5IjogNzQsInoiOiAxMDgsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwODY4NDkwMTQsImFkZGVkQXQiOiAxNjcwMDAwMjI2ODc5fSx7Im5hbWUiOiAiMTMiLCJ4IjogNTAsInkiOiA3NiwieiI6IDUyLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDcxOTcyMzQ0LCJhZGRlZEF0IjogMTY2OTk5OTUxMTEwMn0seyJuYW1lIjogIjQ0IiwieCI6IDk4LCJ5IjogNzcsInoiOiA3NiwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA4NTg2NTk3OCwiYWRkZWRBdCI6IDE2NzAwMDAyMjc4ODF9LHsibmFtZSI6ICIzMiIsIngiOiA3OSwieSI6IDgwLCJ6IjogNzMsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTI3NzAyOTgsImFkZGVkQXQiOiAxNjcwMDAwMDIyMTc0fSx7Im5hbWUiOiAiMzciLCJ4IjogOTEsInkiOiA3NiwieiI6IDM4LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDUxMTMxMzkxLCJhZGRlZEF0IjogMTY3MDAwMDIyNjQyM30seyJuYW1lIjogIjQ1IiwieCI6IDk4LCJ5IjogNzgsInoiOiA3NSwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTIwMjg0NzczNywiYWRkZWRBdCI6IDE2NzAwMDAyMjgwNzN9XSwiaXNsYW5kIjogIndpbnRlciJ9XX0=";
		List<WaypointGroup> waypointGroups = Waypoints.fromSkytils(waypointGroupsSkytilsBase64, Location.UNKNOWN);
		List<WaypointGroup> expectedWaypointGroups = List.of(new WaypointGroup("Frozen Treasure Locations", Location.WINTER_ISLAND, List.of(
				new NamedWaypoint(new BlockPos(64, 78, 28), "24", new float[]{177 / 255f, 253 / 255f, 255 / 255f}, 64 / 255f, true),
				new NamedWaypoint(new BlockPos(45, 79, 49), "9", new float[]{175 / 255f, 245 / 255f, 255 / 255f}, 64 / 255f, true),
				new NamedWaypoint(new BlockPos(60, 76, 51), "20", new float[]{210 / 255f, 254 / 255f, 255 / 255f}, 56 / 255f, true),
				new NamedWaypoint(new BlockPos(63, 76, 95), "23", new float[]{210 / 255f, 255 / 255f, 252 / 255f}, 60 / 255f, true),
				new NamedWaypoint(new BlockPos(63, 76, 52), "22", new float[]{186 / 255f, 255 / 255f, 243 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(94, 77, 42), "40", new float[]{175 / 255f, 255 / 255f, 249 / 255f}, 58 / 255f, true),
				new NamedWaypoint(new BlockPos(91, 77, 27), "38", new float[]{191 / 255f, 255 / 255f, 244 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(50, 80, 88), "15", new float[]{217 / 255f, 254 / 255f, 255 / 255f}, 63 / 255f, true),
				new NamedWaypoint(new BlockPos(50, 79, 34), "14", new float[]{219 / 255f, 254 / 255f, 255 / 255f}, 65 / 255f, true),
				new NamedWaypoint(new BlockPos(58, 79, 89), "19", new float[]{225 / 255f, 255 / 255f, 250 / 255f}, 68 / 255f, true),
				new NamedWaypoint(new BlockPos(78, 74, 99), "30", new float[]{184 / 255f, 255 / 255f, 243 / 255f}, 66 / 255f, true),
				new NamedWaypoint(new BlockPos(46, 80, 84), "11", new float[]{223 / 255f, 255 / 255f, 246 / 255f}, 63 / 255f, true),
				new NamedWaypoint(new BlockPos(97, 81, 77), "43", new float[]{179 / 255f, 255 / 255f, 249 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(55, 79, 34), "17", new float[]{223 / 255f, 255 / 255f, 250 / 255f}, 65 / 255f, true),
				new NamedWaypoint(new BlockPos(39, 80, 73), "4", new float[]{188 / 255f, 253 / 255f, 255 / 255f}, 68 / 255f, true),
				new NamedWaypoint(new BlockPos(95, 76, 58), "41", new float[]{169 / 255f, 255 / 255f, 249 / 255f}, 68 / 255f, true),
				new NamedWaypoint(new BlockPos(97, 75, 70), "42", new float[]{175 / 255f, 255 / 255f, 234 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(45, 79, 70), "10", new float[]{234 / 255f, 255 / 255f, 249 / 255f}, 63 / 255f, true),
				new NamedWaypoint(new BlockPos(75, 82, 20), "28", new float[]{193 / 255f, 255 / 255f, 239 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(36, 80, 80), "3", new float[]{199 / 255f, 248 / 255f, 255 / 255f}, 56 / 255f, true),
				new NamedWaypoint(new BlockPos(43, 77, 50), "7", new float[]{201 / 255f, 254 / 255f, 255 / 255f}, 66 / 255f, true),
				new NamedWaypoint(new BlockPos(43, 79, 73), "8", new float[]{180 / 255f, 246 / 255f, 255 / 255f}, 68 / 255f, true),
				new NamedWaypoint(new BlockPos(35, 8, 71), "2", new float[]{215 / 255f, 255 / 255f, 252 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(89, 77, 84), "34", new float[]{193 / 255f, 255 / 255f, 245 / 255f}, 64 / 255f, true),
				new NamedWaypoint(new BlockPos(73, 76, 31), "26", new float[]{180 / 255f, 253 / 255f, 255 / 255f}, 66 / 255f, true),
				new NamedWaypoint(new BlockPos(47, 77, 65), "12", new float[]{226 / 255f, 255 / 255f, 253 / 255f}, 63 / 255f, true),
				new NamedWaypoint(new BlockPos(52, 75, 45), "16", new float[]{221 / 255f, 255 / 255f, 249 / 255f}, 67 / 255f, true),
				new NamedWaypoint(new BlockPos(82, 78, 26), "33", new float[]{190 / 255f, 247 / 255f, 255 / 255f}, 65 / 255f, true),
				new NamedWaypoint(new BlockPos(61, 78, 92), "21", new float[]{221 / 255f, 255 / 255f, 246 / 255f}, 60 / 255f, true),
				new NamedWaypoint(new BlockPos(73, 79, 52), "27", new float[]{164 / 255f, 255 / 255f, 248 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(103, 74, 98), "46", new float[]{171 / 255f, 255 / 255f, 233 / 255f}, 66 / 255f, true),
				new NamedWaypoint(new BlockPos(104, 78, 68), "47", new float[]{212 / 255f, 242 / 255f, 255 / 255f}, 56 / 255f, true),
				new NamedWaypoint(new BlockPos(42, 77, 58), "6", new float[]{180 / 255f, 255 / 255f, 250 / 255f}, 60 / 255f, true),
				new NamedWaypoint(new BlockPos(41, 79, 81), "5", new float[]{186 / 255f, 253 / 255f, 255 / 255f}, 67 / 255f, true),
				new NamedWaypoint(new BlockPos(90, 77, 46), "36", new float[]{179 / 255f, 246 / 255f, 255 / 255f}, 66 / 255f, true),
				new NamedWaypoint(new BlockPos(32, 80, 74), "1", new float[]{198 / 255f, 255 / 255f, 251 / 255f}, 82 / 255f, true),
				new NamedWaypoint(new BlockPos(78, 77, 40), "31", new float[]{191 / 255f, 255 / 255f, 244 / 255f}, 60 / 255f, true),
				new NamedWaypoint(new BlockPos(76, 76, 55), "29", new float[]{193 / 255f, 255 / 255f, 239 / 255f}, 68 / 255f, true),
				new NamedWaypoint(new BlockPos(66, 81, 28), "25", new float[]{190 / 255f, 255 / 255f, 238 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(90, 77, 38), "35", new float[]{193 / 255f, 255 / 255f, 245 / 255f}, 60 / 255f, true),
				new NamedWaypoint(new BlockPos(55, 80, 38), "18", new float[]{226 / 255f, 255 / 255f, 253 / 255f}, 64 / 255f, true),
				new NamedWaypoint(new BlockPos(92, 74, 108), "39", new float[]{199 / 255f, 255 / 255f, 246 / 255f}, 64 / 255f, true),
				new NamedWaypoint(new BlockPos(50, 76, 52), "13", new float[]{228 / 255f, 255 / 255f, 248 / 255f}, 63 / 255f, true),
				new NamedWaypoint(new BlockPos(98, 77, 76), "44", new float[]{184 / 255f, 255 / 255f, 250 / 255f}, 64 / 255f, true),
				new NamedWaypoint(new BlockPos(79, 80, 73), "32", new float[]{191 / 255f, 255 / 255f, 250 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(91, 76, 38), "37", new float[]{166 / 255f, 253 / 255f, 255 / 255f}, 62 / 255f, true),
				new NamedWaypoint(new BlockPos(98, 78, 75), "45", new float[]{177 / 255f, 255 / 255f, 249 / 255f}, 71 / 255f, true)
		)));

		Assertions.assertEquals(expectedWaypointGroups, waypointGroups);
	}

	//https://pastebin.com/c4PjUZjJ
	@Test
	void testFromSkytilsBase64CrystalHollowsWaypoints() {
		String waypointsSkytilsBase64 = "W3sibmFtZSI6IlNob3V0b3V0IFRlYmV5IGFuZCB0cmV2YW55YSIsIngiOjUxMCwieSI6NTMsInoiOjM5MywiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMDciLCJ4Ijo0NzksInkiOjM5LCJ6Ijo0MDgsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjExIiwieCI6NDk1LCJ5IjozNCwieiI6NDE4LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIwOSIsIngiOjQ4NSwieSI6MzMsInoiOjQwMiwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMjMiLCJ4Ijo1MTQsInkiOjU1LCJ6IjozODMsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjA2IiwieCI6NDgzLCJ5Ijo0MiwieiI6NDA1LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIwMSIsIngiOjUwMiwieSI6NDgsInoiOjQwMywiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMTgiLCJ4Ijo1MDMsInkiOjU2LCJ6Ijo0MzYsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjAwIC0gU3RhcnQiLCJ4Ijo1MDMsInkiOjQ4LCJ6Ijo0MDAsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjEzIiwieCI6NDc4LCJ5Ijo0NCwieiI6NDE5LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIxOSIsIngiOjUwMSwieSI6NTcsInoiOjQzOCwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMDIiLCJ4Ijo0OTYsInkiOjQ1LCJ6Ijo0MDcsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjA0IiwieCI6NDk1LCJ5Ijo1MywieiI6NDA0LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIwNSIsIngiOjQ3OSwieSI6NDksInoiOjQwNywiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMTIiLCJ4Ijo1MDQsInkiOjQxLCJ6Ijo0MTksImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjAzIiwieCI6NDkwLCJ5Ijo0NSwieiI6MzkyLCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIxMCIsIngiOjQ4OCwieSI6MzIsInoiOjQyMSwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMjIiLCJ4Ijo1MDcsInkiOjUyLCJ6IjozODYsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjE2IiwieCI6NDg4LCJ5Ijo1NSwieiI6NDIxLCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiI5OSAtIEVuZCIsIngiOjUxMCwieSI6NTIsInoiOjM5MywiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMTUiLCJ4Ijo0ODYsInkiOjU1LCJ6Ijo0MjgsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjE0IiwieCI6NDc1LCJ5Ijo0NCwieiI6NDI5LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIxNyIsIngiOjUwOSwieSI6NTAsInoiOjQzMiwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMjAiLCJ4Ijo1MDUsInkiOjU4LCJ6Ijo0MjYsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjA4IiwieCI6NDgwLCJ5IjozOCwieiI6NDA1LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIyMSIsIngiOjQ5NywieSI6NTUsInoiOjM5MSwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn1d";
		List<WaypointGroup> waypointGroups = Waypoints.fromSkytils(waypointsSkytilsBase64, Location.CRYSTAL_HOLLOWS);
		List<WaypointGroup> expectedWaypointGroups = List.of(new WaypointGroup("New Group", Location.CRYSTAL_HOLLOWS, List.of(
				new NamedWaypoint(new BlockPos(510, 53, 393), "Shoutout Tebey and trevanya", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(479, 39, 408), "07", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(495, 34, 418), "11", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(485, 33, 402), "09", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(514, 55, 383), "23", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(483, 42, 405), "06", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(502, 48, 403), "01", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(503, 56, 436), "18", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(503, 48, 400), "00 - Start", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(478, 44, 419), "13", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(501, 57, 438), "19", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(496, 45, 407), "02", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(495, 53, 404), "04", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(479, 49, 407), "05", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(504, 41, 419), "12", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(490, 45, 392), "03", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(488, 32, 421), "10", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(507, 52, 386), "22", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(488, 55, 421), "16", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(510, 52, 393), "99 - End", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(486, 55, 428), "15", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(475, 44, 429), "14", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(509, 50, 432), "17", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(505, 58, 426), "20", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(480, 38, 405), "08", new float[]{1, 0, 0}, 1, true),
				new NamedWaypoint(new BlockPos(497, 55, 391), "21", new float[]{1, 0, 0}, 1, true)
		)));

		Assertions.assertEquals(expectedWaypointGroups, waypointGroups);
	}

	@Test
	void testFromSkytilsV1Gzip() {
		String waypointsSkytilsV1Gzip = "<Skytils-Waypoint-Data>(V1):H4sIAAAAAAAC/51STWuEMBD9KyHntJjJl7u3UugfsIdC2UOq6SKk0cZIaxf/e+OC4moL4i2TmXnvzby54FwHc658aRp8RK8X7PSHiS+ctXVtO5Tpd4OyugoNJvhLd3VVurAs/Wy1NzH/HaM7yinBXXwpSfDP8HNICDZOv1lTxDD41hCsi8IUDyHGVCp+4JLGPi57MoMN2gf07EvtznaCl+kCnQJsgGeRgfM5/BJ4UHkFVhNwukm34sD6E8FlY7UbKnHpmqBdHtvmhE+tsejRWLtpqaM8BPfJKDGlS4mMrSXmla38NSu4lDDYsVItEg5ws46XyT8QKxa5j4UBCEhuPN1xKluYIBVUsZt55oujbJxJTObu3BxPmAL51yGhkU2tL4nuJBMCgP57XKf+FzPhnyPAAwAA";
		List<WaypointGroup> waypointGroups = Waypoints.fromSkytils(waypointsSkytilsV1Gzip, Location.UNKNOWN);
		List<WaypointGroup> expectedWaypointGroups = List.of(new WaypointGroup("Supply Safe Spots", Location.UNKNOWN, List.of(
				new NamedWaypoint(new BlockPos(-141, 76, -90), "Square", new float[]{0, 1, 0}, 128 / 255f, true),
				new NamedWaypoint(new BlockPos(-68, 76, -122), "Start Triangle", new float[]{0, 1, 0}, 128 / 255f, true),
				new NamedWaypoint(new BlockPos(-90, 77, -128), "Triangle", new float[]{0, 1, 0}, 128 / 255f, true)
		)), new WaypointGroup("Fuel Cell Safe Spots", Location.UNKNOWN, List.of(
				new NamedWaypoint(new BlockPos(-81, 77, -133), "Triangle 2.0", new float[]{20 / 255f, 0, 1}, 1, true),
				new NamedWaypoint(new BlockPos(-125, 77, -136), "X", new float[]{20 / 255f, 0, 1}, 1, true),
				new NamedWaypoint(new BlockPos(-141, 76, -90), "Square", new float[]{20 / 255f, 0, 1}, 1, true),
				new NamedWaypoint(new BlockPos(-135, 75, -123), "X 2.0", new float[]{20 / 255f, 0, 1}, 1, true),
				new NamedWaypoint(new BlockPos(-70, 77, -121), "Triangle ", new float[]{20 / 255f, 0, 1}, 1, true)
		)));

		Assertions.assertEquals(expectedWaypointGroups, waypointGroups);
	}

	@Test
	void testFromColeweightJson() {
		String coleweightJson = "[{\"x\":64,\"y\":78,\"z\":28,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"24\"}},{\"x\":45,\"y\":79,\"z\":49,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"9\"}},{\"x\":60,\"y\":76,\"z\":51,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"20\"}},{\"x\":63,\"y\":76,\"z\":95,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"23\"}},{\"x\":63,\"y\":76,\"z\":52,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"22\"}},{\"x\":94,\"y\":77,\"z\":42,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"40\"}},{\"x\":91,\"y\":77,\"z\":27,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"38\"}},{\"x\":50,\"y\":80,\"z\":88,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"15\"}},{\"x\":50,\"y\":79,\"z\":34,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"14\"}},{\"x\":58,\"y\":79,\"z\":89,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"19\"}},{\"x\":78,\"y\":74,\"z\":99,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"30\"}},{\"x\":46,\"y\":80,\"z\":84,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"11\"}},{\"x\":97,\"y\":81,\"z\":77,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"43\"}},{\"x\":55,\"y\":79,\"z\":34,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"17\"}},{\"x\":39,\"y\":80,\"z\":73,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"4\"}},{\"x\":95,\"y\":76,\"z\":58,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"41\"}},{\"x\":97,\"y\":75,\"z\":70,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"42\"}},{\"x\":45,\"y\":79,\"z\":70,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"10\"}},{\"x\":75,\"y\":82,\"z\":20,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"28\"}},{\"x\":36,\"y\":80,\"z\":80,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"3\"}},{\"x\":43,\"y\":77,\"z\":50,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"7\"}},{\"x\":43,\"y\":79,\"z\":73,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"8\"}},{\"x\":35,\"y\":8,\"z\":71,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"2\"}},{\"x\":89,\"y\":77,\"z\":84,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"34\"}},{\"x\":73,\"y\":76,\"z\":31,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"26\"}},{\"x\":47,\"y\":77,\"z\":65,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"12\"}},{\"x\":52,\"y\":75,\"z\":45,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"16\"}},{\"x\":82,\"y\":78,\"z\":26,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"33\"}},{\"x\":61,\"y\":78,\"z\":92,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"21\"}},{\"x\":73,\"y\":79,\"z\":52,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"27\"}},{\"x\":103,\"y\":74,\"z\":98,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"46\"}},{\"x\":104,\"y\":78,\"z\":68,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"47\"}},{\"x\":42,\"y\":77,\"z\":58,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"6\"}},{\"x\":41,\"y\":79,\"z\":81,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"5\"}},{\"x\":90,\"y\":77,\"z\":46,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"36\"}},{\"x\":32,\"y\":80,\"z\":74,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"1\"}},{\"x\":78,\"y\":77,\"z\":40,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"31\"}},{\"x\":76,\"y\":76,\"z\":55,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"29\"}},{\"x\":66,\"y\":81,\"z\":28,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"25\"}},{\"x\":90,\"y\":77,\"z\":38,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"35\"}},{\"x\":55,\"y\":80,\"z\":38,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"18\"}},{\"x\":92,\"y\":74,\"z\":108,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"39\"}},{\"x\":50,\"y\":76,\"z\":52,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"13\"}},{\"x\":98,\"y\":77,\"z\":76,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"44\"}},{\"x\":79,\"y\":80,\"z\":73,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"32\"}},{\"x\":91,\"y\":76,\"z\":38,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"37\"}},{\"x\":98,\"y\":78,\"z\":75,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"45\"}}]";
		WaypointGroup waypointGroup = Waypoints.fromColeweightJson(coleweightJson, Location.WINTER_ISLAND);
		WaypointGroup expectedWaypointGroup = new WaypointGroup("Coleweight", Location.WINTER_ISLAND, List.of(
				new OrderedNamedWaypoint(new BlockPos(64, 78, 28), "24", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(45, 79, 49), "9", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(60, 76, 51), "20", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(63, 76, 95), "23", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(63, 76, 52), "22", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(94, 77, 42), "40", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(91, 77, 27), "38", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(50, 80, 88), "15", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(50, 79, 34), "14", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(58, 79, 89), "19", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(78, 74, 99), "30", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(46, 80, 84), "11", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(97, 81, 77), "43", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(55, 79, 34), "17", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(39, 80, 73), "4", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(95, 76, 58), "41", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(97, 75, 70), "42", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(45, 79, 70), "10", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(75, 82, 20), "28", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(36, 80, 80), "3", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(43, 77, 50), "7", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(43, 79, 73), "8", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(35, 8, 71), "2", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(89, 77, 84), "34", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(73, 76, 31), "26", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(47, 77, 65), "12", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(52, 75, 45), "16", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(82, 78, 26), "33", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(61, 78, 92), "21", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(73, 79, 52), "27", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(103, 74, 98), "46", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(104, 78, 68), "47", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(42, 77, 58), "6", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(41, 79, 81), "5", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(90, 77, 46), "36", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(32, 80, 74), "1", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(78, 77, 40), "31", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(76, 76, 55), "29", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(66, 81, 28), "25", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(90, 77, 38), "35", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(55, 80, 38), "18", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(92, 74, 108), "39", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(50, 76, 52), "13", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(98, 77, 76), "44", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(79, 80, 73), "32", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(91, 76, 38), "37", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(98, 78, 75), "45", new float[]{0, 1, 0})
		), true, true, WaypointGroup.DEFAULT_TYPE);

		Assertions.assertEquals(expectedWaypointGroup, waypointGroup);
	}

	@Test
	void testFromColeweightJsonSapphire() {
		String coleweightJson = "[{\"x\":821,\"y\":137,\"z\":809,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"1\"}},{\"x\":821,\"y\":143,\"z\":809,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"2\"}},{\"x\":812,\"y\":154,\"z\":798,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"3\"}},{\"x\":817,\"y\":159,\"z\":803,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"4\"}},{\"x\":814,\"y\":168,\"z\":798,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"5\"}},{\"x\":814,\"y\":171,\"z\":809,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"6\"}},{\"x\":810,\"y\":177,\"z\":821,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"7\"}},{\"x\":803,\"y\":183,\"z\":821,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"8\"}},{\"x\":802,\"y\":178,\"z\":817,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"9\"}},{\"x\":803,\"y\":175,\"z\":811,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"10\"}},{\"x\":800,\"y\":167,\"z\":799,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"11\"}},{\"x\":787,\"y\":174,\"z\":809,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"12\"}},{\"x\":783,\"y\":177,\"z\":820,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"13\"}},{\"x\":766,\"y\":177,\"z\":822,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"14\"}},{\"x\":769,\"y\":175,\"z\":811,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"15\"}},{\"x\":775,\"y\":170,\"z\":810,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"16\"}},{\"x\":778,\"y\":161,\"z\":800,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"17\"}},{\"x\":787,\"y\":155,\"z\":792,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"18\"}},{\"x\":778,\"y\":153,\"z\":801,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"19\"}},{\"x\":789,\"y\":154,\"z\":809,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"20\"}},{\"x\":794,\"y\":159,\"z\":823,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"21\"}},{\"x\":804,\"y\":163,\"z\":816,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"22\"}},{\"x\":794,\"y\":164,\"z\":800,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"23\"}},{\"x\":801,\"y\":168,\"z\":795,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"24\"}},{\"x\":806,\"y\":161,\"z\":783,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"25\"}},{\"x\":801,\"y\":157,\"z\":778,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"26\"}},{\"x\":791,\"y\":161,\"z\":781,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"27\"}},{\"x\":796,\"y\":164,\"z\":776,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"28\"}},{\"x\":798,\"y\":167,\"z\":774,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"29\"}},{\"x\":803,\"y\":161,\"z\":764,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"30\"}},{\"x\":810,\"y\":159,\"z\":762,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"31\"}},{\"x\":817,\"y\":156,\"z\":767,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"32\"}},{\"x\":821,\"y\":149,\"z\":754,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"33\"}},{\"x\":814,\"y\":139,\"z\":742,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"34\"}},{\"x\":818,\"y\":137,\"z\":736,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"35\"}},{\"x\":818,\"y\":143,\"z\":736,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"36\"}},{\"x\":802,\"y\":140,\"z\":739,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"37\"}},{\"x\":804,\"y\":131,\"z\":730,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"38\"}},{\"x\":792,\"y\":121,\"z\":726,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"39\"}},{\"x\":788,\"y\":127,\"z\":727,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"40\"}},{\"x\":792,\"y\":127,\"z\":726,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"41\"}},{\"x\":783,\"y\":123,\"z\":731,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"42\"}},{\"x\":786,\"y\":122,\"z\":717,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"43\"}},{\"x\":785,\"y\":124,\"z\":707,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"44\"}},{\"x\":769,\"y\":129,\"z\":709,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"45\"}},{\"x\":764,\"y\":131,\"z\":716,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"46\"}},{\"x\":757,\"y\":131,\"z\":717,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"47\"}},{\"x\":755,\"y\":139,\"z\":727,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"48\"}},{\"x\":753,\"y\":134,\"z\":723,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"49\"}},{\"x\":768,\"y\":126,\"z\":723,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"50\"}},{\"x\":770,\"y\":122,\"z\":720,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"51\"}},{\"x\":777,\"y\":116,\"z\":720,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"52\"}},{\"x\":779,\"y\":113,\"z\":725,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"53\"}},{\"x\":786,\"y\":116,\"z\":742,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"54\"}},{\"x\":783,\"y\":123,\"z\":752,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"55\"}},{\"x\":778,\"y\":125,\"z\":762,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"56\"}},{\"x\":784,\"y\":131,\"z\":754,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"57\"}},{\"x\":789,\"y\":135,\"z\":760,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"58\"}},{\"x\":792,\"y\":138,\"z\":758,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"59\"}},{\"x\":802,\"y\":138,\"z\":769,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"60\"}},{\"x\":807,\"y\":142,\"z\":780,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"61\"}},{\"x\":805,\"y\":132,\"z\":775,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"62\"}},{\"x\":820,\"y\":123,\"z\":772,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"63\"}},{\"x\":813,\"y\":131,\"z\":766,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"64\"}},{\"x\":812,\"y\":127,\"z\":763,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"65\"}},{\"x\":804,\"y\":126,\"z\":753,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"66\"}},{\"x\":810,\"y\":125,\"z\":750,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"67\"}},{\"x\":821,\"y\":127,\"z\":751,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"68\"}},{\"x\":815,\"y\":124,\"z\":742,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"69\"}},{\"x\":815,\"y\":120,\"z\":732,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"70\"}},{\"x\":806,\"y\":115,\"z\":732,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"71\"}},{\"x\":796,\"y\":125,\"z\":741,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"72\"}},{\"x\":798,\"y\":119,\"z\":757,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"73\"}},{\"x\":799,\"y\":112,\"z\":763,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"74\"}},{\"x\":783,\"y\":110,\"z\":765,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"75\"}},{\"x\":804,\"y\":116,\"z\":777,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"76\"}},{\"x\":801,\"y\":116,\"z\":788,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"77\"}},{\"x\":793,\"y\":110,\"z\":798,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"78\"}},{\"x\":795,\"y\":107,\"z\":800,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"79\"}},{\"x\":805,\"y\":100,\"z\":803,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"80\"}},{\"x\":821,\"y\":105,\"z\":809,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"81\"}},{\"x\":818,\"y\":96,\"z\":816,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"82\"}},{\"x\":803,\"y\":92,\"z\":802,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"83\"}},{\"x\":793,\"y\":97,\"z\":813,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"84\"}},{\"x\":791,\"y\":94,\"z\":809,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"85\"}},{\"x\":787,\"y\":94,\"z\":810,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"86\"}},{\"x\":775,\"y\":92,\"z\":802,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"87\"}},{\"x\":771,\"y\":91,\"z\":799,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"88\"}},{\"x\":763,\"y\":89,\"z\":805,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"89\"}},{\"x\":768,\"y\":101,\"z\":806,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"90\"}},{\"x\":785,\"y\":105,\"z\":808,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"91\"}},{\"x\":791,\"y\":101,\"z\":805,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"92\"}},{\"x\":791,\"y\":103,\"z\":784,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"93\"}},{\"x\":771,\"y\":102,\"z\":778,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"94\"}},{\"x\":764,\"y\":99,\"z\":781,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"95\"}},{\"x\":758,\"y\":97,\"z\":792,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"96\"}},{\"x\":762,\"y\":93,\"z\":783,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"97\"}},{\"x\":778,\"y\":92,\"z\":775,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"98\"}},{\"x\":786,\"y\":90,\"z\":784,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"99\"}},{\"x\":790,\"y\":88,\"z\":792,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"100\"}},{\"x\":792,\"y\":82,\"z\":815,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"101\"}},{\"x\":783,\"y\":76,\"z\":811,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"102\"}},{\"x\":795,\"y\":69,\"z\":821,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"103\"}},{\"x\":800,\"y\":66,\"z\":807,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"104\"}},{\"x\":810,\"y\":65,\"z\":811,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"105\"}},{\"x\":817,\"y\":75,\"z\":813,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"106\"}},{\"x\":815,\"y\":75,\"z\":800,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"107\"}},{\"x\":822,\"y\":85,\"z\":800,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"108\"}},{\"x\":822,\"y\":85,\"z\":785,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"109\"}},{\"x\":811,\"y\":88,\"z\":778,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"110\"}},{\"x\":804,\"y\":86,\"z\":792,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"111\"}},{\"x\":792,\"y\":78,\"z\":790,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"112\"}},{\"x\":790,\"y\":77,\"z\":793,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"113\"}},{\"x\":785,\"y\":68,\"z\":800,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"114\"}},{\"x\":773,\"y\":75,\"z\":797,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"115\"}},{\"x\":773,\"y\":78,\"z\":794,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"116\"}},{\"x\":757,\"y\":83,\"z\":797,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"117\"}},{\"x\":742,\"y\":84,\"z\":791,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"118\"}},{\"x\":737,\"y\":85,\"z\":797,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"119\"}},{\"x\":731,\"y\":75,\"z\":813,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"120\"}},{\"x\":731,\"y\":81,\"z\":816,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"121\"}},{\"x\":743,\"y\":82,\"z\":821,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"122\"}},{\"x\":746,\"y\":96,\"z\":798,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"123\"}},{\"x\":742,\"y\":110,\"z\":788,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"124\"}},{\"x\":745,\"y\":110,\"z\":807,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"125\"}},{\"x\":752,\"y\":113,\"z\":805,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"126\"}},{\"x\":742,\"y\":123,\"z\":801,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"127\"}},{\"x\":736,\"y\":129,\"z\":801,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"128\"}},{\"x\":733,\"y\":138,\"z\":795,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"129\"}},{\"x\":737,\"y\":134,\"z\":792,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"130\"}},{\"x\":741,\"y\":131,\"z\":799,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"131\"}},{\"x\":743,\"y\":129,\"z\":802,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"132\"}},{\"x\":753,\"y\":134,\"z\":804,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"133\"}},{\"x\":755,\"y\":139,\"z\":808,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"134\"}},{\"x\":757,\"y\":131,\"z\":798,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"135\"}},{\"x\":772,\"y\":140,\"z\":803,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"136\"}},{\"x\":773,\"y\":144,\"z\":797,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"137\"}},{\"x\":784,\"y\":142,\"z\":797,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"138\"}},{\"x\":785,\"y\":141,\"z\":795,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"139\"}},{\"x\":793,\"y\":147,\"z\":801,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"140\"}},{\"x\":785,\"y\":137,\"z\":810,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"141\"}},{\"x\":790,\"y\":133,\"z\":800,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"142\"}},{\"x\":806,\"y\":131,\"z\":803,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"143\"}},{\"x\":803,\"y\":131,\"z\":809,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":\"144\"}}]";
		WaypointGroup waypointGroup = Waypoints.fromColeweightJson(coleweightJson, Location.CRYSTAL_HOLLOWS);

		Assertions.assertEquals(SAPPHIRE_WAYPOINTS, waypointGroup);
	}

	@Test
	void testFromColeweightJsonMineshaftAmber() {
		String coleweightJson = "[{\"x\":-174,\"y\":10,\"z\":-190,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":1}},{\"x\":-166,\"y\":8,\"z\":-177,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":2}},{\"x\":-181,\"y\":8,\"z\":-162,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":3}},{\"x\":-176,\"y\":8,\"z\":-159,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":4}},{\"x\":-162,\"y\":10,\"z\":-159,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":5}},{\"x\":-164,\"y\":8,\"z\":-164,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":6}},{\"x\":-157,\"y\":8,\"z\":-166,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":7}},{\"x\":-152,\"y\":7,\"z\":-166,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":8}},{\"x\":-155,\"y\":10,\"z\":-182,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":9}},{\"x\":-150,\"y\":10,\"z\":-187,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":10}},{\"x\":-144,\"y\":12,\"z\":-188,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":11}},{\"x\":-141,\"y\":10,\"z\":-181,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":12}},{\"x\":-130,\"y\":7,\"z\":-178,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":13}},{\"x\":-125,\"y\":8,\"z\":-192,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":14}},{\"x\":-121,\"y\":8,\"z\":-192,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":15}},{\"x\":-114,\"y\":7,\"z\":-182,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":16}},{\"x\":-110,\"y\":7,\"z\":-178,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":17}},{\"x\":-106,\"y\":28,\"z\":-162,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":18}},{\"x\":-113,\"y\":22,\"z\":-152,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":19}},{\"x\":-154,\"y\":25,\"z\":-162,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":20}},{\"x\":-155,\"y\":25,\"z\":-168,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":21}},{\"x\":-155,\"y\":26,\"z\":-186,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":22}},{\"x\":-160,\"y\":24,\"z\":-190,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":23}},{\"x\":-170,\"y\":21,\"z\":-189,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":24}},{\"x\":-175,\"y\":24,\"z\":-175,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":25}},{\"x\":-162,\"y\":24,\"z\":-177,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":26}},{\"x\":-174,\"y\":27,\"z\":-164,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":27}},{\"x\":-168,\"y\":26,\"z\":-153,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":28}},{\"x\":-174,\"y\":23,\"z\":-158,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":29}},{\"x\":-178,\"y\":17,\"z\":-154,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":30}},{\"x\":-185,\"y\":17,\"z\":-164,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":31}},{\"x\":-194,\"y\":9,\"z\":-181,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":32}},{\"x\":-193,\"y\":6,\"z\":-196,\"r\":0,\"g\":1,\"b\":0,\"options\":{\"name\":33}}]";
		WaypointGroup waypointGroup = Waypoints.fromColeweightJson(coleweightJson, Location.GLACITE_MINESHAFTS);
		WaypointGroup expectedWaypointGroup = new WaypointGroup("Coleweight", Location.GLACITE_MINESHAFTS, List.of(
				new OrderedNamedWaypoint(new BlockPos(-174, 10, -190), "1", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-166, 8, -177), "2", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-181, 8, -162), "3", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-176, 8, -159), "4", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-162, 10, -159), "5", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-164, 8, -164), "6", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-157, 8, -166), "7", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-152, 7, -166), "8", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-155, 10, -182), "9", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-150, 10, -187), "10", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-144, 12, -188), "11", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-141, 10, -181), "12", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-130, 7, -178), "13", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-125, 8, -192), "14", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-121, 8, -192), "15", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-114, 7, -182), "16", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-110, 7, -178), "17", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-106, 28, -162), "18", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-113, 22, -152), "19", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-154, 25, -162), "20", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-155, 25, -168), "21", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-155, 26, -186), "22", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-160, 24, -190), "23", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-170, 21, -189), "24", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-175, 24, -175), "25", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-162, 24, -177), "26", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-174, 27, -164), "27", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-168, 26, -153), "28", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-174, 23, -158), "29", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-178, 17, -154), "30", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-185, 17, -164), "31", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-194, 9, -181), "32", new float[]{0, 1, 0}),
				new OrderedNamedWaypoint(new BlockPos(-193, 6, -196), "33", new float[]{0, 1, 0})
		), true, true, WaypointGroup.DEFAULT_TYPE);

		Assertions.assertEquals(expectedWaypointGroup, waypointGroup);
	}

	@Test
	void testFromSkyblocker() {
		String waypointGroupsSkyblocker = "[Skyblocker-Waypoint-Data-V1]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";
		List<WaypointGroup> waypointGroups = Waypoints.fromSkyblocker(waypointGroupsSkyblocker, Location.UNKNOWN);
		List<WaypointGroup> expectedWaypointGroups = List.of(SAPPHIRE_WAYPOINTS);

		Assertions.assertEquals(expectedWaypointGroups, waypointGroups);
	}

	@Test
	void testFromSkyblockerOrderedRiftParkour() {
		String waypointGroup = "[Skyblocker::OrderedWaypoints::v1]H4sIAAAAAAAA/43US2rEMAwG4LtorYXfj2x7jJJFOuNCIROHdMpQQu5e2ZlCV5UWIRA+ZH5Jzg7vc32UDYYdlulWYPj9gFCW6W0uVxju21dBeEzfa/1Y7p8wvO6w1vZ2AXXI6NyIcKlz3V7qba1LOdV44F8YFbrIwtih1yxMBDV6/uh4wsRC348OhoWaUkcMmYW5w8hC3yomAXSqV0xsGJt7RQFMfYQ8dI6gxcxPxhJ0EmgIegls7QkSeLaHh8/2CODZHhmMFCZIZk1QK89Jk56SH6IR16TgkS6j4u+NEQbqTadQWiyNEkl6jGWj01q2Rln219Il7aaT1WxSUjPHtpws9P/D8Th+AGteDz6MBQAA";
		List<WaypointGroup> waypointGroups = Waypoints.fromSkyblocker(waypointGroup, Location.THE_RIFT);
		List<WaypointGroup> expected = List.of(RIFT_PARKOUR_WAYPOINTS);

		Assertions.assertEquals(expected, waypointGroups);
	}

	@Test
	void testFromSkyblockerOrderedDarkMonoliths() {
		String waypointGroup = "[Skyblocker::OrderedWaypoints::v1]H4sIAAAAAAAA/42SzQrCMBCE32XPG8jmr6ZXzz6B9FA1YKFNilZESt/drSKeZD2EQPgyOzPJDLuSS99NZ6hnyO2QoP4eIaTcHvp0gnq63BLCvX2MpcvTFer9DGNZd0UBjQ2oom0QjqUvl20ZxpLTm2sW/KAuotEG+YaRUM+i5FAZL5FkItKmQh9F0mukGHhpCQ3ETh3rkkRGek0nK5IVU4EdyOFVXCkkLUfi4Rb/KAmp0ly8PNlxcEJlxYoUJzeGUS++uwqeVfmLhF9Gm2V5AvVdxiCLAgAA";
		List<WaypointGroup> waypointGroups = Waypoints.fromSkyblocker(waypointGroup, Location.DWARVEN_MINES);
		List<WaypointGroup> expected = List.of(DARK_MONOLITHS_WAYPOINTS);

		Assertions.assertEquals(expected, waypointGroups);
	}

	@Test
	void testToSkyblocker() {
		String waypointGroupsSkyblocker = Waypoints.toSkyblocker(List.of(SAPPHIRE_WAYPOINTS));
		String expectedWaypointGroupsSkyblocker = "[Skyblocker-Waypoint-Data-V1]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";

		Assertions.assertEquals(expectedWaypointGroupsSkyblocker, waypointGroupsSkyblocker);
	}
}
