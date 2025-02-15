package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import it.unimi.dsi.fastutil.Pair;

import java.util.ArrayList;
import java.util.List;

public class SeaCreatureTracker { //todo handle double hook
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final List<LiveSeaCreature> seaCreatures = new ArrayList<>(); //todo clear on world change ect
	private static SeaCreature lastCatch;


	@Init
	public static void init() {
		ChatEvents.RECEIVE_STRING.register(SeaCreatureTracker::onChatMessage);
		ClientEntityEvents.ENTITY_UNLOAD.register(SeaCreatureTracker::onEntityDespawn);
	}


	private static void onEntityDespawn(Entity entity, ClientWorld clientWorld) {
		seaCreatures.removeIf(liveSeaCreature -> liveSeaCreature.entity.equals(entity));
	}

	//
	public static void onEntitySpawn(ArmorStandEntity armorStand) {
		if (!armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible() || lastCatch == null) {
			return;
		}
		if (armorStand.getName().getString().contains(lastCatch.name)) { //todo probaly use pattern
			seaCreatures.add(new LiveSeaCreature(lastCatch, armorStand, System.currentTimeMillis()));
			System.out.println("found new sea creature");
			lastCatch = null;
		}
	}

	private static void onChatMessage(String s) {
		String message = Formatting.strip(s);
		//todo check enabled / is fishing?
		//see if message matches any creature
		for (SeaCreature creature : SeaCreature.values()) {
			if (creature.chatMessage.equals(message)) {
				//found a sea creature add it to current creatures
				System.out.println("found creature: " + creature.name());
				lastCatch = creature;
				break;
			}
		}
	}

	protected static long getOldestSeaCreatureAge() {
		return System.currentTimeMillis() - seaCreatures.getFirst().spawnTime;
	}

	protected static Boolean isCreaturesAlive() {
		return !seaCreatures.isEmpty();
	}


	protected static Pair<String,Formatting> getTimerText(long maxTime, long currentTime) {
		String time = SkyblockTime.formatTime((maxTime - currentTime) / 1000f).getString();
		//colour text depending on time left
		float percentage = (float) (maxTime - currentTime) / (float) maxTime;
		if (percentage > 0.4) {
			return  Pair.of(time, Formatting.DARK_GREEN);
		} else if (percentage > 0.1) {
			return  Pair.of(time, Formatting.GOLD);
		} else {
			return  Pair.of(time, Formatting.RED);
		}
	}
	protected static int seaCreatureCount(){
		return seaCreatures.size();
	}

	protected static int getSeaCreatureCap() {
		return switch (Utils.getLocation()){
			case CRYSTAL_HOLLOWS -> 20;
			case CRIMSON_ISLE -> 5;
			default -> 30;//todo config cap
		};
	}



	record LiveSeaCreature(SeaCreature seaCreature, Entity entity, Long spawnTime) {}


}

