package de.hysky.skyblocker.skyblock.dungeon;

import java.util.List;
import java.util.Set;

import de.hysky.skyblocker.utils.FunUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2dc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class DungeonMapLabels {
	private static final Object2ObjectOpenHashMap<String, RoomLabel> LABELS = new Object2ObjectOpenHashMap<>();
	private static final float LABEL_SCALE = 0.55f;
	private static final float MAX_WIDTH_SCALAR = 1.5f;

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_n, _p, _c) -> DungeonMapLabels.clearLabels());
		DungeonEvents.DUNGEON_ENDED.register(DungeonMapLabels::clearLabels);
		DungeonEvents.ROOM_MATCHED.register(DungeonMapLabels::onRoomMatched);
		Scheduler.INSTANCE.scheduleCyclic(() -> updateRoomNames(null), 20);
	}

	private static boolean shouldProcess() {
		return Utils.isInDungeons() && DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss() && SkyblockerConfigManager.get().dungeons.dungeonMap.showRoomLabels;
	}

	private static void clearLabels() {
		if (!LABELS.isEmpty()) LABELS.clear();
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
		Font textRenderer = Minecraft.getInstance().font;

		if (newRoom != null) {
			addRoomLabel(newRoom, entrancePos, mapEntrancePos, mapRoomSize, textRenderer);
			return;
		}

		LABELS.clear();
		DungeonManager.getRoomsStream().filter(Room::isMatched).forEach(room ->
				addRoomLabel(room, entrancePos, mapEntrancePos, mapRoomSize, textRenderer));
	}

	private static void addRoomLabel(Room room, Vector2ic entrancePos, Vector2ic mapEntrancePos, int mapRoomSize, Font textRenderer) {
		if (LABELS.containsKey(room.getName())) return;
		Vec3 labelPos = getPosForLabel(room, mapRoomSize, textRenderer.lineHeight);
		if (labelPos == null) return;
		Vector2dc mapPos = DungeonMapUtils.getMapPosFromPhysical(entrancePos, mapEntrancePos, mapRoomSize, labelPos);
		//noinspection DataFlowIssue - room is matched
		DungeonManager.RoomInfo roomInfo = DungeonManager.getRoomMetadata(room.getName());
		if (roomInfo == null) return;
		String roomName = roomInfo.name();

		int color = switch (room.clearState) {
			case Room.ClearState.GREEN_CHECKED -> CommonColors.GREEN;
			case Room.ClearState.WHITE_CHECKED -> CommonColors.WHITE;
			case Room.ClearState.UNCLEARED -> CommonColors.GRAY;
			case Room.ClearState.FAILED -> CommonColors.RED;
		};

		float width = getMaxWidth(room, mapRoomSize) / LABEL_SCALE;
		Component text = getLabelForRoom(room, roomName);
		List<FormattedCharSequence> lines = textRenderer.split(text, (int) width);
		LABELS.put(room.getName(), new RoomLabel(lines, (int) mapPos.x(), (int) mapPos.y(), color));
	}

	private static Component getLabelForRoom(Room room, String roomName) {
		if (room.getType() != Room.Type.ROOM && room.getType() != Room.Type.TRAP) return Component.literal(roomName);

		RoomLabelType roomLabelType = SkyblockerConfigManager.get().dungeons.dungeonMap.roomLabelType;
		return switch (roomLabelType) {
			case RoomLabelType.ROOM_NAME -> Component.literal(roomName);
			case RoomLabelType.SECRETS_FOUND -> Component.literal(room.getFoundSecretCount() + "/" + room.getMaxSecretCount());
			case RoomLabelType.ROOM_NAME_AND_SECRETS_FOUND -> {
				if (FunUtils.shouldEnableFun())
					yield Component.literal(room.getFoundSecretCount() + "/" + room.getMaxSecretCount()).append("\n").append(roomName);
				yield Component.literal(roomName).append("\n").append(room.getFoundSecretCount() + "/" + room.getMaxSecretCount());
			}
		};
	}

	protected static void renderRoomNames(GuiGraphics context) {
		if (!SkyblockerConfigManager.get().dungeons.dungeonMap.showRoomLabels) return;

		Font textRenderer = Minecraft.getInstance().font;
		for (RoomLabel label : LABELS.values()) {
			context.pose().pushMatrix();
			context.pose().translate(label.x, label.y);
			context.pose().scale(LABEL_SCALE);
			drawText(context, textRenderer, label.textLines, label.color);
			context.pose().popMatrix();
		}
	}

	/**
	 * The general idea is to put the text in the center of the room, this can vary on the room rotation.<br>
	 * For 1x1 rooms, this is very simple.<br>
	 * For L-shaped rooms, the text is centered on the wide part of the room.<br>
	 * For the remaining room types, we take the average of the x and y.<br>
	 */
	@SuppressWarnings("incomplete-switch")
	private static @Nullable Vec3 getPosForLabel(Room room, int mapRoomSize, int fontHeight) {
		switch (room.getType()) {
			case BLOOD, FAIRY:
				return null;
		}

		// Each room is made up of one or more segments, depending on the size of the room.
		Set<Vector2ic> segments = room.getSegments();
		int[] segmentsX = segments.stream().mapToInt(Vector2ic::x).sorted().toArray();
		int[] segmentsY = segments.stream().mapToInt(Vector2ic::y).sorted().toArray();

		Vector2i result = null;
		switch (room.getShape()) {
			case TRAP, PUZZLE, MINIBOSS, ONE_BY_ONE -> {
				result = new Vector2i(segmentsX[0] + mapRoomSize - 2, (int) (segmentsY[0] + mapRoomSize - ((double) fontHeight / 2) + 1));
				if (mapRoomSize == 18) result.x -= 2;
			}
			case L_SHAPE -> {
				int medX;
				int medY = segmentsY[1] + mapRoomSize - 2;

				if (room.getDirection() == Room.Direction.SE || room.getDirection() == Room.Direction.SW) {
					medX = segmentsX[1] - 2;
				} else {
					medX = segmentsX[2] - mapRoomSize / 4 + 2;
				}

				result = new Vector2i(medX, medY);
			}
			case TWO_BY_TWO, ONE_BY_TWO, ONE_BY_THREE, ONE_BY_FOUR -> {
				int avgX = (segmentsX[0] + segmentsX[segmentsX.length - 1] + mapRoomSize) / 2;
				if (room.getDirection() == Room.Direction.SE || room.getDirection() == Room.Direction.SW) {
					avgX += mapRoomSize / 2;
				} else {
					avgX += mapRoomSize / 4 + 1;
				}
				int avgY = (segmentsY[0] + segmentsY[segmentsY.length - 1] + mapRoomSize) / 2 + mapRoomSize / 3 + 2;

				result = new Vector2i(avgX, avgY);
			}
		}

		result.sub(0, fontHeight / 2, result);
		return new Vec3(result.x(), 0, result.y());
	}

	/**
	 * Determines how long a room name can be before wrapping onto the next line.<br>
	 * Generally, it is the size of the room multiplied by mapRoomSize.<br>
	 * For 1x2s, 1x3s, and 1x4s, the rotation of the room is important, since we only draw text horizontally.
	 */
	private static int getMaxWidth(Room room, int mapRoomSize) {
		int maxWidth = switch (room.getShape()) {
			case ONE_BY_ONE -> {
				if (mapRoomSize == 16) yield mapRoomSize + 2; // minor nudging so most names can fit on one line
				else yield mapRoomSize;
			}
			case TRAP, PUZZLE, MINIBOSS -> mapRoomSize;
			case ONE_BY_TWO, ONE_BY_THREE, ONE_BY_FOUR -> {
				if (room.getDirection() == Room.Direction.NE) yield mapRoomSize;
				yield mapRoomSize * 3;
			}
			case L_SHAPE, TWO_BY_TWO -> mapRoomSize * 2;
		};

		return (int) (maxWidth * MAX_WIDTH_SCALAR);
	}

	private static void drawText(GuiGraphics context, Font textRenderer, List<FormattedCharSequence> lines, int color) {
		int y = lines.size() > 1 ? -(textRenderer.lineHeight / 2) * (lines.size() - 1) : 0;
		for (FormattedCharSequence orderedText : lines) {
			int textWidth = textRenderer.width(orderedText) / 2;
			HudHelper.drawOutlinedText(context, orderedText, -textWidth, y, color, CommonColors.BLACK);
			y += 9;
		}
	}

	private record RoomLabel(List<FormattedCharSequence> textLines, int x, int y, int color) {}

	public enum RoomLabelType implements StringRepresentable {
		ROOM_NAME,
		SECRETS_FOUND,
		ROOM_NAME_AND_SECRETS_FOUND;

		@Override
		public String getSerializedName() {
			return name();
		}

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.dungeons.map.roomLabelType." + name());
		}
	}
}
