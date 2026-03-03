package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import de.hysky.skyblocker.utils.Location;
import net.minecraft.core.BlockPos;

public class Shards {
	public Traps traps = new Traps();
	public List<OwnedShard> owned = List.of();
	public int fused;

	public static class Traps {
		@SerializedName("active_traps")
		public List<ActiveTrap> activeTraps = List.of();

		public static class ActiveTrap {
			@SerializedName("trap_item")
			public String trapItem = "";
			@SerializedName("capture_time")
			public long captureTime;
			public String mode = "";
			/**
			 * Comma delimited string of coordinates with decimals
			 */
			public String location = "";
			@SerializedName("placed_at")
			public long placedAt;
			public String shard = "";
			public boolean captured;
			public UUID uuid = UUID.randomUUID();

			public Location getIsland() {
				return Location.from(this.mode);
			}

			public BlockPos getPos() {
				String[] split = this.location.split(",");

				return BlockPos.containing(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
			}
		}
	}

	public static class OwnedShard {
		public String type = "";
		@SerializedName("amount_owned")
		public int amountOwned;
		public long captured;
	}
}
