package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Map;
import java.util.Set;

/**
 * Helper for camping Blood Mobs in the dungeon Blood Room.
 */
public class BloodCampHelper {

	private static final Set<String> WATCHER_SKINS = Set.of(
			"ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwNjM1MjMyMiwKICAicHJvZmlsZUlkIiA6ICI3MmY5MTdjNWQyNDU0OTk0YjlmYzQ1YjVhM2YyMjIzMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGF0X0d1eV9Jc19NZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yNzM5ZDdmNGU2NmE3ZGIyZWE2Y2Q0MTRlNGM0YmE0MWRmN2E5MjQ1NWM5ZmM0MmNhYWIwMTQ2NjVjMzY3YWQ1IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
			"ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwNjI5MjgzNiwKICAicHJvZmlsZUlkIiA6ICIzZDIxZTYyMTk2NzQ0Y2QwYjM3NjNkNTU3MWNlNGJlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTcl83MUJsYWNrYmlyZCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iZjZlMWU3ZWQzNjU4NmMyZDk4MDU3MDAyYmMxYWRjOTgxZTI4ODlmN2JkN2I1YjM4NTJiYzU1Y2M3ODAyMjA0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
			"ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwNjIzOTU4NiwKICAicHJvZmlsZUlkIiA6ICJhYWZmMDUwYTExOTk0NzM1YjEyNDVlNDk0MGFlZjY4NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJMYXN0SW1tb3J0YWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVjMWRjNDdhMDRjZTU3MDAxYThiNzI2ZjAxOGNkZWY0MGI3ZWE5ZDdiZDZkODM1Y2E0OTVhMGVmMTY5Zjg5MyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
			"ewogICJ0aW1lc3RhbXAiIDogMTY5NzMwOTQxNzI1NiwKICAicHJvZmlsZUlkIiA6ICJjYjYxY2U5ODc4ZWI0NDljODA5MzliNWYxNTkwMzE1MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJWb2lkZWRUcmFzaDUxODUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY2MmI2ZmI0YjhiNTg2ZGM0Y2RmODAzYjA0NDRkOWI0MWQyNDVjZGY2NjhkYWIzOGZhNmMwNjRhZmU4ZTQ2MSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
			"ewogICJ0aW1lc3RhbXAiIDogMTY5NzIzODQ0NjgxMiwKICAicHJvZmlsZUlkIiA6ICJmMjc0YzRkNjI1MDQ0ZTQxOGVmYmYwNmM3NWIyMDIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeXBpZ3NlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80Y2VjNDAwMDhlMWMzMWMxOTg0ZjRkNjUwYWJiMzQxMGYyMDM3MTE5ZmQ2MjRhZmM5NTM1NjNiNzM1MTVhMDc3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
			"ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwNjIxMjc1NSwKICAicHJvZmlsZUlkIiA6ICI2NGRiNmMwNTliOTk0OTM2YTY0M2QwODEwODE0ZmJkMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVTaWx2ZXJEcmVhbXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkNjFlODA1NWY2ZWU5N2FiNWI2MTk2YThkN2VjOTgwNzhhYzM3ZTAwMzc2MTU3YjZiNTIwZWFhYTJmOTNhZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
			"ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwNjAwOTg2NywKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM3ZGQxOGI1OTgzYTc2N2U1NTZkYzY0NDI0YWY0YjlhYmRiNzVkNGM5ZThiMDk3ODE4YWZiYzQzMWJmMGUwOSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
			"ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwNTkyNDIwNSwKICAicHJvZmlsZUlkIiA6ICIzZDIxZTYyMTk2NzQ0Y2QwYjM3NjNkNTU3MWNlNGJlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTcl83MUJsYWNrYmlyZCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mNWYwZDc4ZmUzOGQxZDdmNzVmMDhjZGNmMmExODU1ZDZkYTAzMzdlMTE0YTNjNjNlM2JmM2M2MThiYzczMmIwIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
			"ewogICJ0aW1lc3RhbXAiIDogMTU4OTU1MDkyNjM2MSwKICAicHJvZmlsZUlkIiA6ICI0ZDcwNDg2ZjUwOTI0ZDMzODZiYmZjOWMxMmJhYjRhZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzaXJGYWJpb3pzY2hlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUxOTY3ZGI1ZTMxOTk5MTYyNTIwMjE5MDNjZjRlOTk1MmVmN2NlYzIyMGZhYWNhMWJhNzliYWZlNTkzOGJkODAiCiAgICB9CiAgfQp9"
	);

	private static final float[] BOX_COLOR = {1f, 0f, 0f};
	private static final float[] LINE_COLOR = {1f, 1f, 0f};

	private static final Set<ZombieEntity> WATCHERS = new HashSet<>();
	private static final Map<ArmorStandEntity, TrackedMob> MOBS = new HashMap<>();
	/**
	 * Newly loaded zombies to check for watcher status after a tick.
	 */
	private static final Map<ZombieEntity, Integer> PENDING_WATCHERS = new HashMap<>();
	/**
	 * Counts how many mobs have started moving and received a predicted position.
	 * Used to determine which mobs are part of the first wave.
	 */
	private static int mobsPredicted = 0;

	@Init
	public static void init() {
		ClientEntityEvents.ENTITY_LOAD.register(BloodCampHelper::onEntityLoad);
		ClientEntityEvents.ENTITY_UNLOAD.register(BloodCampHelper::onEntityUnload);
		WorldRenderEvents.AFTER_ENTITIES.register(BloodCampHelper::render);
		ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
		ClientPlayConnectionEvents.JOIN.register((h, s, c) -> reset());
		ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> reset());
	}

	private static void onEntityLoad(Entity entity, ClientWorld world) {
		if (!Utils.isInDungeons()) return;
		if (entity instanceof ZombieEntity zombie) {
			PENDING_WATCHERS.put(zombie, 1);
		}
	}

	private static void onEntityUnload(Entity entity, ClientWorld world) {
		if (!Utils.isInDungeons()) return;
		if (entity instanceof ZombieEntity zombie) {
			WATCHERS.remove(zombie);
			PENDING_WATCHERS.remove(zombie);
		} else if (entity instanceof ArmorStandEntity stand) {
			MOBS.remove(stand);
		}
	}

	private static void tick() {
		if (!Utils.isInDungeons()) return;
		long now = System.currentTimeMillis();
		// Process any newly loaded zombies waiting to be checked
		PENDING_WATCHERS.entrySet().removeIf(e -> !e.getKey().isAlive());
		PENDING_WATCHERS.replaceAll((z, ticks) -> ticks - 1);
		PENDING_WATCHERS.entrySet().removeIf(e -> {
			if (e.getValue() <= 0) {
				ZombieEntity zombie = e.getKey();
				if (zombie.hasStackEquipped(EquipmentSlot.HEAD)) {
					String texture = ItemUtils.getHeadTexture(zombie.getEquippedStack(EquipmentSlot.HEAD));
					if (WATCHER_SKINS.contains(texture)) {
						WATCHERS.add(zombie);
					}
				}
				return true;
			}
			return false;
		});

		WATCHERS.removeIf(watcher -> !watcher.isAlive());
		for (ZombieEntity watcher : WATCHERS) {
			if (!watcher.isAlive()) continue;
			var stands = watcher.getWorld().getEntitiesByClass(
					ArmorStandEntity.class,
					watcher.getBoundingBox().expand(8),
					stand -> stand.hasStackEquipped(EquipmentSlot.HEAD) && !MOBS.containsKey(stand)
			);
			for (ArmorStandEntity stand : stands) {
				MOBS.put(stand, new TrackedMob(stand));
			}
		}

		MOBS.entrySet().removeIf(e -> !e.getKey().isAlive());
		for (TrackedMob mob : MOBS.values()) {
			mob.update(mob.entity.getPos(), now);
		}
	}

	private static void render(WorldRenderContext context) {
		if (!Utils.isInDungeons()) return;
		for (TrackedMob mob : MOBS.values()) {
			if (mob.predictedPos != null && mob.inMotion) {
				RenderHelper.renderOutline(context, mob.entity.getBoundingBox().offset(0f, 2f, 0f), LINE_COLOR, 2, true);
				RenderHelper.renderLinesFromPoints(context,
						new Vec3d[]{mob.entity.getPos().add(0f, 2f, 0f), mob.predictedPos.add(0f, 2f, 0f)}, LINE_COLOR, 0.5f, 2f, true);
				Box box = new Box(mob.predictedPos.x - 0.5, mob.predictedPos.y + 2, mob.predictedPos.z - 0.5,
						mob.predictedPos.x + 0.5, mob.predictedPos.y + 4, mob.predictedPos.z + 0.5);
				RenderHelper.renderOutline(context, box, BOX_COLOR, 5, true);
			}
		}
	}

	private static void reset() {
		WATCHERS.clear();
		MOBS.clear();
		PENDING_WATCHERS.clear();
		mobsPredicted = 0;
	}

	private static class TrackedMob {
		final ArmorStandEntity entity;
		final Vec3d startPos;
		long startTime = -1;
		Vec3d lastPos;
		long lastTime;
		boolean firstWave;
		static final int DELTA_SAMPLES = 5;
		final Deque<Vec3d> deltas = new ArrayDeque<>();
		boolean inMotion = false;
		Vec3d predictedPos;

		TrackedMob(ArmorStandEntity entity) {
			this.entity = entity;
			this.startPos = entity.getPos();
			this.lastPos = this.startPos;
			this.lastTime = System.currentTimeMillis();
		}

		void update(Vec3d currentPos, long now) {
			long dt = now - lastTime;
			lastTime = now;
			Vec3d delta = currentPos.subtract(lastPos);
			lastPos = currentPos;
			inMotion = delta.lengthSquared() > 0 && dt > 0;
			if (inMotion) {
				if (startTime < 0) {
					startTime = now - dt;
				}
				if (deltas.size() == DELTA_SAMPLES) {
					deltas.removeFirst();
				}
				deltas.addLast(delta);
			}
			if (deltas.size() == DELTA_SAMPLES && predictedPos == null) {
				Vec3d totalDelta = Vec3d.ZERO;
				for (Vec3d d : deltas) {
					totalDelta = totalDelta.add(d);
				}
				Vec3d avg = totalDelta.multiply(1.0 / deltas.size());
				Vec3d dir = avg.normalize();

				firstWave = mobsPredicted < 4;
				mobsPredicted++;

				double distance = firstWave ? 15.6 + 0.5 : 11.4 + 0.5;
				predictedPos = startPos.add(dir.multiply(distance));
			}
		}
	}
}
