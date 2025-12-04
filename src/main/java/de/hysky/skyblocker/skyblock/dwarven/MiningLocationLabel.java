package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.waypoint.DistancedNamedWaypoint;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class MiningLocationLabel extends DistancedNamedWaypoint {
	private final Category category;

	public MiningLocationLabel(Category category, BlockPos pos) {
		// Set enabled to false in order to prevent the waypoint from being rendered, but the name text and distance will still be rendered.
		super(pos, getName(category), new float[]{0, 0, 0}, false);
		this.category = category;
	}

	private static Text getName(Category category) {
		if (SkyblockerConfigManager.get().mining.commissionWaypoints.useColor) {
			return Text.literal(category.getName()).withColor(category.getColor());
		}
		return Text.literal(category.getName());
	}

	public Category category() {
		return category;
	}

	/**
	 * Override the {@link DistancedNamedWaypoint#shouldRenderName()} method to always return true,
	 * as the name should always be rendered, even though this waypoint is always disabled.
	 */
	@Override
	protected boolean shouldRenderName() {
		return true;
	}

	public interface Category {
		String getName();

		int getColor(); //all the color codes are the color of the block the waypoint is for
	}

	enum DwarvenCategory implements Category {
		LAVA_SPRINGS("Lava Springs", new BlockPos(60, 197, -15)),
		CLIFFSIDE_VEINS("Cliffside Veins", new BlockPos(40, 128, 40)),
		RAMPARTS_QUARRY("Rampart's Quarry", new BlockPos(-100, 150, -20)),
		UPPER_MINES("Upper Mines", new BlockPos(-130, 174, -50)),
		ROYAL_MINES("Royal Mines", new BlockPos(130, 154, 30)),
		GLACITE_WALKER("Glacite Walker", new BlockPos(0, 128, 150));


		boolean isTitanium;
		private final String name;
		private final BlockPos location;

		DwarvenCategory(String name, BlockPos location) {
			this.name = name;
			this.location = location;
		}

		public BlockPos getLocation() {
			return location;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getColor() {
			if (isTitanium) {
				return 0xd8d6d8;
			}
			return 0x45bde0;
		}
	}

	enum DwarvenEmissaries implements Category {
		LAVA_SPRINGS(new BlockPos(58, 198, -8)),
		CLIFFSIDE_VEINS(new BlockPos(42, 134, 22)),
		RAMPARTS_QUARRY(new BlockPos(-72, 153, -10)),
		UPPER_MINES(new BlockPos(-132, 174, -50)),
		ROYAL_MINES(new BlockPos(171, 150, 31)),
		DWARVEN_VILLAGE(new BlockPos(-37, 200, -131)),
		DWARVEN_MINES(new BlockPos(89, 198, -92));

		private final BlockPos location;

		DwarvenEmissaries(BlockPos location) {

			this.location = location;
		}

		public BlockPos getLocation() {
			return location;
		}

		@Override
		public String toString() {
			return "Emissary";
		}

		@Override
		public String getName() {
			return "Emissary";
		}

		@Override
		public int getColor() {
			return 0xffffff;
		}
	}

	enum GlaciteCategory implements Category {
		AQUAMARINE("Aquamarine", 0x334cb1, new BlockPos[]{new BlockPos(20, 136, 370), new BlockPos(-14, 132, 386), new BlockPos(6, 137, 411), new BlockPos(50, 117, 302)}),
		ONYX("Onyx", 0x191919, new BlockPos[]{new BlockPos(4, 127, 307), new BlockPos(-3, 139, 434), new BlockPos(77, 118, 411), new BlockPos(-68, 130, 404)}),
		PERIDOT("Peridot", 0x667f33, new BlockPos[]{new BlockPos(66, 144, 284), new BlockPos(94, 154, 284), new BlockPos(-62, 147, 303), new BlockPos(-77, 119, 283), new BlockPos(87, 122, 394), new BlockPos(-73, 122, 456)}),
		CITRINE("Citrine", 0x664c33, new BlockPos[]{new BlockPos(-86, 143, 261), new BlockPos(74, 150, 327), new BlockPos(63, 137, 343), new BlockPos(38, 119, 386), new BlockPos(55, 150, 400), new BlockPos(-45, 127, 415), new BlockPos(-60, 144, 424), new BlockPos(-54, 132, 410)}),
		CAMPFIRE("Base Camp", 0x983333, new BlockPos[]{new BlockPos(-7, 126, 229)});

		private final String name;
		private final int color;
		private final BlockPos[] location;

		GlaciteCategory(String name, int color, BlockPos[] location) {
			this.name = name;
			this.color = color;
			this.location = location;
		}

		public BlockPos[] getLocations() {
			return location;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getColor() {
			return color;
		}
	}

	/**
	 * enum for the different waypoints used int the crystals hud each with a {@link CrystalHollowsLocationsCategory#name} and associated {@link CrystalHollowsLocationsCategory#color}
	 */
	public enum CrystalHollowsLocationsCategory implements Category, StringIdentifiable {
		UNKNOWN("Unknown", Color.WHITE, null), //used when a location is known but what's at the location is not known
		JUNGLE_TEMPLE("Jungle Temple", new Color(DyeColor.PURPLE.getSignColor()), "[NPC] Kalhuiki Door Guardian:"),
		MINES_OF_DIVAN("Mines of Divan", Color.GREEN, "    Jade Crystal"),
		GOBLIN_QUEENS_DEN("Goblin Queen's Den", new Color(DyeColor.ORANGE.getSignColor()), "    Amber Crystal"),
		LOST_PRECURSOR_CITY("Lost Precursor City", Color.CYAN, "    Sapphire Crystal"),
		KHAZAD_DUM("Khazad-dûm", Color.YELLOW, "    Topaz Crystal"),
		FAIRY_GROTTO("Fairy Grotto", Color.PINK, null),
		DRAGONS_LAIR("Dragon's Lair", Color.BLACK, null),
		CORLEONE("Corleone", Color.WHITE, null),
		KING_YOLKAR("King Yolkar", Color.RED, "[NPC] King Yolkar:"),
		ODAWA("Odawa", Color.MAGENTA, "[NPC] Odawa:"),
		KEY_GUARDIAN("Key Guardian", Color.LIGHT_GRAY, null);

		public static final Codec<CrystalHollowsLocationsCategory> CODEC = StringIdentifiable.createBasicCodec(CrystalHollowsLocationsCategory::values);

		public final Color color;
		private final String name;
		private final String linkedMessage;

		CrystalHollowsLocationsCategory(String name, Color color, String linkedMessage) {
			this.name = name;
			this.color = color;
			this.linkedMessage = linkedMessage;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getColor() {
			return this.color.getRGB();
		}

		public String getLinkedMessage() {
			return this.linkedMessage;
		}

		@Override
		public String asString() {
			return name();
		}
	}

}
