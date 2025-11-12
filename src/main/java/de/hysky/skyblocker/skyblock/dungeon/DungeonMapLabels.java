package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2dc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;
import java.util.Set;

public class DungeonMapLabels {
	private static final Object2ObjectOpenHashMap<String, RoomLabel> LABELS = new Object2ObjectOpenHashMap<String, RoomLabel>();
	private static final float LABEL_SCALE = 0.55f;
	private static final float MAX_WIDTH_SCALAR = 1.5f;

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(() -> updateRoomNames(null), 20);
		DungeonEvents.ROOM_MATCHED.register(DungeonMapLabels::onRoomMatched);
	}

	private static boolean shouldProcess() {
		return Utils.isInDungeons() && DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss() && SkyblockerConfigManager.get().dungeons.dungeonMap.showRoomLabels;
	}

	private static void onRoomMatched(Room room) {
		updateRoomNames(room);
	}

	private static void updateRoomNames(@Nullable Room newRoom) {
		if (!shouldProcess()) return;

		Vector2ic entrancePos = DungeonManager.getPhysicalEntrancePos();
		Vector2ic mapEntrancePos = DungeonManager.getMapEntrancePos();
		if (entrancePos == null || mapEntrancePos == null) return;
		int mapRoomSize = DungeonManager.getMapRoomSize();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		if (newRoom != null) {
			addRoomLabel(newRoom, entrancePos, mapEntrancePos, mapRoomSize, textRenderer);
			return;
		}

		LABELS.clear();
		DungeonManager.getRoomsStream().filter(Room::isMatched).forEach(room ->
				addRoomLabel(room, entrancePos, mapEntrancePos, mapRoomSize, textRenderer));
	}

	private static void addRoomLabel(Room room, Vector2ic entrancePos, Vector2ic mapEntrancePos, int mapRoomSize, TextRenderer textRenderer) {
		if (LABELS.containsKey(room.getName())) return;
		Vec3d labelPos = getPosForLabel(room, mapRoomSize, textRenderer.fontHeight);
		if (labelPos == null) return;
		Vector2dc mapPos = DungeonMapUtils.getMapPosFromPhysical(entrancePos, mapEntrancePos, mapRoomSize, labelPos);
		DungeonManager.RoomInfo roomInfo = DungeonManager.getRoomMetadata(room.getName());
		if (roomInfo == null) return;
		String roomName = roomInfo.name();

		int color = Colors.GRAY;
		if (room.greenChecked) {
			color = Colors.GREEN;
		} else if (room.whiteChecked) {
			color = Colors.WHITE;
		}

		Text text = Text.literal(roomName);
		LABELS.put(room.getName(), new RoomLabel(text, (int) mapPos.x(), (int) mapPos.y(), getMaxWidth(room, mapRoomSize), color));
	}

	protected static void renderRoomNames(DrawContext context) {
		if (!SkyblockerConfigManager.get().dungeons.dungeonMap.showRoomLabels) return;

		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		context.getMatrices().pushMatrix();
		for (RoomLabel label : LABELS.values()) {
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(label.x, label.y);
			context.getMatrices().scale(LABEL_SCALE);
			drawText(context, textRenderer, label.text, 0, 0, (int) (label.width / LABEL_SCALE), label.color);
			context.getMatrices().popMatrix();
		}
		context.getMatrices().popMatrix();
	}

	private static Vec3d getPosForLabel(Room room, int mapRoomSize, int fontHeight) {
		switch (room.getType()) {
			case BLOOD, MINIBOSS, FAIRY:
				return null;
		}

		Set<Vector2ic> segments = room.getSegments();
		int[] segmentsX = segments.stream().mapToInt(Vector2ic::x).sorted().toArray();
		int[] segmentsY = segments.stream().mapToInt(Vector2ic::y).sorted().toArray();

		Vector2i result = null;
		switch (room.getShape()) {
			case TRAP, PUZZLE, ONE_BY_ONE:
				result = new Vector2i(segmentsX[0] + mapRoomSize - 2, (int) (segmentsY[0] + mapRoomSize - ((double) fontHeight / 2) + 1));
				if (mapRoomSize == 18) result.x -= 2;
				break;
			case L_SHAPE:
				int medX;
				int medY = segmentsY[1] + mapRoomSize - 2;

				if (room.getDirection() == Room.Direction.SE || room.getDirection() == Room.Direction.SW) {
					medX = segmentsX[1] - 2;
				} else {
					medX = segmentsX[2] - mapRoomSize / 4 + 2;
				}

				result = new Vector2i(medX, medY);
				break;
			case TWO_BY_TWO, ONE_BY_TWO, ONE_BY_THREE, ONE_BY_FOUR:
				int avgX = (segmentsX[0] + segmentsX[segmentsX.length - 1] + mapRoomSize) / 2;
				if (room.getDirection() == Room.Direction.SE || room.getDirection() == Room.Direction.SW) {
					avgX += mapRoomSize / 2;
				} else {
					avgX += mapRoomSize / 4 + 1;
				}
				int avgY = (segmentsY[0] + segmentsY[segmentsY.length - 1] + mapRoomSize) / 2 + mapRoomSize / 3 + 2;

				result = new Vector2i(avgX, avgY);
				break;
		}

		result.sub(0, fontHeight / 2, result);
		return new Vec3d(result.x(), 0, result.y());
	}

	private static int getMaxWidth(Room room, int mapRoomSize) {
		int maxWidth = switch (room.getShape()) {
			case ONE_BY_ONE -> {
				if (mapRoomSize == 16) yield mapRoomSize + 2;
				else yield mapRoomSize;
			}
			case TRAP, PUZZLE -> mapRoomSize;
			case ONE_BY_TWO, ONE_BY_THREE, ONE_BY_FOUR -> {
				if (room.getDirection() == Room.Direction.NE) yield mapRoomSize;
				yield mapRoomSize * 3;
			}
			case L_SHAPE, TWO_BY_TWO -> mapRoomSize * 2;
		};

		return (int) (maxWidth * MAX_WIDTH_SCALAR);
	}

	private static void drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int width, int color) {
		List<OrderedText> lines = textRenderer.wrapLines(text, width);
		if (lines.size() > 1) y -= (textRenderer.fontHeight / 2) * (lines.size() - 1);
		for (OrderedText orderedText : lines) {
			int textWidth = textRenderer.getWidth(orderedText) / 2;
//			if (Screen.hasControlDown()) context.fill(x - textWidth, y - textRenderer.fontHeight / 2 + 4, x + textWidth, y + textRenderer.fontHeight - 1, Colors.RED);
			HudHelper.drawOutlinedText(context, orderedText, x - textWidth, y, color, Colors.BLACK);
			y += 9;
		}
	}

	record RoomLabel(Text text, int x, int y, int width, int color) {}
}
