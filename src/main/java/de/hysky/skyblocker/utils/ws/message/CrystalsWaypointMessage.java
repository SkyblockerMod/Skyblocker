package de.hysky.skyblocker.utils.ws.message;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.skyblock.dwarven.CrystalsLocationsManager;
import de.hysky.skyblocker.skyblock.dwarven.MiningLocationLabel.CrystalHollowsLocationsCategory;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.ws.Type;

public record CrystalsWaypointMessage(CrystalHollowsLocationsCategory location, BlockPos coordinates) implements Message<CrystalsWaypointMessage> {
	private static final Codec<CrystalsWaypointMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CrystalHollowsLocationsCategory.CODEC.fieldOf("name").forGetter(CrystalsWaypointMessage::location),
			BlockPos.CODEC.fieldOf("coordinates").forGetter(CrystalsWaypointMessage::coordinates))
			.apply(instance, CrystalsWaypointMessage::new));
	private static final Codec<List<CrystalsWaypointMessage>> LIST_CODEC = CODEC.listOf();

	public static void handle(Type type, Optional<Dynamic<?>> message) {
		switch (type) {
			case Type.RESPONSE -> {
				if (message.isEmpty()) return;
				CrystalsWaypointMessage waypoint = CODEC.parse(message.get()).getOrThrow();

				RenderHelper.runOnRenderThread(() -> CrystalsLocationsManager.addCustomWaypointFromSocket(waypoint));
			}

			case Type.INITIAL_MESSAGE -> {
				if (message.isEmpty()) return;
				List<CrystalsWaypointMessage> waypoints = LIST_CODEC.parse(message.get()).getPartialOrThrow();

				if (waypoints.isEmpty()) return;
				RenderHelper.runOnRenderThread(() -> CrystalsLocationsManager.addCustomWaypointFromSocket(waypoints.toArray(
						new CrystalsWaypointMessage[0]
				)));
			}

			default -> {}
		}
	}

	@Override
	public Codec<CrystalsWaypointMessage> getCodec() {
		return CODEC;
	}
}
