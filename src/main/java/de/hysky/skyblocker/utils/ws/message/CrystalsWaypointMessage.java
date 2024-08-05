package de.hysky.skyblocker.utils.ws.message;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.skyblock.dwarven.CrystalsLocationsManager;
import de.hysky.skyblocker.skyblock.dwarven.MiningLocationLabel.CrystalHollowsLocationsCategory;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.ws.Type;
import net.minecraft.util.math.BlockPos;

public record CrystalsWaypointMessage(CrystalHollowsLocationsCategory location, BlockPos coordinates) implements Message<CrystalsWaypointMessage> {
	private static final Codec<CrystalsWaypointMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CrystalHollowsLocationsCategory.CODEC.fieldOf("name").forGetter(CrystalsWaypointMessage::location),
			BlockPos.CODEC.fieldOf("coordinates").forGetter(CrystalsWaypointMessage::coordinates))
			.apply(instance, CrystalsWaypointMessage::new));
	private static final Codec<List<CrystalsWaypointMessage>> LIST_CODEC = CODEC.listOf();

	public static void handle(Type type, Optional<Dynamic<?>> message) {
		switch (type) {
			case Type.RESPONSE -> {
				CrystalsWaypointMessage waypoint = CODEC.parse(message.get()).getOrThrow();

				RenderHelper.runOnRenderThread(() -> CrystalsLocationsManager.addCustomWaypointFromSocket(waypoint.location(), waypoint.coordinates()));
			}

			case Type.INITIAL_MESSAGE -> {
				List<CrystalsWaypointMessage> waypoints = LIST_CODEC.parse(message.get()).getOrThrow();

				RenderHelper.runOnRenderThread(() -> {
					for (CrystalsWaypointMessage waypoint : waypoints) {
						CrystalsLocationsManager.addCustomWaypointFromSocket(waypoint.location(), waypoint.coordinates());
					}
				});
			}

			default -> {}
		}
	}

	@Override
	public Codec<CrystalsWaypointMessage> getCodec() {
		return CODEC;
	}
}
