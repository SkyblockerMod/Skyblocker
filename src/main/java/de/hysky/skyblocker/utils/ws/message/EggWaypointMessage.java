package de.hysky.skyblocker.utils.ws.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.chocolatefactory.EggFinder;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.ws.Type;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public record EggWaypointMessage(EggFinder.EggType eggType, BlockPos coordinates) implements Message<EggWaypointMessage> {
	private static final Codec<EggWaypointMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EggFinder.EggType.CODEC.fieldOf("eggType").forGetter(EggWaypointMessage::eggType),
			BlockPos.CODEC.fieldOf("coordinates").forGetter(EggWaypointMessage::coordinates)
	).apply(instance, EggWaypointMessage::new));

	private static final Codec<List<EggWaypointMessage>> LIST_CODEC = CODEC.listOf();

	public static void handle(Type type, Optional<Dynamic<?>> message) {
		switch (type) {
			case Type.RESPONSE -> {
				if (message.isEmpty()) return;
				EggWaypointMessage waypoint = CODEC.parse(message.get()).getOrThrow();

				RenderHelper.runOnRenderThread(() -> EggFinder.onWebsocketMessage(waypoint));
			}

			case Type.INITIAL_MESSAGE -> {
				if (message.isEmpty()) return;
				List<EggWaypointMessage> waypoints = LIST_CODEC.parse(message.get()).getOrThrow();

				RenderHelper.runOnRenderThread(() -> waypoints.forEach(EggFinder::onWebsocketMessage));
			}

			default -> {}
		}
	}

	@Override
	public Codec<EggWaypointMessage> getCodec() {
		return CODEC;
	}
}
